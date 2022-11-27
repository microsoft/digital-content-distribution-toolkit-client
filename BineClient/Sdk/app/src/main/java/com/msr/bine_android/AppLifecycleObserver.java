package com.msr.bine_android;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.msr.bine_android.telemetry.Telemetry;

public class AppLifecycleObserver implements LifecycleObserver {
    public static final String TAG = AppLifecycleObserver.class.getName();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        //run the code we need
        Log.d(TAG, "Entered Foreground");
        Telemetry.getInstance().sendAppInForeground();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        //run the code we need
        Log.d(TAG, "Entered Background");
        Telemetry.getInstance().sendAppInBackground();
    }
}
