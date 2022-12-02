// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk

import android.content.SharedPreferences
import com.msr.bine_sdk.auth.AuthManager
import com.msr.bine_sdk.auth.models.LoginRequest
import com.msr.bine_sdk.auth.models.LoginResponse
import com.msr.bine_sdk.base.BaseTest
import com.msr.bine_sdk.cloud.models.Token
import com.msr.bine_sdk.network.NetworkResponse
import com.msr.bine_sdk.secure.BineSharedPreference
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.io.IOException


@RunWith(RobolectricTestRunner::class)
class AuthManagerTest : BaseTest() {

    private lateinit var authManager: AuthManager

    private lateinit var bineAPI: BineAPI

    @Mock
    var preferences: SharedPreferences? = null

    @Mock
    var editor: SharedPreferences.Editor? = null

    @Before
    fun setUpTest() {
        super.setUp()
        //Mock the AuthService API
        val loginResponse = LoginResponse()
        val token = Token(Constants.TEST_HUB_NAME, Constants.TEST_HUB_NAME)
        loginResponse.app_token = token
        loginResponse.hub_token = token
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.Success(loginResponse, Headers.headersOf())
        runBlocking {
            whenever(authService.validatePhone(Constants.TEST_MOBILE, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }
        authManager = AuthManager(authService, sharedPreferences)
    }

    @Test
    @Throws(Exception::class)
    fun checkPrefManger() {
        `when`(preferences?.getString(anyString(), anyString())).thenReturn("EMAIL")
        assert(sharedPreferences.get(BineSharedPreference.KEY_TOKEN_HUB)==null)
    }

    @After
    fun setTearDown() = super.tearDown()


    fun setLoginSuccess() {
        val networkResponse = NetworkResponse.UnknownError(null, 500)
        runBlocking {
            whenever(authService.validatePhone(Constants.TEST_MOBILE, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }

        authManager = AuthManager(authService, sharedPreferences)
    }

    fun setLoginError() {
        val networkResponse = NetworkResponse.UnknownError(null, 500)
        runBlocking {
            whenever(authService.validatePhone(Constants.TEST_MOBILE, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }

        authManager = AuthManager(authService, sharedPreferences)
    }

   /* fun setRefreshToken() {
        Mockito.`when`(mockContext?.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)).thenReturn(mockPrefs)
        Mockito.`when`(mockPrefs!!.getString("YOUR_KEY", null)).thenReturn("YOUR_VALUE")

    }*/

    fun setLoginNetworkError() {
        val networkResponse = NetworkResponse.NetworkError(IOException())
        runBlocking {
            whenever(authService.validatePhone(Constants.TEST_MOBILE, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }

        authManager = AuthManager(authService, sharedPreferences)
    }

    fun setLoginApiError() {
        val loginResponse = LoginResponse()
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "invalid phone", 2), 404)
        runBlocking {
            whenever(authService.validatePhone(Constants.TEST_MOBILE, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }
        authManager = AuthManager(authService, sharedPreferences)
    }


    fun setLogout() {
        val loginResponse = LoginResponse()
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.Success("Logout", Headers.headersOf())
        runBlocking {
            whenever(authService.logout(Constants.TEST_TOKEN, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }

        authManager = AuthManager(authService, sharedPreferences)
    }


    fun setUpValidateOtp() {
        val loginResponse = LoginResponse()
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.Success(loginResponse, Headers.headersOf())
        runBlocking {
            whenever(authService.validateOTP(Constants.TEST_MOBILE, Constants.TEST_OTP, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }
    }

    fun setValidateOtpError() {
        val networkResponse = NetworkResponse.UnknownError(null, 500)
        runBlocking {
            whenever(authService.validateOTP(Constants.TEST_MOBILE, Constants.TEST_OTP, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }

        authManager = AuthManager(authService, sharedPreferences)
    }

    fun setValidateOtpApiError() {
        val loginResponse = LoginResponse()
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "invalid OTP", 2), 404)
        runBlocking {
            whenever(authService.validateOTP(Constants.TEST_MOBILE, Constants.TEST_OTP, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }
        authManager = AuthManager(authService, sharedPreferences)
    }

    fun setValidateOtpNetworkError() {
        val loginResponse = LoginResponse()
        loginResponse.statusCode = 200
        val networkResponse = NetworkResponse.NetworkError(IOException())
        runBlocking {
            whenever(authService.validateOTP(Constants.TEST_MOBILE, Constants.TEST_OTP, BuildConfig.API_KEY))
                    .thenReturn(networkResponse)
        }
        authManager = AuthManager(authService, sharedPreferences)
    }


    @Test
    fun `test login success`() =
            runBlocking {
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, "")
                val login = authManager.validatePhone(loginRequest)
                assertNotNull(login)
            }


   /* @Test
    fun `test token success`() =
            runBlocking {
                //setRefreshToken()
                assertNotNull(sharedPreferences.get("YOUR_KEY"))
            }*/

    @Test
    fun `test login failure`() =
            runBlocking {
                setLoginError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, "")
                val response = authManager.validatePhone(loginRequest)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test login Network failure`() =
            runBlocking {
                setLoginNetworkError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, "")
                val response = authManager.validatePhone(loginRequest)
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test login Api failure`() =
            runBlocking {
                setLoginApiError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, "")
                val response = authManager.validatePhone(loginRequest)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test validate OTP success`() =
            runBlocking {
                setUpValidateOtp()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, Constants.TEST_OTP)
                val response = authManager.validateOTP(loginRequest)
                assertNotNull(response)
                assert(response.statusCode == 200)
            }

    @Test
    fun `test validate otp validation failure`() =
            runBlocking {
                setValidateOtpError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, Constants.TEST_OTP)
                val response = authManager.validateOTP(loginRequest)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test login otp validation Api failure`() =
            runBlocking {
                setValidateOtpApiError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, Constants.TEST_OTP)
                val response = authManager.validateOTP(loginRequest)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test login otp validation Network Api failure`() =
            runBlocking {
                setValidateOtpNetworkError()
                val loginRequest = LoginRequest(Constants.TEST_MOBILE, Constants.TEST_OTP)
                val response = authManager.validateOTP(loginRequest)
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test logout success`() =
            runBlocking {
                setLogout()
                val response = authManager.logout()
                assert(response.status == true)
            }


}


