package com.msr.bine_sdk.network_old;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Check device's network connectivity and speed
 *
 */
public class Connectivity {

    private static final String TAG = Connectivity.class.getSimpleName();

    /**
     * Get the network info
     *
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }

    public static boolean checkMobileDataIsEnabled(Context context) {
        boolean mobileYN = false;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int dataState = tel.getDataState();
            Log.v(TAG, "tel.getDataState() : " + dataState);
            if (dataState != TelephonyManager.DATA_DISCONNECTED) {
                mobileYN = true;
            }
        }
        return mobileYN;
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                    return false;

                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                    return true;
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11  // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 // ~ 10+ Mbps
                    return true;
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8 // ~25 kbps
                    return false;
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isConnected(Context context) {
        boolean connected;
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return false;
    }
}