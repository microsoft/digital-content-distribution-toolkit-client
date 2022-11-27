package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.cloud.models.PageRequest
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.secure.BineSharedPreference
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import com.msr.bine_sdk.models.Error

class CloudManager @Inject constructor(private val service: CloudService,
                                       private val sharedPreference: BineSharedPreference) {

    suspend fun getContentProviders(): APIResponse.ContentProviderResponse {

        return when (val response = service.getContentProviders()) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "Success", null))
                APIResponse.ContentProviderResponse(response.body, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "APIError", response.body))
                APIResponse.ContentProviderResponse(null, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "APIError", response.body.details))
                APIResponse.ContentProviderResponse(null, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "NetworkError", response.error.message))
                APIResponse.ContentProviderResponse(null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetContentProviders", "UnknownError", response.error?.message))
                APIResponse.ContentProviderResponse(null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getContent(contentProviderId: String, continuationToken: String, pageSize: Int): APIResponse.ContentResponse {

        return when (val response = service.getContent(contentProviderId,
            PageRequest(continuationToken, pageSize))) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetContent", "Success", null))
                APIResponse.ContentResponse(response.body.data, response.body.continuationToken, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetContent", "APIError", response.body))
                APIResponse.ContentResponse(null, null, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetContent", "APIError", response.body.details))
                APIResponse.ContentResponse(null, null, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetContent", "NetworkError", response.error.message))
                APIResponse.ContentResponse(null, null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetContent", "UnknownError", response.error?.message))
                APIResponse.ContentResponse(null, null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getSubscriptions(contentProviderId: String): APIResponse.SubscriptionResponse {

        return when (val response = service.getSubscriptions(contentProviderId)) {
            is NetworkResponse.Success -> {
                EventBus.getDefault().post(LogEvent("GetSubscriptions", "Success", null))
                APIResponse.SubscriptionResponse(response.body, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                EventBus.getDefault().post(LogEvent("GetSubscriptions", "APIError", response.body))
                APIResponse.SubscriptionResponse(null, response.body, Error.API_ERROR)
            }
            is NetworkResponse.ApiError -> {
                EventBus.getDefault().post(LogEvent("GetSubscriptions", "APIError", response.body.details))
                APIResponse.SubscriptionResponse(null, response.body.details, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                EventBus.getDefault().post(LogEvent("GetSubscriptions", "NetworkError", response.error.message))
                APIResponse.SubscriptionResponse(null, response.error.message, Error.NETWORK_ERROR)
            }
            is NetworkResponse.UnknownError -> {
                EventBus.getDefault().post(LogEvent("GetSubscriptions", "UnknownError", response.error?.message))
                APIResponse.SubscriptionResponse(null, response.error?.message, Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getNearestHubs(radius: Double,
                               lat: Double,
                               lng: Double) : APIResponse.HubResponse{

        return when (val response = service.getNearestHubs(radius, lat, lng)) {
            is NetworkResponse.Success -> {
                APIResponse.HubResponse(response.body, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                APIResponse.HubResponse(null, response.body, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                APIResponse.HubResponse(null, response.error.message, Error.NETWORK_ERROR)
            }
            else -> {
                APIResponse.HubResponse(null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    /*fun getAssetToken(contentId: String, callback: (result: String?, error: String?) -> Unit){
        val gson: Gson = GsonBuilder()
                .setLenient()
                .create()
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BuildConfig.BASE_URL + "cmsapi/api/v1/")
                .build()
        val cloudService: CloudService = retrofit.create(CloudService::class.java)
        val token: Call<String> = cloudService.getAssetToken(contentId)

        token.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    EventBus.getDefault().post(LogEvent("AccessToken", response.body()!!, null))
                    saveAssetTokens(response.body()!!)
                    callback(response.body()!!, null)
                } else if ( response.errorBody() != null){
                    EventBus.getDefault().post(LogEvent("AccessToken", response.errorBody().toString(), null))
                    callback(null, response.errorBody().toString())
                }
                else {
                    EventBus.getDefault().post(LogEvent("AccessToken", "Unknown Error", null))
                    callback(null, "Unknown Error")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                EventBus.getDefault().post(LogEvent("AccessToken", t.localizedMessage, null))
                callback(null, t.localizedMessage)
            }
        })
    }*/

    suspend fun refreshAssetToken() : APIResponse.TokenResponse{

        return when (val response = service.refreshAssetToken()) {
            is NetworkResponse.Success -> {
                //TODO: saveAssetTokens(response.body)
                APIResponse.TokenResponse(response.body.access, null, null)
            }
            is NetworkResponse.Api4xxError -> {
                APIResponse.TokenResponse(null, response.body, Error.API_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                APIResponse.TokenResponse(null, response.error.message, Error.NETWORK_ERROR)
            }
            else -> {
                APIResponse.TokenResponse(null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    private fun saveAssetTokens(token: String) {
        sharedPreference.saveSecure(BineSharedPreference.KEY_TOKEN_ASSET,
                token)
        sharedPreference.saveSecure(BineSharedPreference.KEY_RTOKEN_ASSET,
                token)
    }
}