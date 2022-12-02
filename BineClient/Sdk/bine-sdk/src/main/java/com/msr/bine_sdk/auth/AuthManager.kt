package com.msr.bine_sdk.auth

import android.util.Log
import com.msr.bine_sdk.BuildConfig
import com.msr.bine_sdk.auth.models.LoginRequest
import com.msr.bine_sdk.auth.models.LoginResponse
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.secure.BineSharedPreference
import javax.inject.Inject
import com.msr.bine_sdk.models.Error

class AuthManager @Inject constructor(private val service: AuthService,
                                      private val sharedPreference: BineSharedPreference) {
    companion object {
        private val TAG = AuthManager::class.java.simpleName
    }

    suspend fun validatePhone(request: LoginRequest): LoginResponse {
        when (val response = service.validatePhone(request.phoneNumber, BuildConfig.API_KEY)) {
            is NetworkResponse.Success -> {
                //Save tokens
                saveTokens(response.body)
                return response.body
            }
            is NetworkResponse.ApiError -> {
                return LoginResponse(false, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                return LoginResponse(false, "", Error.NETWORK_ERROR)
            }
            else -> {
                return LoginResponse(false, "", Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun validateOTP(request: LoginRequest): LoginResponse {
        val response = service.validateOTP(request.phoneNumber,
                request.otp,
                BuildConfig.API_KEY)
        when (response) {
            is NetworkResponse.Success -> {
                //Save tokens
                saveTokens(response.body)
                return response.body
            }
            is NetworkResponse.ApiError -> {
                return LoginResponse(false, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                return LoginResponse(false, "", Error.NETWORK_ERROR)
            }
            else -> {
                return LoginResponse(false, "", Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun logout(): LoginResponse {
        val token = sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
        if (token == null || token == "") {
            //Token empty, proceed logout
            clearTokens()
            return LoginResponse(true)
        }

        val response = service.logout(token,
                BuildConfig.API_KEY)
        when (response) {
            is NetworkResponse.Success -> {
                //Save tokens
                clearTokens()
                return LoginResponse(true)
            }
            is NetworkResponse.ApiError -> {
                return LoginResponse(false, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                return LoginResponse(false, "", Error.NETWORK_ERROR)
            }
            else -> {
                return LoginResponse(false, "", Error.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun refreshHubToken(): APIResponse.TokenResponse {
        val token = sharedPreference.getSecure(BineSharedPreference.KEY_RTOKEN_HUB)
        if (token == null || token == "") {
            return APIResponse.TokenResponse(null, null, Error.UNKNOWN_ERROR)
        }

        return when (val response = service.refreshHubToken(token)) {
            is NetworkResponse.Success -> {
                response.body.access.let { sharedPreference.save(it, BineSharedPreference.KEY_TOKEN_HUB) }
                response.body.refresh.let { sharedPreference.save(it, BineSharedPreference.KEY_RTOKEN_HUB) }
                APIResponse.TokenResponse(response.body.access, null, null)
            }
            is NetworkResponse.ApiError -> {
                APIResponse.TokenResponse(null, response.body.details, Error.UNKNOWN_ERROR)
            }
            is NetworkResponse.NetworkError -> {
                APIResponse.TokenResponse(null, null, Error.NETWORK_ERROR)
            }
            else -> {
                APIResponse.TokenResponse(null, null, Error.UNKNOWN_ERROR)
            }
        }
    }

    private fun saveTokens(loginResponse: LoginResponse) {
        loginResponse.app_token?.let {  it->
            sharedPreference.saveSecure(BineSharedPreference.KEY_TOKEN_CLOUD,
                    it.access)
            sharedPreference.saveSecure(BineSharedPreference.KEY_RTOKEN_CLOUD,
                    it.refresh)
            Log.d(TAG, loginResponse.app_token.toString())
        }
        loginResponse.hub_token?.let {it->
            sharedPreference.saveSecure(BineSharedPreference.KEY_TOKEN_HUB,
                    it.access)
            sharedPreference.saveSecure(BineSharedPreference.KEY_RTOKEN_HUB,
                    it.refresh)
            Log.d(TAG, loginResponse.hub_token.toString())
        }
    }

    private fun clearTokens() {
        sharedPreference.save(BineSharedPreference.KEY_TOKEN_CLOUD, "")
        sharedPreference.save(BineSharedPreference.KEY_RTOKEN_CLOUD, "")
        sharedPreference.save(BineSharedPreference.KEY_TOKEN_HUB, "")
        sharedPreference.save(BineSharedPreference.KEY_RTOKEN_HUB, "")
        sharedPreference.save(BineSharedPreference.KEY_TOKEN_ASSET, "")
        sharedPreference.save(BineSharedPreference.KEY_RTOKEN_ASSET, "")
    }
}