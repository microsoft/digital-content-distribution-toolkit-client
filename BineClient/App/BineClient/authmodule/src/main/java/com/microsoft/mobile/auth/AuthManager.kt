// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.auth

import com.google.gson.Gson
import com.microsoft.mobile.auth.dtos.*
import okhttp3.ResponseBody

class AuthManager {
    lateinit var service: AuthService

    companion object {
        private val TAG = AuthManager::class.java.simpleName
        private var instance: AuthManager? = null

        @Synchronized
        fun getInstance(): AuthManager = instance
                ?: AuthManager().also {
                    instance = it
                }
    }

    suspend fun validatePhone(phone: String, useVoice: Boolean, deviceId: String): AuthResponse.VerifyPhone {
        service = if (BuildConfig.DEBUG) {
            AuthService.create(getAlphaBaseUrl(phone))
        }
        else {
            AuthService.create(BuildConfig.AUTH_BASE_URL)
        }
        try {
            val response = service.loginWithPhoneForPartner(
                getDefaultHeaders(),
                VerifyPhoneRequest(
                    phone,
                    useVoice,
                    "SixDigit",
                    deviceId
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    return AuthResponse.VerifyPhone(it.accountStatus, it.mode)
                }
            } else {
                val errorBody = getError(response.errorBody())
                if (errorBody != null) {
                    return AuthResponse.VerifyPhone(errorBody.message, AuthError.API_ERROR)
                }
            }
            return AuthResponse.VerifyPhone(null, AuthError.UNKNOWN_ERROR)
        } catch (he: Exception) {
            return AuthResponse.VerifyPhone(null, AuthError.NETWORK_ERROR)
        }
    }

    suspend fun validatePin(phone: String, pin: String, deviceId: String): AuthResponse.VerifyPin {
        service = if (BuildConfig.DEBUG) {
            AuthService.create(getAlphaBaseUrl(phone))
        }
        else {
            AuthService.create(BuildConfig.AUTH_BASE_URL)
        }
        try {
            val response = service.verifyPinForPartner(getDefaultHeaders(),
                VerifyPinRequest(
                    phone,
                    pin,
                    listOf("1.1"),
                    phone,
                    deviceId
                ))

            if (response.isSuccessful) {
                response.body()?.let {
                    return AuthResponse.VerifyPin(it.userId, it.authenticationToken, it.isNewUser)
                }
            } else {
                val errorBody = getError(response.errorBody())
                if (errorBody != null) {
                    return AuthResponse.VerifyPin(errorBody.message, AuthError.API_ERROR)
                }
            }
            return AuthResponse.VerifyPin(null, AuthError.UNKNOWN_ERROR)
        } catch (he: Exception) {
            return AuthResponse.VerifyPin(he.message, AuthError.NETWORK_ERROR)
        }
    }

    suspend fun refreshToken(token: String): AuthResponse.RefreshToken {
        service = AuthService.create(BuildConfig.AUTH_BASE_URL)

        val headers = HashMap<String, String>()
        headers["accessToken"] = token
        return try {
            val response = service.refreshToken(headers)
            if (response.isSuccessful) {
                AuthResponse.RefreshToken(response.body()?.accessToken)
            } else {
                val errorBody = getError(response.errorBody())
                if (errorBody != null) {
                    AuthResponse.RefreshToken(
                        response.code(),
                        errorBody.message,
                        AuthError.API_ERROR
                    )
                } else {
                    AuthResponse.RefreshToken(0, null, AuthError.UNKNOWN_ERROR)
                }
            }
        } catch (he: Exception) {
            AuthResponse.RefreshToken(0, he.message, AuthError.NETWORK_ERROR)
        }
    }

    suspend fun registerFCM(accessToken: String, fcmToken: String, phone: String, topics: List<String>): AuthResponse.FCMToken {
        service = if (BuildConfig.DEBUG) {
            AuthService.create(getAlphaBaseUrl(phone))
        }
        else {
            AuthService.create(BuildConfig.AUTH_BASE_URL)
        }

        val headers = HashMap<String, String>()
        headers["ACCESSTOKEN"] = accessToken
        try {
            val response = service.registerFCMToken(headers, RegisterFCMRequest(fcmToken, "Android", topics))
            if (response.isSuccessful) {
                response.body()?.let { return AuthResponse.FCMToken(true) }
            }
            val errorBody = getError(response.errorBody())
            return if (errorBody != null) {
                AuthResponse.FCMToken(
                    response.code(),
                    errorBody.message,
                    AuthError.API_ERROR
                )
            } else {
                AuthResponse.FCMToken(0, null, AuthError.UNKNOWN_ERROR)
            }

        } catch (e: Exception) { return AuthResponse.FCMToken(0, e.message, AuthError.NETWORK_ERROR)}
    }

    private fun getAlphaBaseUrl(phone: String): String {
        if (phone.endsWith("1") ||
                phone.endsWith("3") ||
                phone.endsWith("5")) {
            return BuildConfig.AUTH_BASE_URL.replace("alpha", "alpha1")
        }
        if (phone.endsWith("2") ||
                phone.endsWith("7")||
                phone.endsWith("8")||
                phone.endsWith("9")) {
            return BuildConfig.AUTH_BASE_URL.replace("alpha", "alpha2")
        }
        return BuildConfig.AUTH_BASE_URL
    }

    private fun getDefaultHeaders(): HashMap<String, String> {
        val headers = HashMap<String, String>()

        headers["AppName"] = AuthService.APP_NAME
        headers["Content-Type"] = "application/json"
        headers["X-ZUMO-APPLICATION"] = BuildConfig.MobileServiceKey
        headers["User-Agent"] = UserAgent.getUserAgent()
        headers["X-ZUMO-VERSION"] = UserAgent.getUserAgent()
        return headers
    }

    private fun getError(error: ResponseBody?): ErrorBody? {
        return when {
            error == null -> null
            error.contentLength() == 0L -> null
            else -> try {
                Gson().fromJson(error.string(), ErrorBody::class.java)
            } catch (ex: Exception) {
                null
            }
        }
    }
}