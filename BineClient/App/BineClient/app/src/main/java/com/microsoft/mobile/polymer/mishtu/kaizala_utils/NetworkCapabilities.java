package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.Keep;
import android.telephony.TelephonyManager;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by ajay on 31/08/18.
 */

@Keep
public class NetworkCapabilities {

    /***
     * Interface to listen the network capabilities changes
     * onNetworkBecameCaptive is invoked if connected network is wifi and gets detected as Captive
     * onNetworkHasNoInternet() is invoked when connected network is wifi and gets detected as no internet
     */
    public interface INetworkCapabilitiesListener {
        void onNetworkBecameCaptive();
        void onNetworkHasNoInternet();
    }

    private boolean mNoInternetDialogShownToUser = false;
    private long mNoInternetDialogShownTime = 0;
    private boolean mNetworkCapabilitiesTestScheduled = false;
    private final Object mTestScheduledLock = new Object();

    private static final String LOG_TAG = "NetworkCapabilities";

    private final List<SoftReference<INetworkCapabilitiesListener>> sNetworkCapabilitiesListeners =
            Collections.synchronizedList(new CopyOnWriteArrayList<SoftReference<INetworkCapabilitiesListener>>());

    private static class InstanceHolder {
        static final NetworkCapabilities INSTANCE = new NetworkCapabilities();
    }

    private NetworkCapabilities() {
    }

    /***
     * Returns a singleton instance of NetworkCapabilities
     */
    @Keep
    public static NetworkCapabilities getInstance()
    {
        return InstanceHolder.INSTANCE;
    }

    /***
     * API to register for network capabilities changes events
     * Any module/component interested in listening for network capabilities events, must register through this API
     */
    public void registerListener(INetworkCapabilitiesListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        synchronized (sNetworkCapabilitiesListeners) {
            sNetworkCapabilitiesListeners.add(new SoftReference<>(listener));
        }
    }

    /**
     * API to unregister for network capabilities events
     */
    public void unregisterListener(INetworkCapabilitiesListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        synchronized (sNetworkCapabilitiesListeners) {
            Iterator<SoftReference<INetworkCapabilitiesListener>> iterator = sNetworkCapabilitiesListeners.iterator();
            while (iterator.hasNext()) {
                SoftReference<INetworkCapabilitiesListener> registeredListenerRef = iterator.next();
                INetworkCapabilitiesListener registeredListener = registeredListenerRef.get();
                if (registeredListener == listener) {
                    sNetworkCapabilitiesListeners.remove(registeredListenerRef);
                    return;
                } else if (registeredListener == null) {
                    // we have a dead reference we will remove it from the array and not on COW iterator
                    // since element-changing operations on iterators themselves (remove, set, and add) are not supported.
                    sNetworkCapabilitiesListeners.remove(registeredListenerRef);
                }
            }
        }
    }

    public void stopNetworkCapabilitiesTest()
    {
        synchronized (mTestScheduledLock) {
            if (mNetworkCapabilitiesTestScheduled) {
                // Stop the check
                mNetworkCapabilitiesTestScheduled = false;
            }
        }
    }


    private boolean shouldBroadcastNetworkCapabilitiesChanged(Context context)
    {
        NetworkConnectivity.NetworkType networktype = NetworkConnectivity.getInstance(context).getConnectedNetworkType();
        // Broadcast if
        // - Notification not shown to user And
        // - In case OS detected captive Or Connected network type is WIFI AND
        // - Feature enabled

        if(!isNoNetworkDialogShownToUser() &&
                ( NetworkConnectivity.getInstance(context).isConnectedNetworkCaptive()|| (networktype == NetworkConnectivity.NetworkType.WIFI)))
        {
            return true;
        }

        return false;
    }

    @Keep
    void notifyNetworkCapablitiesTestResult(Context context, boolean internetCapabilityIssueObserved){
        if (!isNetworkCapabilitiesTestScheduled()) {
            // Test is already stopped, ignore the result
            return;
        }

        synchronized (mTestScheduledLock) {
            mNetworkCapabilitiesTestScheduled = false;
        }

        if(internetCapabilityIssueObserved) {
            broadcastNetworkCapablitiesChanged(context, false, internetCapabilityIssueObserved);
        }
    }

    void broadcastNetworkCapablitiesChanged(Context context, boolean networkBecomeCaptive, boolean deviceHasNoInternet){
        if(shouldBroadcastNetworkCapabilitiesChanged(context)) {
            THREAD_POOL_EXECUTOR.execute(new Runnable() {
                 @Override
                 public void run() {
                     synchronized (sNetworkCapabilitiesListeners) {
                         Iterator<SoftReference<INetworkCapabilitiesListener>> iterator = sNetworkCapabilitiesListeners.iterator();
                         while (iterator.hasNext()) {
                             SoftReference<INetworkCapabilitiesListener> networkChangedListenerSoftReference = iterator.next();
                             INetworkCapabilitiesListener registeredListener = networkChangedListenerSoftReference.get();
                             if (registeredListener != null) {

                                 if (networkBecomeCaptive) {
                                     registeredListener.onNetworkBecameCaptive();
                                 }

                                 if (deviceHasNoInternet) {
                                     registeredListener.onNetworkHasNoInternet();
                                 }

                             } else {
                                 // we have a dead reference we will remove it from the array and not on COW iterator
                                 // since element-changing operations on iterators themselves (remove, set, and add) are not supported.
                                 sNetworkCapabilitiesListeners.remove(networkChangedListenerSoftReference);
                             }
                         }
                     }
                 }
             }
            );
        }
    }



    boolean isNetworkCapabilitiesTestScheduled(){
        synchronized (mTestScheduledLock) {
            return mNetworkCapabilitiesTestScheduled;
        }
    }

    boolean isNoNetworkDialogShownToUser()
    {
        return mNoInternetDialogShownToUser;
    }

    long getNoInternetDialogShownTime() {
        return mNoInternetDialogShownTime;
    }

    void setNoInternetDialogShownTime(long appNoInternetDialogShownTime) {
        mNoInternetDialogShownTime = appNoInternetDialogShownTime;
    }

    @TargetApi(Build.VERSION_CODES.M)
    static boolean isCaptiveNetwork(ConnectivityManager connectivityManager)
    {
        boolean captiveNetwork = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager != null) {
                Network network = connectivityManager.getActiveNetwork();
                android.net.NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    captiveNetwork = networkCapabilities.hasCapability(android.net.NetworkCapabilities
                            .NET_CAPABILITY_CAPTIVE_PORTAL);
                }
            }
        }

        return captiveNetwork;
    }

    static NetworkConnectivity.NetworkType getConnectedNetworkType(final NetworkInfo activeNetworkInfo, final boolean isCaptiveNetwork) {
        boolean isNetworkConnected =  false;
        if(activeNetworkInfo != null) {
            isNetworkConnected = activeNetworkInfo.isConnected() &&
                    activeNetworkInfo.isAvailable()&&
                    !isCaptiveNetwork;
        }

        if(!isNetworkConnected) {
            return NetworkConnectivity.NetworkType.None;
        }

        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NetworkConnectivity.NetworkType.WIFI;
        }

        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = activeNetworkInfo.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return NetworkConnectivity.NetworkType.MOBILE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return NetworkConnectivity.NetworkType.MOBILE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return NetworkConnectivity.NetworkType.MOBILE_4G;
                default:
                    return NetworkConnectivity.NetworkType.UNKNOWN;
            }
        }
        return NetworkConnectivity.NetworkType.UNKNOWN;
    }
}
