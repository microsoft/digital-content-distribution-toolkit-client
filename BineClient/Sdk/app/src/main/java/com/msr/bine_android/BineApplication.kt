// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.msr.bine_android.data.AppDatabase.Companion.getDatabase
import com.msr.bine_android.data.DataRepository.Companion.getInstance
import com.msr.bine_android.data.SharedPreferenceStore
import com.msr.bine_android.utils.Constants
import com.msr.bine_android.telemetry.Telemetry
import com.msr.bine_sdk.BineAPI
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BineApplication : Application(), Configuration.Provider  {

//    val appComponent: BineAPIComponent by lazy {
//        DaggerBineAPIComponent.builder()
//                .appModule(AppModule(this))
//                .build()
//    }


    init {
        instance = this
    }

    companion object {
        private var instance: BineApplication? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        BineAPI.init(applicationContext, BineAPI.Environment.STAGE)

        val appLifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        if (!AppCenter.isConfigured()) {
            AppCenter.start(this, BuildConfig.APP_CENTER_SECRET, Crashes::class.java, Analytics::class.java)
        }

        val dataRepository = getInstance(
                getDatabase(this).folderDao(),
                getDatabase(this).contentDao(),
                getDatabase(this).cartDao(),
                getDatabase(this).folderEntityDao(),
                SharedPreferenceStore(this))

        Analytics.setEnabled(BuildConfig.ENABLE_ANALYTICS)

        Telemetry.getInstance().init(this,
                dataRepository)

        if (!BuildConfig.DEBUG) {
            dataRepository.changeScreenRecordingSettings(Constants.SCREEN_RECORDING, "enable")
        }

        BineEventBus(this,dataRepository).register()
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
            Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
}