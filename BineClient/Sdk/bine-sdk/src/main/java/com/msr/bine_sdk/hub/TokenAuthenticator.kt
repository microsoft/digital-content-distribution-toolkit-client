// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.hub

import android.util.Log
import com.google.gson.Gson
import com.msr.bine_sdk.BuildConfig
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.Constants.AUTH_HEADER
import com.msr.bine_sdk.eventbus.events.AnalyticsEvent
import com.msr.bine_sdk.auth.AuthLogoutException
import com.msr.bine_sdk.eventbus.events.LogoutEvent
import com.msr.bine_sdk.cloud.models.RefreshTokenResponse
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.*
import org.greenrobot.eventbus.EventBus

class TokenAuthenticator constructor(private val sharedPreference: BineSharedPreference): Authenticator {
    companion object {
        private val TAG = TokenAuthenticator::class.java.simpleName
    }

    private var tryCount = 0

    override fun authenticate(route: Route?, response: Response): Request? {
        tryCount++

        val endpoint: String = Constants.HUB_REFRESH_TOKEN

        val requestBuilder: Request.Builder = Request.Builder()
                .url(BuildConfig.AUTH_BASE_URL + endpoint)

        val refresh = sharedPreference.getSecure(BineSharedPreference.KEY_RTOKEN_HUB)

        val requestBody = FormBody.Builder()
        requestBody.add("refresh", refresh!!)
        requestBuilder.method("POST", requestBody.build())

        val client = OkHttpClient()
        val refreshResponse = client.newCall(requestBuilder.build()).execute()
        if (refreshResponse.isSuccessful && tryCount < 3) {

            EventBus.getDefault().post(AnalyticsEvent("TokenExpired", null))

            val loginResponse: RefreshTokenResponse? = Gson().fromJson<RefreshTokenResponse>(refreshResponse.body!!.string(),
                    RefreshTokenResponse::class.java)
            if (loginResponse != null) {
                val key: String = BineSharedPreference.KEY_TOKEN_HUB
                sharedPreference.saveSecure(
                        key,
                        loginResponse.access)

               return response.request.newBuilder()
                        .removeHeader(AUTH_HEADER)
                        .addHeader(AUTH_HEADER, "Bearer " + loginResponse.access)
                        .build()
            }
        } else {
            Log.d(TAG, "Token Refresh Failed - $refreshResponse")
            //clear tokens
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_CLOUD, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_CLOUD, "")
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_HUB, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_HUB, "")
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_ASSET, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_ASSET, "")

            EventBus.getDefault().post(LogoutEvent(true))
        }
        throw AuthLogoutException("Refresh Token Failed")
    }
}