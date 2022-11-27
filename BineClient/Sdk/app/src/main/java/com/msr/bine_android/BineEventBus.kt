package com.msr.bine_android

import android.content.Context
import android.content.Intent
import android.util.Log
import com.msr.bine_android.data.DataRepository
import com.msr.bine_sdk.download.exo.DRMManager
import com.msr.bine_sdk.eventbus.EventBusInterface
import com.msr.bine_sdk.eventbus.events.*
import com.msr.bine_sdk.player.BinePlayerViewActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BineEventBus(var context: Context, var dataRepository: DataRepository): EventBusInterface {

    override fun register() {
        EventBus.getDefault().register(this)
    }

    override fun unRegister() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onNotificationVoucherClicked(event: VoucherEvent) {
        /*if (!TextUtils.isEmpty(event.voucherCode)) {
            showCouponDialog(event.voucherCode)
        }*///TODO:
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onContentDownloadComplete(event: DownloadCompleteEvent) {
        val playerIntent = Intent(context, BinePlayerViewActivity::class.java)
        playerIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        playerIntent.putExtra(com.msr.bine_sdk.Constants.TITLE, event.videoTitle)
        playerIntent.putExtra(com.msr.bine_sdk.Constants.VIDEO_URL, event.videoURL)
        playerIntent.putExtra(com.msr.bine_sdk.Constants.FOLDER_ID, event.folderId)
        context.startActivity(playerIntent)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun onMessage(event: AnalyticsEvent) {
        //Telemetry.getInstance().sendCustomEvent(event.key, event.params)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onLogout(event: LogoutEvent) {
        DRMManager.getInstance(context).resetDownloadManager();
//        val intent = Intent(context, LoginActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(intent)
        dataRepository.saveUserId("")
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun onLogEvent(event: LogEvent) {
        Log.d("BineEventBus", event.key + ":" + event.description)
    }
}