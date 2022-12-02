// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import android.util.Log
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
import retrofit2.http.*

interface UserService {
    @POST(value = "UserOnboarding/user")
    suspend fun createUser(@Body request: CreateUserRequest): NetworkResponse<String, Error>

    @POST(value = "UserBasic/assignRetailer/{referralCode}")
    suspend fun assignRetailer(@Path("referralCode") referralCode: String): NetworkResponse<String, Error>

    @GET(value = "UserBasic/me")
    suspend fun userDetail(): NetworkResponse<User, Error>

    @PUT(value = "UserBasic/profile")
    suspend fun updateUser(@Body request: UpdateUserRequest): NetworkResponse<String, Error>

    //Deprecated
    @POST(value = "User/dataExport")
    suspend fun dataExport(@Body request: DataExportRequest): NetworkResponse<DataExportResponse, Error>

    @POST(value = "UserBasic/dataExport/create")
    suspend fun requestDataExport(@Body request: DataExportRequest): NetworkResponse<String, Error>

    @HTTP(method = "DELETE", path = "UserBasic/user", hasBody = true)
    suspend fun deleteUser(@Body request: DataExportRequest): NetworkResponse<String, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "userapi/api/v1/"

        fun create(sharedPreference: BineSharedPreference): UserService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(logger)
                    .authenticator(authenticator)
                    .addInterceptor { chain ->

                        val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                        Log.d("UMSService", "${Constants.AUTH_HEADER} - Bearer $token")
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
                    .create(UserService::class.java)
        }
    }
}