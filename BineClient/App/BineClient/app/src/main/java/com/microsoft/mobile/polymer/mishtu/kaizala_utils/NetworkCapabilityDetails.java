package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.BitSet;

/**
 * Class to store Network capabilities
 * @author Nirendra Awasthi
 * @since 05-10-2018.
 */

public class NetworkCapabilityDetails {
    private BitSet mOldCapabilities;

    NetworkCapabilityDetails() {
        mOldCapabilities = new BitSet(32);
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean checkAndUpdateCapabilities(android.net.NetworkCapabilities networkCapabilities) {
        BitSet currentCapabilities = new BitSet(32);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentCapabilities.set(android.net.NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL, networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL));
            currentCapabilities.set(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED, networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        }
        currentCapabilities.set(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET, networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET));
        currentCapabilities.set(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED, networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED));
        currentCapabilities.set(android.net.NetworkCapabilities.TRANSPORT_CELLULAR, networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
        currentCapabilities.set(android.net.NetworkCapabilities.TRANSPORT_WIFI, networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI));

        if(!mOldCapabilities.equals(currentCapabilities)) {
            mOldCapabilities = currentCapabilities;
            return true;
        } else {
            return false;
        }
    }
}
