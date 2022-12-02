// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.storage.repositories.IncentivesRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.RewardsEvent
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.IncentivePlan

import kotlinx.coroutines.launch

import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.greenrobot.eventbus.EventBus

@HiltViewModel
class IncentiveViewModel @Inject constructor(private val orderRepository: OrderRepository,
                                             private val incentivesRepository: IncentivesRepository): ViewModel(){

    var totalCoins = MutableLiveData<Int>()
    var error = MutableLiveData<String>()
    var success = MutableLiveData<Boolean>()
    var loading = MutableLiveData<Boolean>()
    var rewardsTrigged = MutableLiveData<Boolean>()

    var cachedCoins: Int = 0
        set(value) {
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_REWARDS_COINS, value.toString())
            field = value
        }
        get() {
            val value = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_REWARDS_COINS)
            return if (value.isNullOrEmpty()) 0
            else value.toInt()
        }

    fun getTotalCoins() {
        viewModelScope.launch {
            val response = BineAPI.Incentives().getIncentiveEvents()
            response.result?.let {
                cachedCoins = it.totalValue
                totalCoins.postValue(it.totalValue)
            }
        }
    }

    fun redeemOffer(subscription: SubscriptionPack) {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.OMS().redeemOffer(subscription.contentProviderId, subscription.id)
            loading.postValue(false)
            response.result?.let {
                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_REDEEM_OFFER_DONE, "true")
                //Telemetry
                AnalyticsLogger.getInstance().logOrderCreated(it, subscription.id, subscription.contentProviderId)
                orderRepository.getActiveSubscription(subscription.contentProviderId)
                success.postValue(true)
            } ?:
            response.details?.let {
                error.postValue(it)
            }
        }
    }

    fun expenseEventExist(): Boolean {
        return incentivesRepository.expenseEventExist()
    }

    fun triggerIncentiveEvents() {
        CoroutineScope(Dispatchers.Default).launch {
            val triggered = incentivesRepository.triggerAppStartEvents()

            if (triggered) {
                rewardsTrigged.postValue(true)
                return@launch
            }

            val shouldShowStreamingRewards = SharedPreferenceStore.getInstance().get(IncentivesRepository.showStreamingReward)
            if (!shouldShowStreamingRewards.isNullOrEmpty()) {
                incentivesRepository.getPlanDetails(BOConverter.EventType.CONTENT_STREAMED)?.let {
                    EventBus.getDefault().post(RewardsEvent(it.formula.firstOperand,
                        BOConverter.EventType.CONTENT_STREAMED, null))
                    SharedPreferenceStore.getInstance().save(IncentivesRepository.showStreamingReward, "")
                    rewardsTrigged.postValue(true)
                }
            }

            rewardsTrigged.postValue(false)
        }
    }

    fun planContainsEvent(
        event: BOConverter.EventType,
        contentProviderId: String?
    ): List<IncentivePlan.PlanDetail?> {
        return incentivesRepository.planContainsEvent(event,contentProviderId)
    }
}