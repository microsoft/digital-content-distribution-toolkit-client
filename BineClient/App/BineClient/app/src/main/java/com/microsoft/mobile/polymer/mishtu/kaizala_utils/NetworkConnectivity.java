package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.Keep;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used for notifications about network changes.
 */
@Keep
public class NetworkConnectivity {

    @Keep
    public enum NetworkType {
        None(0),
        MOBILE_2G(1),
        MOBILE_3G(2),
        MOBILE_4G(3),
        WIFI(4),
        UNKNOWN(5);

        private final int numVal;

        NetworkType(int numVal) {
            this.numVal = numVal;
        }

        @Keep
        public int getValue() {
            return numVal;
        }
    }

    private static volatile NetworkConnectivity sNetworkConnectivity;
    private final List<SoftReference<INetworkChangedListener>> sNetworkChangedListeners =
            Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private DetailNetworkStatus mLastConnectionDetailStatus;
    private DetailNetworkStatus mCurrentConnectionDetailStatus;

    private Handler mHandler;
    private Runnable mPeriodicCheckConnectivity;
    private volatile boolean mConnectivityPeriodicCheckScheduled = false;
    private Runnable mBroadcastCaptiveNetwork;
    private Runnable mStopCaptiveTest;
    private NetworkCapabilityDetails mCapabilitiesDetails;

    private AtomicLong mConnectivityCheckCount;
    private volatile boolean sIsCurrentlyAttemptingNetworkCheck = false;

    private ConnectivityManager mConnectivityManager;

    private static final long CONNECTIVITY_CHECK_INTERVAL_IN_MILLISECS = 1000;
    private static final long CONNECTIVITY_CHECK_MAX_COUNT_IN_UNBLOCKED_STATE = 10; // Check for 10 secs in unblocked state
    // (if blocked is not removed after checking 2 times then may be app is not supposed to use it)
    private static final long CONNECTIVITY_CHECK_MAX_COUNT_IN_BLOCKED_STATE = 2; // Check for 2 secs in blocked state

    private static final long START_CONNECTIVITY_CHECK_AFTER_INTERVAL_IN_MILLISECS = 10;

    private static final long BROADCAST_CAPTIVE_NETWORk_AFTER_INTERVAL_IN_MILLISECS = 100;

    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private AtomicBoolean isNetworkCallbackRegistered = new AtomicBoolean(false);

    private String lastNetIdentifier = "";
    private boolean netIdentifierChanged = false;

    private NetworkConnectivity(Context context) {

        mHandler = new Handler();
        mConnectivityCheckCount = new AtomicLong(0);
        mLastConnectionDetailStatus = new DetailNetworkStatus();
        mCurrentConnectionDetailStatus = new DetailNetworkStatus();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mCapabilitiesDetails = new NetworkCapabilityDetails();
        mPeriodicCheckConnectivity = () -> {

            // If network already connected then don't check connectivity again
            if (mLastConnectionDetailStatus.getNetworkType() == NetworkType.None) {

                // Check network state from OS
                checkConnectivity();

                // If still not connected after checking with OS
                if (mLastConnectionDetailStatus.getNetworkType() == NetworkType.None) {
                    if (mConnectivityCheckCount.get() < getConnectivityCheckMaxCount()) {
                        mConnectivityCheckCount.getAndIncrement();
                        getHandler().postDelayed(mPeriodicCheckConnectivity, CONNECTIVITY_CHECK_INTERVAL_IN_MILLISECS);
                    } else {
                        // Reached maximum tries, stop the periodic check
                        mConnectivityPeriodicCheckScheduled = false;
                    }
                }
            }
        };

        mBroadcastCaptiveNetwork = () -> {
            // Android detected that network is captive, broadcast this to App through Network capablities helper class
            NetworkCapabilities.getInstance().broadcastNetworkCapablitiesChanged(context, true, false);
        };

        mStopCaptiveTest = () -> {
            // Stop Network capablities Test for connectivity
            NetworkCapabilities.getInstance().stopNetworkCapabilitiesTest();
        };

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {

                netIdentifierChanged = !lastNetIdentifier.equals(network.toString());
                lastNetIdentifier = network.toString();
                NetworkType networkType = checkConnectivity();
                // Network type is changed stop the test if running
                getHandler().post(mStopCaptiveTest);

                if (networkType == NetworkType.None) {

                    // Report To telemetry that network not found
                    reportNetworkInconsistencyToTelemetry("NetworkCallback::onAvailable");

                    // Network should be available, OS invoked this callback. If not connected then Network may get connected,
                    // in a short interval, lets poll for a short interval
                    startCheckingConnectivityAtRegularInterval();
                }
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
            }

            @Override
            public void onLost(Network network) {
                checkConnectivity();
            }

            @Override
            public void onUnavailable() {
                checkConnectivity();
            }

            @Override
            public void onCapabilitiesChanged(Network network, android.net.NetworkCapabilities networkCapabilities) {

                // onCapabilitiesChanged() could get invoked frequently on a flaky network even for a change in
                // signal strength.
                // Return if previous network capabilities are same as the existing ones to avoid battery drain.
                if (!checkAndUpdateCapabilities(networkCapabilities)) {
                    return;
                }


                NetworkType networkType = checkConnectivity();
                if (networkType == NetworkType.None) {

                    // Report to Telemetry
                    reportNetworkInconsistencyToTelemetry("NetworkCallback::onCapabilitiesChanged");

                    // Network should be available, as capabilities of connected network changed . If not connected then Network may get connected,
                    // in a short interval, lets poll for a short interval
                    startCheckingConnectivityAtRegularInterval();
                }
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                checkConnectivity();
            }
        };

