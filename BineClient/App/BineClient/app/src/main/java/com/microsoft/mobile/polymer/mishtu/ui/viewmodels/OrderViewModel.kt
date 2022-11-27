package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils

import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Order
import com.msr.bine_sdk.models.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    var error = MutableLiveData<Error>()
    var errorCode = MutableLiveData<Int>()
    var success = MutableLiveData<Boolean>()
    var loading = MutableLiveData<Boolean>()

    var orders = MutableLiveData<List<Order>>()
    var subscription = MutableLiveData<SubscriptionPack>()
    var cancelOrder = MutableLiveData<String>()
    val currentOrder = MutableLiveData<com.microsoft.mobile.polymer.mishtu.storage.entities.Order>()

    fun createOrder(subscription: SubscriptionPack) {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response =
                BineAPI.OMS().createOrder(subscription.contentProviderId, subscription.id)
            Log.d("order", response.toString())
            loading.postValue(false)
            response.result?.let {
                if(subscription.subscriptionType == SubscriptionPack.PackType.SVOD.value) {
                    SharedPreferenceStore.getInstance()
                            .save(SharedPreferenceStore.KEY_ORDER_ID + "_${subscription.contentProviderId}",
                                    it)
                }

                //Telemetry
                AnalyticsLogger.getInstance()
                    .logOrderCreated(it, subscription.id, subscription.contentProviderId)

                orderRepository.getActiveOrder(subscription.contentProviderId)

                success.postValue(true)
            } ?: response.error?.let {
                if (response.code == 400) {
                    errorCode.postValue(400)
                } else {
                    error.postValue(it)
                }
            }
        }
    }

    fun getAllOrders() {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.OMS().getOrders(arrayListOf())
            loading.postValue(false)
            response.result?.let {

                Collections.sort(it) { order: Order, order1: Order ->
                    return@sort TimestampUtils.getDateFromUTCString(order1.orderCreatedDate)
                        .compareTo(TimestampUtils.getDateFromUTCString(order.orderCreatedDate))
                }
                orders.postValue(it)
            } ?: response.error?.let {
                error.postValue(it)
            }
        }
    }

    fun cancelOrder(orderId: String, contentProviderId: String) {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.OMS().cancelOrder(orderId)
            loading.postValue(false)
            response.result?.let {
                orderRepository.deleteOrderByOrderId(orderId)
                SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_ORDER_ID + "_$contentProviderId", "")
                cancelOrder.postValue("Order canceled")
            }
            response.error?.let {
                error.postValue(it)
            }
        }
    }

    fun getActiveorder(orderId: String) {
        viewModelScope.launch {
            currentOrder.postValue(orderRepository.getActiveOrderByOrderId(orderId))
        }
    }
    fun getSubscriptionPackLiveData(subscriptionId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            subscription.postValue(orderRepository.getSubscriptionById(subscriptionId))
        }
    }



}