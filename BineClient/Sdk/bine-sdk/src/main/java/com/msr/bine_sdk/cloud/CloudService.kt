// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import android.util.Log
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.cloud.models.*
import com.msr.bine_sdk.cloud.models.Token
import com.msr.bine_sdk.network.Error
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface CloudService {

    @GET(value = "BrowseContent/contentproviders")
    suspend fun getContentProviders(): NetworkResponse<List<ContentProvider>, Error>

    @POST(value = "browsecontent/{contentProviderId}/broadcasted")
    suspend fun getContent(@Path("contentProviderId") contentProviderId: String, @Body pageRequest: PageRequest): NetworkResponse<ContentListResponse, Error>

    @GET(value = "hubs_rad?format=json&")
    suspend fun getNearestHubs(@Query("dist") radius:Double,
                               @Query("lat") latitude:Double,
                               @Query("lng") longitude:Double): NetworkResponse<HubListResponse, Error>

    @GET(value = "Content/{contentID}/token")
    fun getAssetToken(@Path("contentID") contentID: String): Call<String>

    @POST(value = "asset_token_refresh")
    suspend fun refreshAssetToken(): NetworkResponse<Token, Error>

    @GET(value = "BrowseSubscription/{contentProviderId}")
    suspend fun getSubscriptions(@Path("contentProviderId") contentProviderId: String): NetworkResponse<List<SubscriptionPack>, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "cmsapi/api/v1/"

        fun create(sharedPreference: BineSharedPreference): CloudService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(logger)
                    .authenticator(authenticator)
                    .addInterceptor { chain ->

                        val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                        Log.d("CloudService", "${Constants.AUTH_HEADER} - Bearer $token")
                        val newRequest = chain.request().newBuilder()
                                .addHeader(Constants.AUTH_HEADER, "Bearer $token" ?: "")
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
                    .create(CloudService::class.java)
        }
    }
}