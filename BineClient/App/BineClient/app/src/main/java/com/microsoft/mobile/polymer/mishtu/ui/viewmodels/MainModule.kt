// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.microsoft.mobile.auth.AuthManager
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.services.FirebaseMessageReceiverService
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.IncentivesRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.SubscriptionRepository
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Source

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltViewModel
class MainModule @Inject constructor(
    private val contentRepository: ContentRepository,
    private val orderRepository: OrderRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val incentivesRepository: IncentivesRepository
) : ViewModel() {

    var progressLiveData = MutableLiveData<Boolean>()
    var errorLiveData = MutableLiveData<String>()
    var tokenSuccess = MutableLiveData<Boolean>()
    var logoutLiveData = MutableLiveData<Boolean>()

    fun refreshToken() {
        val token = BineAPI.getInstance().getToken(Source.CLOUD)
        if (token.isNullOrEmpty()) {
            return
        }

        GlobalScope.launch {
            //Showing progress bar only until content provider id received
            //Content provider id is a necessary property for most of data fetch & API queries
            //Hence blocking the UI only if its empty (Happen ideally only after first install)
            /*val shouldShowProgress = contentRepository.getContentProviderId(BuildConfig.EROS_NOW_TITLE).isNullOrBlank()
            if (shouldShowProgress) progressLiveData.postValue(true)*/
            val response = AuthManager.getInstance().refreshToken(token)
            response.authToken?.let {
                if (it.isNotEmpty()) {
                    // 200 with token = token is about to expire hence server sent new token
                    BineAPI.getInstance().init(it, false, "")
                }
                // 200 with empty token = token is having good enough validity
                initData()
            } ?:
            response.error?.let {
                progressLiveData.postValue(false)
                //token is expired / incorrect will get 401 Unauthorised
                if (response.code == 401) {
                    logoutLiveData.postValue(true)
                    return@launch
                }
                //Other Error
                else errorLiveData.postValue(response.details ?: "")
                initData()
            }
        }
    }

    private suspend fun initData() {
        /*var contentProviderId = contentRepository.getContentProviderId(BuildConfig.EROS_NOW_TITLE)
        if (contentProviderId.isNullOrEmpty()) {
            val isFetched = contentRepository.fetchContentProviders()
            if (isFetched) {
                contentProviderId = contentRepository.getContentProviderId(BuildConfig.EROS_NOW_TITLE)
            }
            if (contentProviderId.isNullOrEmpty()) {
                progressLiveData.postValue(false)
                errorLiveData.postValue("Error Fetching Data")
                return
            }
        }*/
        val hasFCMRegisterFailed = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_FCM_TOKEN_FAILED)
        if(!hasFCMRegisterFailed.isNullOrEmpty()) FirebaseMessageReceiverService.registerFCM(null)

        contentRepository.fetchContentIfTime()
        subscriptionRepository.fetchAllSubscriptionsIfTime()
        orderRepository.getActiveSubscriptionForAllContentProvider()
        orderRepository.getActiveOrderForAllContentProvider()

        incentivesRepository.syncFailedEvent()
        progressLiveData.postValue(false)
    }
}