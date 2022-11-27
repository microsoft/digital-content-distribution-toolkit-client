package com.microsoft.mobile.polymer.mishtu.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentManager

import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.SubscriptionActivity
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetOrderComplete

class NotificationActionHandler (private val activity: Activity, private val fragmentManager: FragmentManager?) {

    fun onWatchContentClicked() {
        val activity: MainActivity = activity as MainActivity
        activity.startService(/*BNConstants.Service.ENTERTAINMENT*/)
    }

    fun onRenewPackClicked() {
        fragmentManager?.let {
            AppUtils.startOrderFlow(it, activity as Context,null)
        }
    }

    fun viewOffersClicked() {

    }

    fun viewSubscriptionClicked() {
        val subscriptionActivity = Intent(activity, SubscriptionActivity::class.java)
        activity.startActivity(subscriptionActivity)
    }

    fun handleNotification(type: String, notificationId: Int, dataId: String?) {
        AnalyticsLogger.getInstance().logNotificationClicked(type)
        val notificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        if (type == BNConstants.NotificationType.NewContentArrived.name) {
            Handler(Looper.getMainLooper()).postDelayed({
                val activity: MainActivity = activity as MainActivity
                activity.startFilmsTab()
            }, 1000)
        }
        if (type == BNConstants.NotificationType.NewOffer.name) {
            Handler(Looper.getMainLooper()).postDelayed({
                val activity: MainActivity = activity as MainActivity
                activity.startService(/*BNConstants.Service.OFFERS*/)
            }, 1000)
        }
        if (type == BNConstants.NotificationType.OrderComplete.name) {
            Log.d("NoAction", dataId.toString()+"_"+notificationId)
            fragmentManager?.let {
                val bottomSheet = BottomSheetOrderComplete(dataId)
                bottomSheet.show(it,bottomSheet.tag)
            }
        }
        if (type == BNConstants.NotificationType.UserDataExportComplete.name) {
            Handler(Looper.getMainLooper()).postDelayed({
                val activity: MainActivity = activity as MainActivity
                activity.exportData()
            }, 500)
        }
        if (type == BNConstants.NotificationType.Downloads.name) {
            Handler(Looper.getMainLooper()).postDelayed({
                val activity: MainActivity = activity as MainActivity
                activity.selectTab(activity.downloadTabIndex) }, 500)
        }
    }
}