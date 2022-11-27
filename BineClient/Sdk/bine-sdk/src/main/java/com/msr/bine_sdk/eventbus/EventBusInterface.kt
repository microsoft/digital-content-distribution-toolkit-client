package com.msr.bine_sdk.eventbus

import com.msr.bine_sdk.eventbus.events.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

interface EventBusInterface {
    fun register()

    fun unRegister()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationVoucherClicked(event: VoucherEvent)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContentDownloadComplete(event: DownloadCompleteEvent)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessage(event: AnalyticsEvent)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogout(event: LogoutEvent)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogEvent(event: LogEvent)
}