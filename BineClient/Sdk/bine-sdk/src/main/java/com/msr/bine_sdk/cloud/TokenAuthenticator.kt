// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud

import com.msr.bine_sdk.auth.AuthLogoutException
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.eventbus.events.LogoutEvent
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.*
import org.greenrobot.eventbus.EventBus

class TokenAuthenticator constructor(private val sharedPreference: BineSharedPreference): Authenticator {
    companion object {
        private val TAG = TokenAuthenticator::class.java.simpleName
    }

    private var tryCount = 0

    override fun authenticate(route: Route?, response: Response): Request? {
        //tryCount++
        EventBus.getDefault().post(LogEvent("TokenAuthenticator", "Token Failure Occurred", null))

        /*val endpoint: String = Constants.CLOUD_REFRESH_TOKEN

        val requestBuilder: Request.Builder = Request.Builder()
                .url(BuildConfig.AUTH_BASE_URL + endpoint)

        val refresh = sharedPreference.getSecure(BineSharedPreference.KEY_RTOKEN_CLOUD)

        val requestBody = FormBody.Builder()
        requestBody.add("refresh", refresh!!)
        requestBuilder.method("POST", requestBody.build())

        val client = OkHttpClient()
        val refreshResponse = client.newCall(requestBuilder.build()).execute()
        if (refreshResponse.isSuccessful && tryCount < 3) {

            EventBus.getDefault().post(LogEvent("TokenAuthenticator", "Token Refreshed", null))
            val loginResponse: RefreshTokenResponse? = Gson().fromJson<RefreshTokenResponse>(refreshResponse.body!!.string(),
                    RefreshTokenResponse::class.java)
            if (loginResponse != null) {
                val key: String = BineSharedPreference.KEY_TOKEN_CLOUD
                sharedPreference.saveSecure(
                        key,
                        loginResponse.access)

               return response.request.newBuilder()
                        .removeHeader(AUTH_HEADER)
                        .addHeader(AUTH_HEADER, "Bearer " + loginResponse.access)
                        .build()
            }
        } else {*/
            //EventBus.getDefault().post(LogEvent("TokenAuthenticator", "3 attempts failed", null))
            //Log.d(TAG, "Token Refresh Failed - $refreshResponse")
            //clear tokens
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_CLOUD, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_CLOUD, "")
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_HUB, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_HUB, "")
            sharedPreference.save(BineSharedPreference.KEY_TOKEN_ASSET, "")
            sharedPreference.save(BineSharedPreference.KEY_RTOKEN_ASSET, "")
            EventBus.getDefault().post(LogoutEvent(true))
        //}
        throw AuthLogoutException("Refresh Token Failed")
    }
}