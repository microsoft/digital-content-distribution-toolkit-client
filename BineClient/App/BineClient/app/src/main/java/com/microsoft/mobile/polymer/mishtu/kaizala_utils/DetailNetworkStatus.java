package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.net.NetworkInfo;

/**
 * Created by ajay on 31/08/18.
 */

class DetailNetworkStatus {
    boolean mIsNetworkConnected ;
    boolean mIsNetworkAvailable ;
    boolean mIsCaptiveNetwork ;
    NetworkConnectivity.NetworkType mNetworkType;
    NetworkInfo.State mNetworkState;
    NetworkInfo.DetailedState mNetworkDetailedState;
    boolean mIsValuesSet;

    DetailNetworkStatus(){
        mNetworkType = NetworkConnectivity.NetworkType.UNKNOWN;
        mIsNetworkConnected = false;
    }

    DetailNetworkStatus(DetailNetworkStatus dns) {
        this.mIsNetworkConnected = dns.mIsNetworkConnected;
        this.mIsNetworkAvailable = dns.mIsNetworkAvailable;
        this.mIsCaptiveNetwork = dns.mIsCaptiveNetwork;
        this.mNetworkType = dns.mNetworkType;
        this.mNetworkState = dns.mNetworkState;
        this.mIsValuesSet = dns.mIsValuesSet;
        this.mNetworkDetailedState = dns.mNetworkDetailedState;
    }

    boolean isNetworkUsable() {

        return mIsNetworkConnected &&
                mIsNetworkAvailable &&
                !mIsCaptiveNetwork;
    }

    boolean isCaptiveNetwork() {
        return mIsCaptiveNetwork;
    }


    boolean hasNetworkBecomeCaptive(DetailNetworkStatus newNetworkStatus) {
        return (! mIsCaptiveNetwork && newNetworkStatus.mIsCaptiveNetwork );
    }

    boolean isChanged(DetailNetworkStatus networkStatus) {
        return (mNetworkType != networkStatus.mNetworkType)||
                mIsCaptiveNetwork != networkStatus.mIsCaptiveNetwork ||
                mIsNetworkConnected != networkStatus.mIsNetworkConnected;
    }

    String getNetworkStatusAsString() {
        String networkDetailsStatus = "Network Usable = " + isNetworkUsable() +
                " Detail network status : connected =  " + mIsNetworkConnected +
                " Available =  " + mIsNetworkAvailable +
                " Captive = " + mIsCaptiveNetwork +
                " Type = " + mNetworkType +
                " State = " + mNetworkState +
                " DetailedState = "+ mNetworkDetailedState;
        return networkDetailsStatus;
    }

    NetworkConnectivity.NetworkType getNetworkType()
    {
        return mNetworkType;
    }

    public NetworkInfo.DetailedState getNetworkDetailedState() {
        return mNetworkDetailedState;
    }
}
