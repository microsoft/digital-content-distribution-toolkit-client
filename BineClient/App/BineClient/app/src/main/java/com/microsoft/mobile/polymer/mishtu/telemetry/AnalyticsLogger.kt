// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.telemetry

import android.app.Application
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner

import com.microsoft.appcenter.analytics.Analytics

import com.microsoft.mobile.polymer.mishtu.AppLifecycleObserver
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.DeviceUtil
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content

import java.util.*
import kotlin.collections.HashMap

/**
 * App Center Telemetry logger for Mishtu specific events
 * 1. Initializes the SDK
 * 2. Wrapper that provides methods to log events
 */
class AnalyticsLogger {
    companion object {
        private const val LOG_TAG = "ACTelemetryLogger"
        private const val KEY_LAST_ACTIVE_TIME = "LastActiveTimeStamp"
        private const val KEY_PLAY_SESSION_ID = "PlaySessionId"
        private const val KEY_SHORT_PLAY_SESSION_ID = "ShortPlaySessionId"
        private const val MobileServiceUserIdPrefix = "MobileAppsService:"
        private var instance = AnalyticsLogger()
        private var m_interactionSessionId: String? = null
        private lateinit var deviceId: String

        const val LongForm = "LongForm"
        const val ShortForm = "ShortForm"
        const val AppLink = "AppLink"

        @Synchronized
        fun getInstance(): AnalyticsLogger = instance
    }

    fun start(application: Application) {
        val appLifecycleObserver = AppLifecycleObserver(application)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        deviceId = DeviceUtil.getDeviceId(application)
    }

