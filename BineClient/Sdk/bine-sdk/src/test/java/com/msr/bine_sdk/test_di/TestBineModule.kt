// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.test_di

import android.content.Context
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.auth.AuthManager
import com.msr.bine_sdk.auth.AuthService
import com.msr.bine_sdk.cloud.CloudManager
import com.msr.bine_sdk.cloud.CloudService
import com.msr.bine_sdk.di.BineModule
import com.msr.bine_sdk.cloud.DeviceManager
import com.msr.bine_sdk.cloud.DeviceLocalService
import com.msr.bine_sdk.secure.BineSharedPreference
import com.nhaarman.mockitokotlin2.mock
import io.mockk.mockk

class TestBineModule(context: Context): BineModule(context) {

    override fun provideBineSharedPreference(context: Context): BineSharedPreference = mock()

    override fun provideAuthService(): AuthService = mock()

    override fun provideCloudService(sharedPreference: BineSharedPreference): CloudService = mock()

    override fun provideHubService(sharedPreference: BineSharedPreference): DeviceLocalService = mockk()

    override fun provideHubManageService(sharedPreference: BineSharedPreference): HubManagementService = mock()

    override fun provideAuthManager(service: AuthService, sharedPreference: BineSharedPreference): AuthManager {
        return AuthManager(service,sharedPreference)
    }

    override fun provideCloudManager(service: CloudService,
                                     hubManagementService: HubManagementService,
                                     sharedPreference: BineSharedPreference): CloudManager = mockk()

    override fun provideHubManager(service: DeviceLocalService): DeviceManager = mockk()

    override fun provideBineApi(): BineAPI = mockk()
}