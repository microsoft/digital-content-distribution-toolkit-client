// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.hub.TokenAuthenticator
import com.msr.bine_sdk.hub.model.FolderPathResponse
import com.msr.bine_sdk.network.Error
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.network_old.RetryRequestInterceptor
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceLocalService {
    @GET(value = "http://{hubIP}:5000/folderpath")
    suspend fun getFolderPath(@Path(value = "hubIP", encoded = true) hubIP: String, @Query("assetId") id: String): NetworkResponse<FolderPathResponse, Error>

    @GET(value = "http://{hubIP}:5000/deviceId")
    suspend fun getDeviceId(@Path(value = "hubIP", encoded = true) hubIP: String): NetworkResponse<Void, Error>

    companion object {

        fun create(sharedPreference: BineSharedPreference): DeviceLocalService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .authenticator(authenticator)
                    .addInterceptor(RetryRequestInterceptor())
                    .addInterceptor { chain ->
                        val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_HUB)
                        val newRequest = chain.request().newBuilder()
                                .addHeader(Constants.AUTH_HEADER, "Bearer $token")
                                .build()
                        chain.proceed(newRequest)
                    }
                    .build()
            val baseURL = String.format("http://%s:5000/", sharedPreference.getDefaultIfNull(BineSharedPreference.KEY_HUB_IP))

            return Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(DeviceLocalService::class.java)
        }
    }
}