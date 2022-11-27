package com.microsoft.mobile.auth

import com.microsoft.mobile.auth.dtos.*

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface AuthService {
    @POST("api/Authentication/LoginWithPhoneForPartners")
    suspend fun loginWithPhoneForPartner(@HeaderMap header: HashMap<String, String>, @Body request: VerifyPhoneRequest): Response<VerifyPhoneResponse>

    @POST("api/Authentication/VerifyPhonePinForPartnerLogin")
    suspend fun verifyPinForPartner(@HeaderMap header: HashMap<String, String>, @Body request: VerifyPinRequest): Response<VerifyPinResponse>

    @GET("v1/RefreshPartnerAccessToken")
    suspend fun refreshToken(@HeaderMap header: HashMap<String, String>): Response<AccessToken>

    @POST("cp/notifications/register")
    suspend fun registerFCMToken(@HeaderMap header: HashMap<String, String>, @Body request: RegisterFCMRequest): Response<Any>

    companion object {
        const val APP_NAME = "com.microsoft.mobile.polymer.mishtu"
        fun create(url: String): AuthService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val client = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(AuthService::class.java)
        }
    }
}