    private fun addDefaultParams(params: HashMap<String, String>): HashMap<String, String> {
        params["AndroidOS"] = Build.VERSION.RELEASE
        params["PhoneModel"] = Build.MODEL
        val userId = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            params["KaizalaUserId"] = sanitizeUserId(userId)
        }
        val clientUserId = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_CLIENT_USER_ID)
        if (!clientUserId.isNullOrEmpty()) {
            params["UserId"] = clientUserId
        }
        params["DeviceId"] = deviceId
        params["Timestamp"] = Date().time.toString()
        params["DeviceLanguage"] = LanguageUtils.getDeviceLanguage()
        m_interactionSessionId?.let {  params["SessionId"] = it }
        return params
    }

    private fun logEvent(eventName: String, keyValuePairs: HashMap<String, String>) {
        if (TextUtils.isEmpty(eventName)) {
            return
        }
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Sending Telemetry - $eventName:$keyValuePairs")
        }
        ClientLogging.getInstance().writeLog(LOG_TAG, "$eventName:$keyValuePairs")
        addDefaultParams(keyValuePairs)
        Analytics.trackEvent(eventName, keyValuePairs)
    }

    fun logEvent(event: Event, keyValuePairs: HashMap<String, String>) {
        logEvent(event.value, keyValuePairs)
    }

    fun logEvent(event: Event) {
        val params = HashMap<String, String>()
        logEvent(event.value, addDefaultParams(params))
    }

    fun logLanguageSelected() {
        val params = HashMap<String, String>()
        SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_LANGUAGE)?.let { params.put("Language", it) }
        logEvent(Event.LANGUAGE_SELECTED, addDefaultParams(params))
    }

    fun logAppStart() {
        val params = HashMap<String, String>()
        logEvent(Event.APP_ACTIVE, addDefaultParams(params))
        SharedPreferenceStore.getInstance().save(KEY_LAST_ACTIVE_TIME, Date().time.toString())
    }

    fun logAppStop() {
        val params = HashMap<String, String>()
        val time = SharedPreferenceStore.getInstance().get(KEY_LAST_ACTIVE_TIME)
        val longTime = time ?: "0"
        val difference: Long = Date().time - longTime.toLong()
        params["TimeSpent"] = difference.toString()
        logEvent(Event.APP_BACKGROUND, addDefaultParams(params))
    }

    fun logOrderCreated(orderId: String, subscriptionId: String, providerId: String) {
        val params = java.util.HashMap<String, String>()
        params["OrderId"] = orderId
        params["SubscriptionId"] = subscriptionId
        params["ProviderId"] = providerId
        logEvent(Event.ORDER_CREATED, params)
    }

    fun logNotification(type: String) {
        val params = java.util.HashMap<String, String>()
        params["NotificationType"] = type
        logEvent(Event.NOTIFICATION_RECEIVED, params)
    }

    fun logNotificationClicked(type: String) {
        val params = java.util.HashMap<String, String>()
        params["NotificationType"] = type
        logEvent(Event.NOTIFICATION_CLICKED, params)
    }

    fun logScreenView(screen: Screen) {
        val params = java.util.HashMap<String, String>()
        params["ScreenName"] = screen.name
        logEvent(Event.SCREEN_VIEW, params)
    }

    fun logScreenView(screen: Screen, subtitle: String) {
        val params = java.util.HashMap<String, String>()
        params["ScreenName"] = "${screen.value} $subtitle"
        logEvent(Event.SCREEN_VIEW, params)
    }

    fun logShortFormContentView() {
        val playSessionId = UUID.randomUUID().toString()
        val params = HashMap<String, String>()
        params["ContentType"] = ShortForm
        params["PlaySessionId"] = playSessionId
        val genre = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_FW_CATEGORY)
        params["Genre"] =  if (genre.isNullOrEmpty()) "Default" else genre
        logEvent(Event.SHORT_CONTENT_START, params)
        SharedPreferenceStore.getInstance().save(KEY_SHORT_PLAY_SESSION_ID, playSessionId)
    }

    fun logShortFormContentPause() {
        val playSessionId = SharedPreferenceStore.getInstance().get(KEY_SHORT_PLAY_SESSION_ID)
        val params = HashMap<String, String>()
        params["ContentType"] = ShortForm
        params["PlaySessionId"] = playSessionId ?: ""
        val genre = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_FW_CATEGORY)
        params["Genre"] =  if (genre.isNullOrEmpty()) "Default" else genre
        logEvent(Event.SHORT_CONTENT_STOP, params)
        SharedPreferenceStore.getInstance().save(KEY_SHORT_PLAY_SESSION_ID, "")
    }

    fun logLongFormContentView(content: Content, isOffline: Boolean) {
        val playSessionId = UUID.randomUUID().toString()

        val params = HashMap<String, String>()
        params["ContentId"] = content.contentId
        params["Title"] = content.title
        params["Category"] = if(content.isMovie) "Movie" else "Series"
        params["ProviderId"] = content.contentProviderId
        params["Language"] = content.language
        params["Genre"] = content.genre
        params["ContentType"] =  LongForm
        params["IsDownloaded"] = if(isOffline) "1" else "0"
        params["PlaySessionId"] = playSessionId
        logEvent(Event.CONTENT_VIEW, params)

        SharedPreferenceStore.getInstance().save(KEY_PLAY_SESSION_ID, playSessionId)
    }

    fun logLongFormContentPause(content: Content) {
        val playSessionId = SharedPreferenceStore.getInstance().get(KEY_PLAY_SESSION_ID)

        val params = HashMap<String, String>()
        params["ContentId"] = content.contentId
        params["Title"] = content.title
        params["IsMovie"] = if(content.isMovie) "1" else "0"
        params["ProviderId"] = content.contentProviderId
        params["ContentType"] = LongForm
        params["PlaySessionId"] = playSessionId ?: ""
        logEvent(Event.CONTENT_STOP, params)

        SharedPreferenceStore.getInstance().save(KEY_PLAY_SESSION_ID, "")
    }

    fun setInteractionSessionId() {
        if (m_interactionSessionId == null) {
            m_interactionSessionId = getInteractionSessionId()
            SharedPreferenceStore.getInstance().save("SessionId", m_interactionSessionId ?: "")
        }
    }

    private fun getInteractionSessionId(): String? {
        if (m_interactionSessionId == null) {
            @Synchronized
                if (m_interactionSessionId == null) {
                    m_interactionSessionId = UUID.randomUUID().toString()
                }
        }
        return m_interactionSessionId
    }

    fun resetInteractionSessionId() {
        m_interactionSessionId = null
        SharedPreferenceStore.getInstance().save("SessionId", "")
    }


    private fun sanitizeUserId(userId: String): String {
        var userId = userId
        if (!TextUtils.isEmpty(userId) && userId.startsWith(MobileServiceUserIdPrefix)) {
            userId = userId.replace(MobileServiceUserIdPrefix, "")
        }
        return userId
    }

    fun logGenericLogs(event: Event, key: String, status: String?, details: String?) {
        val params = HashMap<String, String>()
        params["APIMethod"] = key
        params["Status"] = status ?: ""
        params["Details"] = details ?: ""
        logEvent(event, params)
    }

    fun logContentDownload(
            timeTaken: Long,
            content: com.msr.bine_sdk.cloud.models.Content,
            hubId: String,
            connectionType: String,
            frequencyHz: Int,
            downloadCount: String) {
        val params = java.util.HashMap<String, String>()
        params["DownloadTime"] = timeTaken.toString()
        params["ContentId"] = content.contentId
        params["ProviderId"] = content.contentProviderId
        params["Title"] = content.title
        params["HubDeviceId"] = hubId
        params["ConnectionType"] = connectionType
        params["FrequencyHz"] = frequencyHz.toString()
        params["AssetSize"] = "${content.audioTarFileSize + content.videoTarFileSize}"
        params["DownloadCount"] = downloadCount
        logEvent(Event.CONTENT_DOWNLOAD, params)
    }

    fun logContentShare(
        contentId: String,
        contentProviderId: String,
        title: String,
        type: String) {
        val params = java.util.HashMap<String, String>()
        params["ContentId"] = contentId
        params["ProviderId"] = contentProviderId
        params["Title"] = title
        params["ShareType"] = type
        logEvent(Event.SHARE, params)
    }
}