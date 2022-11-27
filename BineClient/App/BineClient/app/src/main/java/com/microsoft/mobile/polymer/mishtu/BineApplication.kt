package com.microsoft.mobile.polymer.mishtu

import android.app.Application
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.ClientLogging
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.BineEventBus
import com.msr.bine_sdk.BineAPI
import dagger.hilt.android.HiltAndroidApp
import java.lang.Exception
import java.util.*


@HiltAndroidApp
class BineApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        BootTelemetryLogger.getInstance().start(BootTelemetryLogger.BootType.COLD)

        if (!BuildConfig.DEBUG) {
            AppCenter.start(
                this,
                BuildConfig.APP_CENTER_SECRET,
                Crashes::class.java,
                Analytics::class.java/*,
                Distribute::class.java*/
            )
            Analytics.setEnabled(BuildConfig.ENABLE_ANALYTICS)
        }
        AnalyticsLogger.getInstance().start(this)

        BineAPI.init(this,BNConstants.bineSDKEnvironment())
        BineAPI.getInstance().saveDownloadThreadCount(3)
        SharedPreferenceStore.init(this)
        ClientLogging.init(this)
        if(SharedPreferenceStore.getInstance().get(SharedPreferenceStore.DOWNLOAD_COUNT_START_DATE).isNullOrEmpty()){
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.DOWNLOAD_COUNT_START_DATE, Date().time.toString())
        }
        ClientLogging.clearClientLogFileIfTimeExceeded()
        BineEventBus(this).register()
    }

}