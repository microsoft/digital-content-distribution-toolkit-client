package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by nkhattar on 9/23/2016.
 */
public class DeviceUtil {
    private static final String LOG_TAG = "DeviceUtil";

    // Device dimensions and DP scale factor
    private static int mHeight = 0;
    private static int mWidth = 0;

    private static DeviceType mType = null;
    private static double mDeviceSize = 0.0;
    private static String mDeviceId = "";

    // Threshold dimensions of table. Same as WXPO
    public static final int TABLET_MIN_WIDTH = 510;
    private static final long NUMBER_OF_BYTES_IN_MEGABYTE = 1048576L; // 1024 * 1024;

    // Type of device.
    public enum DeviceType {
        PHONE,
        TABLET,
        TV
    }

    /**
     * we have synced with Office team to understand threat , and they have exempted hardware ID from being PII
     */
    @SuppressLint("HardwareIds")
    public static @NonNull
    String getDeviceId(Context context) {
        if (context == null) return "";
        // if already set, return early as the call has associated perf cost
        if (!TextUtils.isEmpty(mDeviceId)) return mDeviceId;

        mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return (mDeviceId == null) ? "" : mDeviceId;
    }

    public static DeviceType getDeviceType(Context context) {
        // Need to set the type of device once
        if (mType == null) {
            updateWidth(context);
            updateDeviceType(context);
        }
        return mType;
    }

    public static double getScreenSize(Context context) {
        if (Double.compare(mDeviceSize, 0.0) == 0) {
            DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
            mDeviceSize = Math.sqrt(Math.pow(displaymetrics.widthPixels / displaymetrics.xdpi, 2) +
                    Math.pow(displaymetrics.heightPixels / displaymetrics.ydpi, 2));

        }
        return mDeviceSize;
    }

    public static String getShellResult(String[] command) {
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            reader.close();
            return buffer.toString();
        } catch (Exception e) {

            return "";
        }
    }

    private static void updateWidth(Context context) {
        float scaleFactor = 1.0f;
        if (context != null) {
            DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
            scaleFactor = ((float) displaymetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

            //Updating Width
            mWidth = (int) (displaymetrics.widthPixels / scaleFactor);

            //Updating Height
            mHeight = (int) (displaymetrics.heightPixels / scaleFactor);
        }
    }

    private static void updateDeviceType(Context context) {
        if (context == null) {
            return;
        }

        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);

        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            mType = DeviceType.TV;
            return;
        }

        if (mHeight != 0 && mWidth != 0) {
            int minWidth = Math.min(mWidth, mHeight);
            if (minWidth >= TABLET_MIN_WIDTH) {
                mType = DeviceType.TABLET;
            } else {
                mType = DeviceType.PHONE;
            }
        }
    }

    /**
     * This method will return total memory in device in MB
     * @return total memory in device
     *//*
    public static long getTotalMemory()
    {
        Context context = ContextContainer.getContext();
        ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
        if (activityManager == null) {
            return -1;
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem / NUMBER_OF_BYTES_IN_MEGABYTE;
    }

    *//**
     * This method will return available memory in device in MB
     * @return available memory in device
     *//*
    public static long getAvailableMemory()
    {
        Context context = ContextContainer.getContext();
        ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
        if (activityManager == null) {
            return -1;
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.availMem / NUMBER_OF_BYTES_IN_MEGABYTE;
    }

    *//**
     * This method will return true if device have low RAM false otherwise
     * @return true if device have low RAM false otherwise
     *//*
    public static boolean isLowRamDevice() {
        Context context = ContextContainer.getContext();
        ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
        if (activityManager == null) {
            return false;
        }
        return activityManager.isLowRamDevice();
    }*/
}
