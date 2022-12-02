// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content

import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.SubscriptionRepository
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Order
import com.msr.bine_sdk.models.Error

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(@ApplicationContext val context: Context,
    private var subscriptionRepository: SubscriptionRepository,
    private var contentRepository: ContentRepository,
    private var orderRepository: OrderRepository,
    val subscriptionManager: SubscriptionManager): ViewModel() {

    var allSubscriptionPack = subscriptionRepository.allSubscriptionPack
    var subscriptionPack = MutableLiveData<List<SubscriptionPack>>()
    var purchasedSubscriptions = MutableLiveData<List<Order>>()
    var error = MutableLiveData<String>()
    var loading = MutableLiveData<Boolean>()
    var searchByQueryResult = MutableLiveData<List<SubscriptionPack>>()
    val allSubscriptionPackList = allSubscriptionPack.value
    var populateListWithContent = true

    fun getAllSubscriptionPacks() {
        subscriptionRepository.fetchAsyncAllSubscriptionsIfTime()
    }

    fun getSubscriptionPackFromQuery(query: String) {
        val qry = query.lowercase()
        val returnValue = ArrayList<SubscriptionPack>()
        CoroutineScope(Dispatchers.Default).launch {
            if (populateListWithContent && !allSubscriptionPackList.isNullOrEmpty()) {
                populateContentListOfSubscription(allSubscriptionPackList)
                populateListWithContent = false
            }
            if (!allSubscriptionPackList.isNullOrEmpty()) {
                for (sub in allSubscriptionPackList) {
                    when {
                        sub.title.lowercase().contains(qry) -> {
                            returnValue.add(sub)
                        }
                        else -> {
                            searchByQueryInSubscriptionContent(sub, qry, returnValue)
                        }
                    }
                }
            }
            searchByQueryResult.postValue(returnValue)
        }

    }

    private fun searchByQueryInSubscriptionContent(sub: SubscriptionPack, qry: String, returnValue: ArrayList<SubscriptionPack>) {
        if (!sub.contentList.isNullOrEmpty()) {
            for (con in sub.contentList!!) {
                when {
                    con.title.lowercase().contains(qry) -> {
                        returnValue.add(sub)
                        break
                    }
                    con.artists.toString().lowercase().contains(qry) -> {
                        returnValue.add(sub)
                        break
                    }
                    con.genre.lowercase().contains(qry) -> {
                        returnValue.add(sub)
                        break
                    }
                    con.language.lowercase().contains(qry) -> {
                        returnValue.add(sub)
                        break
                    }
                    !con.yearOfRelease.isNullOrEmpty() &&
                            con.yearOfRelease.lowercase().contains(qry) -> {
                        returnValue.add(sub)
                        break
                    }

                }
            }
        }
    }

    fun getSubscriptionRepository(): SubscriptionRepository {
        return subscriptionRepository
    }

    fun getSubscriptionPacks(contentProviderId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val content = subscriptionRepository.getSubscriptions(contentProviderId)
            content.first?.let {subs->
                populateContentListOfSubscription(subs)
                subscriptionPack.postValue(subs)
            } ?:
            content.second?.let {
                //Error
                when(it){
                    Error.NETWORK_ERROR -> {
                        error.postValue(context.getString(R.string.error_no_internet_connection))
                    }
                    else ->
                        error.postValue(context.getString(R.string.bn_server_error))
                }
            }
        }
    }

    private suspend fun populateContentListOfSubscription(subs: List<SubscriptionPack>) {
            val cpIdToContentListMap = HashMap<String, List<Content>?>()
            for (sub in subs) {
                if (sub.subscriptionType == SubscriptionPack.PackType.TVOD.value) {
                    populateContentListForTvodSubscription(sub)
                } else {
                    if(!cpIdToContentListMap.containsKey(sub.contentProviderId)){
                        val allContentOfCP = contentRepository.getPaidContentListByContentProviderId(sub.contentProviderId)
                        cpIdToContentListMap[sub.contentProviderId] = allContentOfCP?.distinctBy { con-> con.name }
                    }
                    cpIdToContentListMap[sub.contentProviderId]?.let {
                        sub.contentList = ArrayList(it)
                    }
                }

        }
    }

    private fun populateContentListForTvodSubscription(sub: SubscriptionPack) {
        sub.contentIdList?.let { contentIds ->
            val contentList = ArrayList<Content>()
            for (conId in contentIds) {
                val contentObj = contentRepository.getContent(conId!!)
                contentObj?.let {
                    contentList.add(it)
                }
            }
            sub.contentList = ArrayList(contentList.distinctBy { con -> con.name })
        }
    }

    fun getUserSubscription() {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.OMS().getOrders(arrayListOf("Completed"))
            loading.postValue(false)
            response.result?.let {
                purchasedSubscriptions.postValue(it)
            }

            response.error?.let {
                when(it){
                    Error.NETWORK_ERROR -> {
                        error.postValue(context.getString(R.string.error_no_internet_connection))
                    }
                    Error.API_ERROR ->{
                        if(response.code == 404){
                            error.postValue(context.getString(R.string.no_subscription_found))
                        }
                        else{
                            error.postValue(it.toString())
                        }
                    }
                    else ->
                        error.postValue(it.toString())
                }
            }
        }
    }

    suspend fun hasActiveSubscriptionForSubscriptionId(subscriptionId: String): Boolean {
        val sub = orderRepository.getActiveSubscriptionBySubscriptionId(subscriptionId)
        return sub != null && !sub.isExpired()
    }

    suspend fun hasActiveOrderForId(orderId: String): Boolean {
        val order = orderRepository.getActiveOrderByOrderId(orderId)
        return order != null
    }

    fun hasActiveOrder(contentProviderId: String): Boolean {
        return subscriptionManager.hasActiveOrder(contentProviderId)
    }

    fun getSubscriptionByIdLiveData(subscriptionId: String) = subscriptionRepository.getSubscriptionByIdLiveData(subscriptionId)


    /* pack can not be purchased if
       1. User holding any svod pack and requesting another pack of the same CP type
       2. User having active order for that same pack
       3. User having active subscription for that pack
       4. The pack must exist while renewing(can be possible that pack was available previously but not available now)
    */

    var canBuyPack = MutableLiveData<Boolean>()
    fun canBuyPack(order: Order) {
        CoroutineScope(Dispatchers.Default).launch {
            val canBuy = !((subscriptionManager.hasSvodActiveSubscription(order.orderItems[0].subscription.contentProviderId)) ||
                    hasActiveOrderForId(order.id) ||
                    hasActiveSubscriptionForSubscriptionId(order.orderItems[0].subscription.id) ||
                    subscriptionRepository.getSubscriptionById(order.orderItems[0].subscription.id) == null)
            canBuyPack.postValue(canBuy)
        }
    }

    fun hasValidSubscriptionForContent(contentProviderId: String, contentId: String):Boolean{
        return subscriptionManager.hasValidSubscriptionForContent(contentProviderId, contentId)
    }
    fun hasValidSubscriptionForContent(contentProviderId: String, contentId: String, activeSubscriptionList: List<ActiveSubscription>?):Boolean{
        return subscriptionManager.hasValidSubscriptionForContent(contentProviderId, contentId, activeSubscriptionList)
    }
    fun hasSvodActiveSubscription(contentProviderId: String):Boolean{
        return subscriptionManager.hasSvodActiveSubscription(contentProviderId)
    }

}