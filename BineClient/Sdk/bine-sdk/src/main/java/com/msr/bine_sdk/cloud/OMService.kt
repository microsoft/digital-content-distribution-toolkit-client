// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.cloud.models.CreateOrderRequest
import com.msr.bine_sdk.cloud.models.Order
import com.msr.bine_sdk.cloud.models.OrderListRequest
import com.msr.bine_sdk.network.Error
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.network.NetworkResponseAdapterFactory
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface OMService {
    @POST(value = "OrderBasic/createorder")
    suspend fun createOrder(@Body createOrderRequest: CreateOrderRequest): NetworkResponse<String, Error>

    @GET(value = "OrderBasic/{orderId}")
    suspend fun getOrder(@Path("orderId") orderId: String): NetworkResponse<Order, Error>

    @POST(value = "OrderBasic/orderlist")
    suspend fun getOrders(@Body orderListRequest: OrderListRequest): NetworkResponse<List<Order>, Error>

    @GET(value = "OrderBasic/token/{contentId}")
    suspend fun getAssetToken(@Path("contentId") contentID: String): NetworkResponse<String, Error>

    @PUT(value = "OrderBasic/cancel/{orderId}")
    suspend fun cancelOrder(@Path("orderId") orderId: String): NetworkResponse<String, Error>

    @GET(value = "OrderBasic/active")
    suspend fun getActiveSubscription(): NetworkResponse<List<Order.Subscription>, Error>

    @POST(value = "OrderBasic/redeem")
    suspend fun redeemOffer(@Body createOrderRequest: CreateOrderRequest): NetworkResponse<String, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "omsapi/api/v1/"

        fun create(sharedPreference: BineSharedPreference): OMService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(logger)
                    .authenticator(authenticator)
                    .addInterceptor { chain ->
                        val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                        Log.d("OMSService", "${Constants.AUTH_HEADER} - Bearer $token")
                        val newRequest = chain.request().newBuilder()
                                .addHeader(Constants.AUTH_HEADER, "Bearer $token" ?: "")
                        .build()
                        chain.proceed(newRequest)
                    }
                    .build()


            val gson: Gson = GsonBuilder()
                    .setLenient()
                    .create()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build()
                    .create(OMService::class.java)
        }
    }
}