package com.msr.bine_sdk.notifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.msr.bine_sdk.Constants;
import com.msr.bine_sdk.eventbus.events.DownloadCompleteEvent;
import com.msr.bine_sdk.eventbus.events.VoucherEvent;

import org.greenrobot.eventbus.EventBus;

import static com.msr.bine_sdk.notifications.NotificationHelper.DOWNLOAD_STATUS_NOTIFICATION_ID;
import static com.msr.bine_sdk.notifications.NotificationHelper.NOTIFICATION_DOWNLOAD_PROGRESS;
import static com.msr.bine_sdk.notifications.NotificationHelper.NOTIFICATION_VOUCHER;
import static com.msr.bine_sdk.notifications.NotificationHelper.NOTIFICATION_WIFI;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int id = intent.getIntExtra("id", 0);
        Log.e("here id", "--" + id);
        if (id == NOTIFICATION_VOUCHER) {
            VoucherEvent voucherEvent = new VoucherEvent();
            voucherEvent.setVoucherCode(intent.getStringExtra(Constants.COUPON_VALUE));
            EventBus.getDefault().post(voucherEvent);
        } else if (id == NOTIFICATION_WIFI) {
            Intent settingsIntent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);
        } else if (id == DOWNLOAD_STATUS_NOTIFICATION_ID) {
            DownloadCompleteEvent downloadCompleteEvent=new DownloadCompleteEvent();
            downloadCompleteEvent.setFolderId(intent.getStringExtra(Constants.FOLDER_ID));
            downloadCompleteEvent.setVideoTitle(intent.getStringExtra(Constants.TITLE));
            downloadCompleteEvent.setVideoURL(intent.getStringExtra(Constants.VIDEO_URL));
            EventBus.getDefault().post(downloadCompleteEvent);
            notificationManager.cancel(NOTIFICATION_DOWNLOAD_PROGRESS);
        }
        /**
         * cancel notification
         */
        notificationManager.cancel(id);
    }
}