// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk

import android.content.Context
import com.msr.bine_sdk.auth.AuthManager
import com.msr.bine_sdk.cloud.models.InitResponse
import com.msr.bine_sdk.auth.models.LoginRequest
import com.msr.bine_sdk.auth.models.LoginResponse
import com.msr.bine_sdk.cloud.*
import com.msr.bine_sdk.di.BineModule
import com.msr.bine_sdk.di.DaggerBineComponent
import com.msr.bine_sdk.cloud.DeviceManager
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.cloud.models.Source
import com.msr.bine_sdk.secure.BineSharedPreference
import com.msr.bine_sdk.secure.KeystoreManager

import javax.inject.Inject

open class BineAPI @Inject constructor() {

    enum class Environment {
        DEV,
        STAGE,
        PROD
    }

    @Inject
    protected lateinit var authManager: AuthManager
    @Inject
    protected lateinit var cloudManager: CloudManager
    @Inject
    protected lateinit var deviceManager: DeviceManager
    @Inject
    protected lateinit var orderManager: OrderManager
    @Inject
    protected lateinit var retailerManager: RetailerManager
    @Inject
    protected lateinit var userManager: UserManager
    @Inject
    protected lateinit var incentivesManager: IncentivesManager

    @Inject
    protected lateinit var sharedPreference: BineSharedPreference

    companion object {
        private var instance: BineAPI = BineAPI()
        private var environment: Environment = Environment.DEV
        val baseUrl: String
            get() {
                return when(environment) {
                    Environment.DEV -> "https://blendnet-dev.kaiza.la/"
                    Environment.STAGE -> "https://blendnet-stage.kaiza.la/"
                    Environment.PROD -> "https://meramishtu.com/"
                }
            }

        @Synchronized
        fun getInstance(): BineAPI {
            return instance
        }

        fun init(context: Context, env: Environment) {
            environment = env
            val bineComponent = DaggerBineComponent.builder().bineModule(BineModule(context)).build()
            bineComponent.inject(instance)
            KeystoreManager.init(context)
        }

        fun CMS(): CloudManager {
            return instance.cloudManager
        }

        fun OMS(): OrderManager {
            return instance.orderManager
        }

        fun RMS(): RetailerManager {
            return instance.retailerManager
        }
        fun UMS(): UserManager {
            return instance.userManager
        }
        fun Incentives(): IncentivesManager {
            return instance.incentivesManager
        }
        fun Devices(): DeviceManager {
            return instance.deviceManager
        }
    }

    suspend fun verifyPhone(request: LoginRequest): LoginResponse {
        return authManager.validatePhone(request)
    }

    suspend fun verifyOTP(request: LoginRequest): LoginResponse {
        return authManager.validateOTP(request)
    }

    suspend fun logout(): LoginResponse {
        return authManager.logout()
    }

    suspend fun refreshHubToken(): APIResponse.TokenResponse {
        return authManager.refreshHubToken()
    }

    fun resetUser() {
        //Save the token Shared Prefs
        sharedPreference.reset()
    }

    suspend fun init(token: String, createUser: Boolean, userName: String): InitResponse {
        //Save the token Shared Prefs
        sharedPreference.saveSecure(BineSharedPreference.KEY_TOKEN_CLOUD, token)

        if (!createUser)
            return InitResponse(null, null, null)

        //Make API call to BN Cloud for user creation if new user
        val response  = userManager.createUser(userName)
        response.result?.let {
            return InitResponse(it, null, null)
        } ?:
        return InitResponse(null, response.details, response.error)
    }

    open fun getToken(source: Source): String? {
        return if (source == Source.HUB) sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_HUB)
        else sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD)
    }

    //HUB
    /*suspend fun getHubContent(mediaHouse: String, parent: String): APIResponse.HubContentResponse {
        return hubManager.getContent(getHubIP(), mediaHouse, parent)
    }*/
    /*suspend fun getHubContent(mediaHouse: String, parent: String): APIResponse.HubContentResponse {
        return hubManager.getContent(getHubIP(), mediaHouse, parent)
    }*/

    fun saveHubIP(hubIP: String) {
        sharedPreference.save(BineSharedPreference.KEY_HUB_IP, hubIP)
    }

    fun getHubIP(): String {
        return sharedPreference.getDefaultIfNull(BineSharedPreference.KEY_HUB_IP)
    }

    fun saveDownloadThreadCount(count: Int) {
        sharedPreference.save(BineSharedPreference.KEY_PARALLEL_COUNT, count.toString())
    }

    fun getDownloadThreadCount(): Int {
        val countStr = sharedPreference.getDefaultIfNull(BineSharedPreference.KEY_PARALLEL_COUNT)
        return if (countStr.isNullOrEmpty()) 1
        else countStr.toInt()
    }
}