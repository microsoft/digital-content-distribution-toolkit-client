package com.msr.bine_android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import static com.msr.bine_sdk.notifications.NotificationHelper.DOWNLOAD_STATUS_NOTIFICATION_ID;
import static com.msr.bine_sdk.notifications.NotificationHelper.NOTIFICATION_VOUCHER;
import static com.msr.bine_sdk.notifications.NotificationHelper.NOTIFICATION_WIFI;

public class AppNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int id = intent.getIntExtra("id", 0);
        if (id == NOTIFICATION_VOUCHER) {
            /*Intent mainIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.putExtra(Constants.SHOW_COUPON, true);
            context.startActivity(mainIntent);*/
        } else if (id == NOTIFICATION_WIFI) {
            /*Intent settingsIntent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);*/
        } else if (id == DOWNLOAD_STATUS_NOTIFICATION_ID) {
            /*Intent openPlayerIntent = new Intent(context.getApplicationContext(), BinePlayerViewActivity.class);
            openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openPlayerIntent.putExtra("id", DOWNLOAD_STATUS_NOTIFICATION_ID);
            openPlayerIntent.putExtra("title", intent.getStringExtra("title"));
            openPlayerIntent.putExtra("VideoURL", intent.getStringExtra("VideoURL"));
            openPlayerIntent.putExtra("folderId", intent.getStringExtra("folderId"));
            context.startActivity(openPlayerIntent);*/
        }

        notificationManager.cancel(id);
    }
}