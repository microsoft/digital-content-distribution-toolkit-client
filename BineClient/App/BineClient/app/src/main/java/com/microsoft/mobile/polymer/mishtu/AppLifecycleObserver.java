package com.microsoft.mobile.polymer.mishtu;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.microsoft.mobile.polymer.mishtu.kaizala_utils.NetworkConnectivity;
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore;
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger;
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger;
import com.microsoft.mobile.polymer.mishtu.ui.activity.SubscriptionPackListActivity;
import com.microsoft.mobile.polymer.mishtu.ui.fragment.ContentFragment;


public class AppLifecycleObserver implements LifecycleObserver {

    private Application application;
    public static boolean IS_APP_IN_FOREGROUND = false;
    public AppLifecycleObserver(Application application) {
        super();
        this.application = application;
    }
    public static final String TAG = AppLifecycleObserver.class.getName();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        //run the code we need
        Log.d(TAG, "Entered Foreground");
        IS_APP_IN_FOREGROUND = true;
        BootTelemetryLogger.Companion.getInstance().start(BootTelemetryLogger.BootType.WARM);

        AnalyticsLogger.Companion.getInstance().setInteractionSessionId();
        AnalyticsLogger.Companion.getInstance().logAppStart();

        //If app closed while playing short form then send start event on resuming back
        String isPlaying = SharedPreferenceStore.Companion.getInstance().get(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER);
        if (isPlaying != null && isPlaying.equals("1")) {
            AnalyticsLogger.Companion.getInstance().logShortFormContentView();
        }

        NetworkConnectivity.getInstance(application).registerConnectivityNetworkMonitor();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        //run the code we need
        Log.d(TAG, "Entered Background");
        //saving the selectedContentProvider as movie screen one on app closed
        saveSelectedContentProviderForSubscriptionScreen();

        IS_APP_IN_FOREGROUND = false;
        //If app closed while playing short form, then send pause event
        String isPlaying = SharedPreferenceStore.Companion.getInstance().get(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER);
        if (isPlaying != null && isPlaying.equals("1")) {
            AnalyticsLogger.Companion.getInstance().logShortFormContentPause();
        }

        AnalyticsLogger.Companion.getInstance().logAppStop();
        AnalyticsLogger.Companion.getInstance().resetInteractionSessionId();

        NetworkConnectivity.getInstance(application).unregisterConnectivityNetworkMonitor(application);

        BootTelemetryLogger.Companion.getInstance().reset(false);
    }

    //saving the selectedContentProvider as movie screen one on app closed
    private static void saveSelectedContentProviderForSubscriptionScreen() {
        String selectedMovieCP = SharedPreferenceStore.Companion.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + ContentFragment.TYPE.MOVIES.name());
        if(selectedMovieCP !=null && !selectedMovieCP.isEmpty()) {
            SharedPreferenceStore.Companion.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + "_"+ SubscriptionPackListActivity.SUBSCRIPTION,
                   selectedMovieCP );
        }
    }
}