        mConnectivityPeriodicCheckScheduled = false;
    }

    /**
     * This method return the max iteration count for the periodic check
     * This count depends upon the network's detailed state
     * if it is BLOCKED then we will stop periodic check after CONNECTIVITY_CHECK_MAX_COUNT_IN_BLOCKED_STATE
     * otherwise we will check till CONNECTIVITY_CHECK_MAX_COUNT_IN_UNBLOCKED_STATE iterations
     *
     * @return max iteration Count
     */
    private long getConnectivityCheckMaxCount() {
        long maxCount;
        if (mLastConnectionDetailStatus.mNetworkDetailedState != NetworkInfo.DetailedState.BLOCKED) {
            maxCount = CONNECTIVITY_CHECK_MAX_COUNT_IN_UNBLOCKED_STATE;
        } else {
            maxCount = CONNECTIVITY_CHECK_MAX_COUNT_IN_BLOCKED_STATE;
        }
        return maxCount;
    }

    /***
     * Returns a singleton instance of NetworkConnectivity
     */
    @Keep
    public static NetworkConnectivity getInstance(Context context) {
        if (sNetworkConnectivity == null) {
            synchronized (NetworkConnectivity.class) {
                if (sNetworkConnectivity == null) {
                    sNetworkConnectivity = new NetworkConnectivity(context);
                }
            }
        }
        return sNetworkConnectivity;
    }

    /***
     * Returns current network state.
     * True if network is connected
     * False if network is disconnected
     * To check the network status, App should call this method
     */
    @Keep
    public boolean isNetworkConnected() {

        DetailNetworkStatus currentDetailNetworkStatus = getDetailNetworkStatus();
        boolean isConnected = currentDetailNetworkStatus.isNetworkUsable();
        boolean wasConnected = mLastConnectionDetailStatus.isNetworkUsable();

        // Missed Connect Broadcast or User signed in captive network
        if (mLastConnectionDetailStatus.mIsValuesSet && isConnected && !wasConnected) {
            // Inconsistency observed
            // Inconsistency observed. Network broadcast is not received or missed. Check the status and braodcast to all listeners
            // Although if network already connected then don't check connectivity again
            if (mLastConnectionDetailStatus.getNetworkType() == NetworkType.None) {
                startCheckingConnectivityAtRegularInterval();
            }
        } else if (!isConnected && wasConnected) {
            // Update the network state to disconnected otherwise when Network gets reconnected
            // NCH will not broadcast Connected event
            setLastDetailNetworkStatus(currentDetailNetworkStatus);
        } else if (currentDetailNetworkStatus.isCaptiveNetwork()) {
            setLastDetailNetworkStatus(currentDetailNetworkStatus);
        }

        return isConnected;
    }


    /**
     * Checks if Airplane mode is enabled on device
     *
     * @return True if airplane mode is enabled
     * False is airplane mode is disabled
     */
    @Keep
    public boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global
                .AIRPLANE_MODE_ON, 0 /* Value to return if the setting is not defined */) != 0;
    }

    /**
     * Checks the current connected network type 2G, 3G WIFI
     *
     * @return Returns a connected network Type NetworkType
     */
    @Keep
    public int getNetworkTypeAsInt() {
        NetworkType networkType = getConnectedNetworkType();
        return networkType.getValue();
    }

    /**
     * Checks the current connected network type 2G, 3G WIFI
     *
     * @return Returns a connected network Type NetworkType
     */
    public NetworkType getConnectedNetworkType() {
        DetailNetworkStatus detailNetworkStatus = getDetailNetworkStatus();
        return detailNetworkStatus.mNetworkType;
    }

    /**
     * Checks the current connected network type 2G, 3G WIFI
     *
     * @return Returns a connected network Type NetworkType
     */
    public boolean isConnectedNetworkCaptive() {
        return mLastConnectionDetailStatus.isCaptiveNetwork();
    }

    /***
     * Interface to listen the network events
     * onNetworkConnected is invoked if network gets connected
     * onNetworkDisconnected() is invoked when network gets disconnected
     * onNetworkTypeChanged is invoked when network type changes for example 2G->3G
     */
    public interface INetworkChangedListener {
        void onNetworkConnected(final NetworkType networkType);

        void onNetworkDisconnected();

        void onNetworkTypeChanged(final NetworkType networkType);
    }

    /***
     * API to register for network connectivity events
     * Any module/component interested in listening for connectivity events, must register through this API
     */
    public void registerListener(INetworkChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        synchronized (sNetworkChangedListeners) {
            sNetworkChangedListeners.add(new SoftReference<>(listener));
        }
    }

    /**
     * API to unregister for network connectivity events
     */
    public void unregisterListener(INetworkChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        synchronized (sNetworkChangedListeners) {
            Iterator<SoftReference<INetworkChangedListener>> iterator = sNetworkChangedListeners.iterator();
            while (iterator.hasNext()) {
                SoftReference<INetworkChangedListener> registeredListenerRef = iterator.next();
                INetworkChangedListener registeredListener = registeredListenerRef.get();
                if (registeredListener == listener) {
                    sNetworkChangedListeners.remove(registeredListenerRef);
                    return;
                } else if (registeredListener == null) {
                    // we have a dead reference we will remove it from the array and not on COW iterator
                    // since element-changing operations on iterators themselves (remove, set, and add) are not supported.
                    sNetworkChangedListeners.remove(registeredListenerRef);
                }
            }
        }
    }

    /**
     * This function return the last connection type whether.
     *
     * @return last connection network type it case 2G/3G/4G/Wifi or None in case of no network
     */
    public NetworkType getLastConnectionType() {
        return mLastConnectionDetailStatus.getNetworkType();
    }

    /***
     * This API should be invoked by PolymerApplication only to register network connectivity with
     * Android.
     */
    public void registerConnectivityNetworkMonitor() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mConnectivityManager != null && !isNetworkCallbackRegistered.get()) {
                isNetworkCallbackRegistered.set(true);
                mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
            }
        }
    }

    public void unregisterConnectivityNetworkMonitor(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mConnectivityManager != null) {
                if(isNetworkCallbackRegistered.get()) {
                    isNetworkCallbackRegistered.set(false);
                    mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
                }
            } else {
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).unregisterNetworkCallback(mNetworkCallback);
            }
        }
    }

    /***
     * Whenever App observe inconsistency in network state, will call this API to report Telemetry data
     * If App identifies network is connected but NCH state is not connected then thats an inconsistency in network state
     * @param inconsistencySource Caller need to specify the source of information. For example if FCM handler identifies
     *                            inconsistency in network state, it will specify source as "FCMHandler".
     *                            This will help while analyzing the telemetry data.
     */
    public void reportNetworkInconsistencyToTelemetry(String inconsistencySource) {

    }

    private DetailNetworkStatus getDetailNetworkStatus() {
        NetworkInfo activeNetworkInfo = mConnectivityManager == null ? null : mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {

            if (!activeNetworkInfo.isConnected()) {
                // Log connection status for further analysis
            }
            mCurrentConnectionDetailStatus.mIsNetworkConnected = activeNetworkInfo.isConnected();
            mCurrentConnectionDetailStatus.mIsNetworkAvailable = activeNetworkInfo.isAvailable();
            mCurrentConnectionDetailStatus.mIsCaptiveNetwork = NetworkCapabilities.isCaptiveNetwork(mConnectivityManager);
            mCurrentConnectionDetailStatus.mNetworkState = activeNetworkInfo.getState();
            mCurrentConnectionDetailStatus.mNetworkDetailedState = activeNetworkInfo.getDetailedState();
            if (mCurrentConnectionDetailStatus.mIsNetworkConnected) {
                mCurrentConnectionDetailStatus.mNetworkType = NetworkCapabilities.getConnectedNetworkType(activeNetworkInfo, mCurrentConnectionDetailStatus.mIsCaptiveNetwork);
            } else {
                mCurrentConnectionDetailStatus.mNetworkType = NetworkType.None;
            }
            mCurrentConnectionDetailStatus.mIsValuesSet = true;
        } else {
            // activeNetworkInfo is null. Mark network as disconnected
            mCurrentConnectionDetailStatus.mIsNetworkConnected = false;
            mCurrentConnectionDetailStatus.mIsNetworkAvailable = false;
            mCurrentConnectionDetailStatus.mIsCaptiveNetwork = false;
            mCurrentConnectionDetailStatus.mNetworkState = NetworkInfo.State.DISCONNECTED;
            mCurrentConnectionDetailStatus.mNetworkDetailedState = NetworkInfo.DetailedState.DISCONNECTED;
            mCurrentConnectionDetailStatus.mNetworkType = NetworkType.None;
            mCurrentConnectionDetailStatus.mIsValuesSet = true;
        }

        return mCurrentConnectionDetailStatus;
    }

    /***
     * This API returns the detail network status as a string, which can be used for logging
     */
    public String getNetworkStatusAsString() {
        DetailNetworkStatus detailNetworkStatus = getDetailNetworkStatus();
        return detailNetworkStatus.getNetworkStatusAsString();
    }

    private synchronized void setLastDetailNetworkStatus(DetailNetworkStatus newNetworkStatus) {
        if (newNetworkStatus.isCaptiveNetwork() && !NetworkCapabilities.getInstance().isNoNetworkDialogShownToUser()) {
            // Network is captive broadcast Captive to all listeners
            getHandler().postDelayed(mBroadcastCaptiveNetwork, BROADCAST_CAPTIVE_NETWORk_AFTER_INTERVAL_IN_MILLISECS);
        }
        mLastConnectionDetailStatus = new DetailNetworkStatus(newNetworkStatus);
        if (newNetworkStatus.isNetworkUsable()) {
            // Network is connected
            // Network got connected
            mConnectivityPeriodicCheckScheduled = false;
            mConnectivityCheckCount.set(0);
            // Remove all scheduled callbacks
            getHandler().removeCallbacksAndMessages(mPeriodicCheckConnectivity);
        }
    }

    synchronized boolean checkAndUpdateCapabilities(android.net.NetworkCapabilities currentCapabilities) {
        return mCapabilitiesDetails.checkAndUpdateCapabilities(currentCapabilities);
    }

    synchronized NetworkType checkConnectivity() {
        DetailNetworkStatus currentDetailNetworkStatus = getDetailNetworkStatus();

        final boolean wasNetworkConnected = mLastConnectionDetailStatus.isNetworkUsable();
        final boolean isNetworkConnected = currentDetailNetworkStatus.isNetworkUsable();
        final boolean hasNetworkCapablitiesChanged = netIdentifierChanged || mLastConnectionDetailStatus.isChanged(currentDetailNetworkStatus);
        final boolean hasNetworkGotConnected = !wasNetworkConnected && isNetworkConnected;
        final boolean hasNetworkGotDisConnected = mLastConnectionDetailStatus.mIsValuesSet && wasNetworkConnected && !isNetworkConnected;
        final boolean isNetworkDetailedStateChanged = mLastConnectionDetailStatus.mNetworkDetailedState != currentDetailNetworkStatus.mNetworkDetailedState;

        if (hasNetworkCapablitiesChanged) {
            synchronized (sNetworkChangedListeners) {
                Iterator<SoftReference<INetworkChangedListener>> iterator = sNetworkChangedListeners.iterator();
                while (iterator.hasNext()) {
                    SoftReference<INetworkChangedListener> networkChangedListenerSoftReference = iterator.next();
                    INetworkChangedListener registeredListener = networkChangedListenerSoftReference.get();
                    if (registeredListener != null) {
                        if (hasNetworkGotConnected) { // Got connected
                            registeredListener.onNetworkConnected(currentDetailNetworkStatus.getNetworkType());
                        } else if (hasNetworkGotDisConnected) { // Got disconnected
                            registeredListener.onNetworkDisconnected();
                        } else if (netIdentifierChanged || currentDetailNetworkStatus.getNetworkType() != mLastConnectionDetailStatus.getNetworkType()) { // Network type changed like 2G-> 3G
                            registeredListener.onNetworkTypeChanged(currentDetailNetworkStatus.getNetworkType());
                        }
                    } else {
                        // we have a dead reference we will remove it from the array and not on COW iterator
                        // since element-changing operations on iterators themselves (remove, set, and add) are not supported.
                        sNetworkChangedListeners.remove(networkChangedListenerSoftReference);
                    }
                }
            }
            setLastDetailNetworkStatus(currentDetailNetworkStatus);
        } else if (isNetworkDetailedStateChanged) {
            // setting it so that periodic check can be stopped when the detailed state change from disconnected to blocked and vise versa
            setLastDetailNetworkStatus(currentDetailNetworkStatus);
        }
        return currentDetailNetworkStatus.getNetworkType();
    }

    private synchronized void startCheckingConnectivityAtRegularInterval() {
        if (!mConnectivityPeriodicCheckScheduled) {
            mConnectivityPeriodicCheckScheduled = true;
            mConnectivityCheckCount.set(0);
            getHandler().postDelayed(mPeriodicCheckConnectivity, NetworkConnectivity.START_CONNECTIVITY_CHECK_AFTER_INTERVAL_IN_MILLISECS);
        }
    }

    private Handler getHandler() {
        return mHandler;
    }

}
