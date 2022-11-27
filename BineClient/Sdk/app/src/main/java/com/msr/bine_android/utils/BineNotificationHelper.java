package com.msr.bine_android.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.msr.bine_android.R;
import com.msr.bine_sdk.notifications.NotificationHelper;

public class BineNotificationHelper extends NotificationHelper {
    public BineNotificationHelper(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public String getMetaResourceBaseUrl() {
        return "https://stcontentcdnstage.blob.core.windows.net/";
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic_app_icon;
    }

    @NonNull
    @Override
    public Intent getHomeLaunchIntent() {
        return null;
    }
}
