package com.microsoft.mobile.polymer.mishtu.utils

import android.content.Context
import android.content.Intent
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.msr.bine_sdk.notifications.NotificationHelper

class BineNotificationHelper(val context: Context): NotificationHelper(context){

    override fun getAppIcon(): Int {
        return R.drawable.bn_app_logo
    }

    override fun getMetaResourceBaseUrl(): String {
        return BuildConfig.CDN_BASE_URL
    }

    override fun getHomeLaunchIntent(): Intent {
        val openAppIntent = Intent(context, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        openAppIntent.action = MainActivity.NOTIFICATION_TAPPED_BY_USER
        openAppIntent.putExtra(MainActivity.NOTIFICATION_TYPE, BNConstants.NotificationType.Downloads.name)
        return openAppIntent
    }
}