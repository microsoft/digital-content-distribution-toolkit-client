package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.cloud.models.CreateUserRequest
import com.msr.bine_sdk.cloud.models.DataExportRequest
import com.msr.bine_sdk.cloud.models.UpdateUserRequest
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.models.Error

import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class UserManager@Inject constructor(private val service: UserService) {

    suspend fun createUser(userName: String): APIResponse.CreateUserResponse {
        return when (val response = service.createUser(CreateUserRequest(userName, "ConsumerApp"))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("createUser", "Success", response.body))
                APIResponse.CreateUserResponse(response.body, null, response.body, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("createUser", "APIError", response.body))
                APIResponse.CreateUserResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("createUser", "APIError", response.body.details))
                APIResponse.CreateUserResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("createUser", "NetworkError", response.error.message))
                APIResponse.CreateUserResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("createUser", "UnknownError", response.error?.message))
                APIResponse.CreateUserResponse(null, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun assignRetailer(referralCode: String): APIResponse.ReferralCodeResponse {
        return when (val response = service.assignRetailer(referralCode)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("assignRetailer", "Success", response.body))
                APIResponse.ReferralCodeResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("assignRetailer", "APIError", response.body))
                APIResponse.ReferralCodeResponse(null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("assignRetailer", "APIError", response.body.details))
                APIResponse.ReferralCodeResponse(null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("assignRetailer", "NetworkError", response.error.message))
                APIResponse.ReferralCodeResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                //Empty Body
                if (response.code in 200..204) {
                    EventBus.getDefault().post(LogEvent("assignRetailer", "Success", null))
                    APIResponse.ReferralCodeResponse(null, null, null, null)
                }
                else {
                    EventBus.getDefault().post(LogEvent("assignRetailer", "UnknownError", response.error?.message))
                    APIResponse.ReferralCodeResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
                }
            }
        }
    }


    suspend fun userDetail(): APIResponse.UserDetailResponse {
        return when (val response = service.userDetail()) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("userDetail", "Success", response.body.phoneNumber))
                APIResponse.UserDetailResponse(response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("userDetail", "APIError", response.body))
                APIResponse.UserDetailResponse(null, response.code, response.body, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("userDetail", "APIError", response.body.details))
                APIResponse.UserDetailResponse(null, response.code, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("userDetail", "NetworkError", response.error.message))
                APIResponse.UserDetailResponse(null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                //Empty Body
                if (response.code in 200..204) {
                    EventBus.getDefault().post(LogEvent("userDetail", "Success", null))
                    APIResponse.UserDetailResponse(null, null, null, null)
                }
                else {
                    EventBus.getDefault().post(LogEvent("userDetail", "UnknownError", response.error?.message))
                    APIResponse.UserDetailResponse(null, response.code, response.error?.message, Error.UNKNOWN_ERROR)
                }
            }
        }
    }

    suspend fun updateUser(userName: String): APIResponse.UserUpdateResponse {
        return when (val response = service.updateUser(UpdateUserRequest(userName))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("updateUser", "Success", response.body))
                APIResponse.UserUpdateResponse(true, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("updateUser", "APIError", response.body))
                APIResponse.UserUpdateResponse(false, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("updateUser", "APIError", response.body.details))
                APIResponse.UserUpdateResponse(false, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("updateUser", "NetworkError", response.error.message))
                APIResponse.UserUpdateResponse(false, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("updateUser", "UnknownError", response.error?.message))
                APIResponse.UserUpdateResponse(false, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun dataExport(userName: String): APIResponse.DataExportResponse {
        return when (val response = service.dataExport(DataExportRequest(userName))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("DataExport", "Success", response.body.toString()))
                APIResponse.DataExportResponse(true, response.body, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("DataExport", "APIError", response.body))
                APIResponse.DataExportResponse(false, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("DataExport", "APIError", response.body.details))
                APIResponse.DataExportResponse(false, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("DataExport", "NetworkError", response.error.message))
                APIResponse.DataExportResponse(false, null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("DataExport", "UnknownError", response.error?.message))
                APIResponse.DataExportResponse(false, null, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun requestDataExport(userName: String): APIResponse.DataExportResponse {
        return when (val response = service.requestDataExport(DataExportRequest(userName))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("RequestDataExport", "Success", response.body.toString()))
                APIResponse.DataExportResponse(true, null, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("RequestDataExport", "APIError", response.body))
                APIResponse.DataExportResponse(false, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("RequestDataExport", "APIError", response.body.details))
                APIResponse.DataExportResponse(false, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("RequestDataExport", "NetworkError", response.error.message))
                APIResponse.DataExportResponse(false, null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("RequestDataExport", "UnknownError", response.error?.message))
                APIResponse.DataExportResponse(false, null, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun deleteUser(userName: String): APIResponse.DataExportResponse {
        return when (val response = service.deleteUser(DataExportRequest(userName))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("DeleteUser", "Success", response.body.toString()))
                APIResponse.DataExportResponse(true, null, null, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("DeleteUser", "APIError", response.body))
                APIResponse.DataExportResponse(false, null, response.code, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("DeleteUser", "APIError", response.body.details))
                APIResponse.DataExportResponse(false, null, response.code, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("DeleteUser", "NetworkError", response.error.message))
                APIResponse.DataExportResponse(false, null, null,response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("DeleteUser", "UnknownError", response.error?.message))
                APIResponse.DataExportResponse(false, null, response.code,response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }
}