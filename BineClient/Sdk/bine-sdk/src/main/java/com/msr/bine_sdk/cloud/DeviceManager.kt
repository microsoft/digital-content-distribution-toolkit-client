package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.cloud.models.ContentAvailabilityCountRequest
import com.msr.bine_sdk.cloud.models.ContentAvailabilityRequest
import com.msr.bine_sdk.cloud.models.PageRequest
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.network.NetworkResponse
import javax.inject.Inject
import com.msr.bine_sdk.models.Error
import org.greenrobot.eventbus.EventBus

class DeviceManager @Inject constructor(private val service: DeviceLocalService,
                                        private val cloudService: DeviceCloudService) {
    suspend fun getFolderPath(hubIp: String, contentId: String): APIResponse.FolderPathResponse {
        return when (val response = service.getFolderPath(hubIp, contentId)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetFolderPath", "Success", response.body.folderpath))
                APIResponse.FolderPathResponse(response.body, 200,null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetFolderPath", "APIError", response.body))
                APIResponse.FolderPathResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetFolderPath", "APIError", response.body.details))
                APIResponse.FolderPathResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetFolderPath", "NetworkError", response.error.message))
                APIResponse.FolderPathResponse(null, 0, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetFolderPath", "UnknownError", response.error?.message))
                APIResponse.FolderPathResponse(null, null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getDeviceId(hubIp: String): APIResponse.Response {
        return when (val response = service.getDeviceId(hubIp)) {
            is NetworkResponse.Success -> {
                val hubId = response.headers["HubId"] ?: ""
                EventBus.getDefault().post(LogEvent("GetDeviceId", "Success", hubId))
                APIResponse.Response(hubId, 200,null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetDeviceId", "APIError", response.body))
                APIResponse.Response(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetDeviceId", "APIError", response.body.details))
                APIResponse.Response(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetDeviceId", "NetworkError", response.error.message))
                APIResponse.Response(null, 0, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetDeviceId", "UnknownError", response.error?.message))
                APIResponse.Response(null, null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun browseContent(hubId: String, contentProviderId: String, continuationToken: String, pageSize: Int): APIResponse.DeviceContentResponse {
        return when (val response = cloudService.browseContent(hubId, contentProviderId,
            PageRequest(continuationToken, pageSize))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("BrowseContent", "Success", ""))
                APIResponse.DeviceContentResponse(response.body.data, response.body.continuationToken,200,null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("BrowseContent", "APIError", response.body))
                APIResponse.DeviceContentResponse(null, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("BrowseContent", "APIError", response.body.details))
                APIResponse.DeviceContentResponse(null, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("BrowseContent", "NetworkError", response.error.message))
                APIResponse.DeviceContentResponse(null, null, 0, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("BrowseContent", "UnknownError", response.error?.message))
                APIResponse.DeviceContentResponse(null, null, null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getContentAvailability(contentId: String, deviceIds: Array<String>, includeActiveContentCount: Boolean): APIResponse.ContentAvailabilityResponse {
        return when (val response = cloudService.contentAvailability(ContentAvailabilityRequest(contentId, deviceIds, includeActiveContentCount))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailability", "Success", ""))
                APIResponse.ContentAvailabilityResponse(response.body, null,200,null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailability", "APIError", response.body))
                APIResponse.ContentAvailabilityResponse(null, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailability", "APIError", response.body.details))
                APIResponse.ContentAvailabilityResponse(null, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailability", "NetworkError", response.error.message))
                APIResponse.ContentAvailabilityResponse(null, null, 0, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailability", "UnknownError", response.error?.message))
                APIResponse.ContentAvailabilityResponse(null, null, null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getContentAvailabilityCount(contentId: String, deviceIds: Array<String>): APIResponse.DeviceContentResponse {
        return when (val response = cloudService.contentAvailabilityCount(
            ContentAvailabilityCountRequest(contentId, deviceIds))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailabilityCount", "Success", ""))
                APIResponse.DeviceContentResponse(response.body, null,200,null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailabilityCount", "APIError", response.body))
                APIResponse.DeviceContentResponse(null, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailabilityCount", "APIError", response.body.details))
                APIResponse.DeviceContentResponse(null, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailabilityCount", "NetworkError", response.error.message))
                APIResponse.DeviceContentResponse(null, null, 0, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetContentAvailabilityCount", "UnknownError", response.error?.message))
                APIResponse.DeviceContentResponse(null, null, null, null, Error.UNKNOWN_ERROR)
            }
        }
    }
}