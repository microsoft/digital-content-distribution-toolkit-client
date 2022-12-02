// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

@file:Suppress("ObsoleteExperimentalCoroutines")

package com.msr.bine_sdk.auth

import com.msr.bine_sdk.BuildConfig
import com.msr.bine_sdk.auth.models.LoginResponse
import com.msr.bine_sdk.cloud.models.Token
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.network.Error

interface AuthService {
    @POST(value = "api/v1/auth/login")
    @FormUrlEncoded
    suspend fun validatePhone(@Field("phone") phone:String,
                              @Field("api_key") apiKey: String): NetworkResponse<LoginResponse, Error>

    @POST(value = "api/v1/validate")
    @FormUrlEncoded
    suspend fun validateOTP(@Field("phone") phone:String,
                            @Field("otp") otp:String,
                            @Field("api_key") apiKey: String): NetworkResponse<LoginResponse, Error>

    @POST(value = "api/v1/auth/logout")
    @FormUrlEncoded
    suspend fun logout(@Field("token") token:String,
                            @Field("api_key") apiKey: String): NetworkResponse<String, Error>

    @POST(value = "api/v1/hub_token_refresh/")
    @FormUrlEncoded
    suspend fun refreshHubToken(@Field("refresh") token:String): NetworkResponse<Token, Error>

    companion object {
        public const val BASE_URL = BuildConfig.AUTH_BASE_URL

        fun create(): AuthService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()

            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(AuthService::class.java)
        }
    }
}
