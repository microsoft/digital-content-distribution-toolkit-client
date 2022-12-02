// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import android.util.Log
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.cloud.models.RetailerDistance
import com.msr.bine_sdk.network.Error
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetailerService {
    @GET(value = "BrowseRetailer/nearby")
    suspend fun getNearbyRetailers(@Query("distanceMeters") radius:Double,
                                   @Query("lat") latitude:Double,
                                   @Query("lng") longitude:Double): NetworkResponse<List<RetailerDistance>, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "retailerapi/api/v1/"

        fun create(sharedPreference: BineSharedPreference): RetailerService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(logger)
                    .authenticator(authenticator)
                    .addInterceptor { chain ->

                        val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                        Log.d("RMSService", "${Constants.AUTH_HEADER} - Bearer $token")
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
                    .create(RetailerService::class.java)
        }
    }
}