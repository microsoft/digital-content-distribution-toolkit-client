// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.cloud.models.CreateOrderRequest
import com.msr.bine_sdk.cloud.models.OrderListRequest
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.models.Error

import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class OrderManager@Inject constructor(private val service: OMService) {

    suspend fun createOrder(contentProviderId: String, subscriptionId: String): APIResponse.CreateOrderResponse {
        return when (val response = service.createOrder(CreateOrderRequest(subscriptionId, contentProviderId))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("CreateOrder", "Success", null))
                APIResponse.CreateOrderResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "APIError", response.body))
                APIResponse.CreateOrderResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "APIError", response.body.details))
                APIResponse.CreateOrderResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "NetworkError", response.error.message))
                APIResponse.CreateOrderResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "UnknownError", response.error?.message))
                APIResponse.CreateOrderResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getOrder(orderId: String): APIResponse.GetOrderResponse {
        return when (val response = service.getOrder(orderId)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetOrder", "Success", null))
                APIResponse.GetOrderResponse(arrayListOf(response.body), null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetOrder", "APIError", response.body))
                APIResponse.GetOrderResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetOrder", "APIError", response.body.details))
                APIResponse.GetOrderResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetOrder", "NetworkError", response.error.message))
                APIResponse.GetOrderResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetOrder", "UnknownError", response.error?.message))
                APIResponse.GetOrderResponse(null, null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getOrders(array: ArrayList<String>): APIResponse.GetOrderResponse {
        return when (val response = service.getOrders(OrderListRequest(array))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetOrders", "Success", null))
                APIResponse.GetOrderResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetOrders", "APIError", response.body))
                APIResponse.GetOrderResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetOrders", "APIError", response.body.details))
                APIResponse.GetOrderResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetOrders", "NetworkError", response.error.message))
                APIResponse.GetOrderResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetOrders", "UnknownError", response.error?.message))
                APIResponse.GetOrderResponse(null, null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getActiveSubscriptions(): APIResponse.ActiveSubscriptionResponse {
        return when (val response = service.getActiveSubscription()) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("getActiveSubscriptions", "Success", null))
                APIResponse.ActiveSubscriptionResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("getActiveSubscriptions", "APIError", response.body))
                APIResponse.ActiveSubscriptionResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("getActiveSubscriptions", "APIError", response.body.details))
                APIResponse.ActiveSubscriptionResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("getActiveSubscriptions", "NetworkError", response.error.message))
                APIResponse.ActiveSubscriptionResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("getActiveSubscriptions", "UnknownError", response.error?.message))
                APIResponse.ActiveSubscriptionResponse(null, null,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }
    suspend fun cancelOrder(orderId: String): APIResponse.GetOrderResponse {
        return when (val response = service.cancelOrder(orderId)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("CancelOrder", "Success", null))
                APIResponse.GetOrderResponse(listOf(), null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("CancelOrder", "APIError", response.body))
                APIResponse.GetOrderResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("CancelOrder", "APIError", response.body.details))
                APIResponse.GetOrderResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("CancelOrder", "NetworkError", response.error.message))
                APIResponse.GetOrderResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("CancelOrder", "UnknownError", response.error?.message))
                APIResponse.GetOrderResponse(null, null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getAssetToken(contentId: String): APIResponse.AssetTokenResponse{
        return when (val response = service.getAssetToken(contentId)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("AccessToken", "Success", null))
                APIResponse.AssetTokenResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("AccessToken", "APIError", response.body))
                APIResponse.AssetTokenResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("AccessToken", "APIError", response.body.details))
                APIResponse.AssetTokenResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("AccessToken", "NetworkError", response.error.message))
                APIResponse.AssetTokenResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("AccessToken", "UnknownError", response.error?.message))
                APIResponse.AssetTokenResponse(null, null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun redeemOffer(contentProviderId: String, subscriptionId: String): APIResponse.CreateOrderResponse {
        return when (val response = service.redeemOffer(CreateOrderRequest(subscriptionId, contentProviderId))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("RedeemOffer", "Success", null))
                APIResponse.CreateOrderResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("RedeemOffer", "APIError", response.body))
                APIResponse.CreateOrderResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("RedeemOffer", "APIError", response.body.details))
                APIResponse.CreateOrderResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("RedeemOffer", "NetworkError", response.error.message))
                APIResponse.CreateOrderResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("RedeemOffer", "UnknownError", response.error?.message))
                APIResponse.CreateOrderResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }
}