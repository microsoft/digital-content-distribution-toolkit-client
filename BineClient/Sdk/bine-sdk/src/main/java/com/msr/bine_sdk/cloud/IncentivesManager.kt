package com.msr.bine_sdk.cloud

import android.util.Log
import com.msr.bine_sdk.cloud.models.RecordEvent
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.models.Error

import org.greenrobot.eventbus.EventBus
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class IncentivesManager @Inject constructor(private val service: IncentiveService) {

    enum class IncentiveEventType(val value:String) {
        APP_INSTALL("recordSignin"),
        APP_LAUNCH("recordAppLaunch"),
        CONTENT_STREAMED("recordContentStreamed"),
        RATE_RETAILER("recordOnboardingRatingSubmitted"),
        DOWNLOAD_COMPLETE("recordDownloadMediaCompleted")
    }

    suspend fun getIncentivePlan(): APIResponse.IncentivePlanResponse {
        return when (val response = service.getPlan()) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetIncentivePlan", "Success", null))
                APIResponse.IncentivePlanResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetIncentivePlan", "APIError", response.body))
                APIResponse.IncentivePlanResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetIncentivePlan", "APIError", response.body.details))
                APIResponse.IncentivePlanResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetIncentivePlan", "NetworkError", null))
                APIResponse.IncentivePlanResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetIncentivePlan", "UnknownError", null))
                APIResponse.IncentivePlanResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }


    suspend fun getIncentiveEvents(): APIResponse.IncentiveEventResponse {
        return when (val response = service.getEvents()) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetIncentiveEvents", "Success", null))
                APIResponse.IncentiveEventResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetIncentiveEvents", "APIError", response.body))
                APIResponse.IncentiveEventResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetIncentiveEvents", "APIError", response.body.details))
                APIResponse.IncentiveEventResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetIncentiveEvents", "NetworkError", null))
                APIResponse.IncentiveEventResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetIncentiveEvents", "UnknownError", null))
                APIResponse.IncentiveEventResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun recordEvent(event: IncentiveEventType, date: Date, id: String?, deviceId: String?): APIResponse.RecordEventResponse {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        Log.d("RecordEvent", dateFormat.format(date))

        var retryCount = 0
        var response = recordEventCall(event, dateFormat.format(date), id,deviceId)
        while (response.error == Error.NETWORK_ERROR && retryCount < 3) {
            retryCount++
            response = recordEventCall(event, dateFormat.format(date), id,deviceId)
        }
        return response
    }

    private suspend fun recordEventCall(event: IncentiveEventType, date: String, id: String?, deviceId: String?) : APIResponse.RecordEventResponse{
        return when (val response = service.recordEvent(event.value, RecordEvent(date, id, deviceId))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("RecordEvent-${event.value}", "Success", null))
                APIResponse.RecordEventResponse(true, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("RecordEvent-${event.value}", "APIError", response.body))
                APIResponse.RecordEventResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("RecordEvent-${event.value}", "APIError", response.body.details))
                APIResponse.RecordEventResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("RecordEvent-${event.value}", "NetworkError", response.error.message))
                APIResponse.RecordEventResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("RecordEvent-${event.value}", "UnknownError", null))
                APIResponse.RecordEventResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }
}