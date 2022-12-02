// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.models.Error

import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class RetailerManager@Inject constructor(private val service: RetailerService) {

    suspend fun getNearbyRetailer(radius:Double, lat: Double, lng: Double): APIResponse.RetailerListResponse {
        return when (val response = service.getNearbyRetailers(radius, lat, lng)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("getNearbyRetailer", "Success", null))
                APIResponse.RetailerListResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("getNearbyRetailer", "APIError", response.body))
                APIResponse.RetailerListResponse(null, response.code, response.body, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("getNearbyRetailer", "APIError", response.body.details))
                APIResponse.RetailerListResponse(null, response.code, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("getNearbyRetailer", "NetworkError", response.error.message))
                APIResponse.RetailerListResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("getNearbyRetailer", "UnknownError", response.error?.message))
                APIResponse.RetailerListResponse(null, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }
}