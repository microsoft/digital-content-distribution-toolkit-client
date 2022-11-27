package com.microsoft.mobile.polymer.mishtu.utils

import android.content.Context
import android.content.Intent
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.activity.PhoneLoginActivity
import com.msr.bine_sdk.eventbus.EventBusInterface
import com.msr.bine_sdk.eventbus.events.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BineEventBus(private val context: Context): EventBusInterface {
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onContentDownloadComplete(event: DownloadCompleteEvent) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onLogEvent(event: LogEvent) {
        //if (!event.description.equals("Success", true)) {
            AnalyticsLogger.getInstance()
                .logGenericLogs(Event.API_LOG, event.key, event.description, event.exception)
        //}
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onLogout(event: LogoutEvent) {
        val params = java.util.HashMap<String, String>()
        params["LogoutEvent"] = "Token Expired"
        AnalyticsLogger.getInstance().logEvent(Event.LOGOUT_APP, params)

        context.startActivity(
            Intent(context, PhoneLoginActivity::class.java)
                .putExtra(PhoneLoginActivity.WAS_FORCED_LOGOUT, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun onMessage(event: AnalyticsEvent) {
        //event.params?.let { AnalyticsLogger.getInstance().logEvent(Event.CUSTOM_SDK, it) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onNotificationVoucherClicked(event: VoucherEvent) {
    }

    override fun register() {
        EventBus.getDefault().register(this)
    }

    override fun unRegister() {
        EventBus.getDefault().unregister(this)
    }
}