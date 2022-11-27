package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.cloud.models.*
import com.msr.bine_sdk.network.Error
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceCloudService {
    @POST(value = "BrowseDevice/{deviceId}/{contentProvideId}")
    suspend fun browseContent(@Path(value = "deviceId", encoded = true) deviceId: String,
                              @Path(value = "contentProvideId", encoded = true) contentProvideId: String,
                              @Body pageRequest: PageRequest): NetworkResponse<BrowseHubContentResponse, Error>

    @POST(value = "BrowseDevice/contentavailabilitycount")
    suspend fun contentAvailabilityCount(@Body contentAvailabilityCountRequest: ContentAvailabilityCountRequest): NetworkResponse<Array<String>, Error>

    @POST(value = "BrowseDevice/contentavailability")
    suspend fun contentAvailability(@Body contentAvailabilityRequest: ContentAvailabilityRequest): NetworkResponse<Array<DeviceContentDetails>, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "deviceAPI/api/v1/"

        fun create(sharedPreference: BineSharedPreference): DeviceCloudService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .authenticator(authenticator)
                .addInterceptor { chain ->
                    val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                    val newRequest = chain.request().newBuilder()
                        .addHeader(Constants.AUTH_HEADER, "Bearer $token")
                        .build()
                    chain.proceed(newRequest)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(NetworkResponseAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(DeviceCloudService::class.java)
        }
    }
}