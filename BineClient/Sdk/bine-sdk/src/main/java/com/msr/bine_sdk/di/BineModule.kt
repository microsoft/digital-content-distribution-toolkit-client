// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.di

import android.content.Context
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.auth.AuthManager
import com.msr.bine_sdk.auth.AuthService
import com.msr.bine_sdk.cloud.*
import com.msr.bine_sdk.download.custom.CustomDownloader
import com.msr.bine_sdk.download.exo.ExoDownloader
import com.msr.bine_sdk.download.exo.DRMManager
import com.msr.bine_sdk.cloud.DeviceManager
import com.msr.bine_sdk.cloud.DeviceLocalService
import com.msr.bine_sdk.secure.BineSharedPreference
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class BineModule(private val context: Context) {

    @Provides
    @Singleton
    fun giveContext(): Context = this.context

    @Provides
    @Singleton
    open fun provideBineSharedPreference(context: Context): BineSharedPreference {
        return BineSharedPreference(context)
    }

    @Provides
    open fun provideAuthService(): AuthService {
        return AuthService.create()
    }

    @Provides
    open fun provideCloudService(sharedPreference: BineSharedPreference): CloudService {
        return CloudService.create(sharedPreference)
    }

    @Provides
    open fun provideDeviceLocalService(sharedPreference: BineSharedPreference): DeviceLocalService {
        return DeviceLocalService.create(sharedPreference)
    }

    @Provides
    open fun provideDeviceCloudService(sharedPreference: BineSharedPreference): DeviceCloudService {
        return DeviceCloudService.create(sharedPreference)
    }

    @Provides
    open fun provideOMSService(sharedPreference: BineSharedPreference): OMService {
        return OMService.create(sharedPreference)
    }

    @Provides
    open fun provideUMService(sharedPreference: BineSharedPreference): UserService {
        return UserService.create(sharedPreference)
    }

    @Provides
    open fun provideRMService(sharedPreference: BineSharedPreference): RetailerService {
        return RetailerService.create(sharedPreference)
    }

    @Provides
    open fun provideIncentivesService(sharedPreference: BineSharedPreference): IncentiveService {
        return IncentiveService.create(sharedPreference)
    }

    @Provides
    @Singleton
    open fun provideAuthManager(service: AuthService, sharedPreference: BineSharedPreference): AuthManager {
        return AuthManager(service, sharedPreference)
    }

    @Provides
    @Singleton
    open fun provideCloudManager(service: CloudService,
                                 sharedPreference: BineSharedPreference): CloudManager {
        return CloudManager(service, sharedPreference)
    }

    @Provides
    @Singleton
    open fun provideDeviceManager(service: DeviceLocalService,
                                  cloudService: DeviceCloudService): DeviceManager {
        return DeviceManager(service, cloudService)
    }

    @Provides
    @Singleton
    open fun provideOrderManager(service: OMService): OrderManager {
        return OrderManager(service)
    }

    @Provides
    @Singleton
    open fun provideRetailerManager(service: RetailerService): RetailerManager {
        return RetailerManager(service)
    }

    @Provides
    @Singleton
    open fun provideUserManager(service: UserService): UserManager {
        return UserManager(service)
    }

    @Provides
    @Singleton
    open fun provideIncentivesManager(service: IncentiveService): IncentivesManager {
        return IncentivesManager(service)
    }

    @Provides
    @Singleton
    open fun provideDRMManager(context: Context): DRMManager {
        return DRMManager.getInstance(context)
    }

    @Provides
    @Singleton
    open fun provideExoDownloader(context: Context,
                                  authManager: AuthManager,
                                  sharedPreference: BineSharedPreference,
                                  drmManager: DRMManager): ExoDownloader {
        return ExoDownloader(context, authManager, sharedPreference, drmManager)
    }

    @Provides
    @Singleton
    open fun provideCustomDownloader(context: Context): CustomDownloader {
        return CustomDownloader(context)
    }

    @Provides
    @Singleton
    open fun provideBineApi():BineAPI{
        return BineAPI()
    }
}