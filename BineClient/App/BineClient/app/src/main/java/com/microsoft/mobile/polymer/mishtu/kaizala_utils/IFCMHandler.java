package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.content.Context;

import java.util.Map;

public interface IFCMHandler {

    /**
     * Called when a message is received.
     *
     * This is also called when a notification message is received while the app is in the foreground.
     * @param context context
     * @param notificationData notification data
     */
    void onMessageReceived(Context context, Map<String, String> notificationData);

    /**
     * Called when the FCM server deletes pending messages. This may be due to:
     *
     *  1. Too many messages stored on the FCM server. This can occur when an app's servers send a bunch of non-collapsible messages to FCM servers while the device is offline.
     *  2. The device hasn't connected in a long time and the app server has recently (within the last 4 weeks) sent a message to the app on that device.
     *
     *  It is recommended that the app do a full sync with the app server after receiving this call.
     */
    void onDeleteMessages();
}
