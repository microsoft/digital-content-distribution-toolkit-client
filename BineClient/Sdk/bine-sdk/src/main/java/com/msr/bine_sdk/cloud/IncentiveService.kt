package com.msr.bine_sdk.cloud

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IncentiveService {
    @GET(value = "IncentiveBrowse/consumer/active/REGULAR")
    suspend fun getPlan(): NetworkResponse<IncentivePlan, Error>

    @GET(value = "IncentiveEventBrowse/consumer/regular")
    suspend fun getEvents(): NetworkResponse<IncentiveEvent, Error>

    @POST(value = "UserIncentiveEvents/{event}")
    suspend fun recordEvent(@Path("event") eventType: String, @Body event: RecordEvent): NetworkResponse<Void, Error>

    companion object {
        private val BASE_URL = BineAPI.baseUrl + "incentiveapi/api/v1/"

        fun create(sharedPreference: BineSharedPreference): IncentiveService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authenticator = TokenAuthenticator(sharedPreference)

            val client = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(logger)
                .authenticator(authenticator)
                .addInterceptor { chain ->
                    val token =  sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
                    Log.d("IncentiveService", "${Constants.AUTH_HEADER} - Bearer $token")
                    val newRequest = chain.request().newBuilder()
                        .addHeader(Constants.AUTH_HEADER, "Bearer $token")
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
                .create(IncentiveService::class.java)
        }
    }
}