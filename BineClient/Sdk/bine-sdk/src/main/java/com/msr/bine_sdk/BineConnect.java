package com.msr.bine_sdk;

import android.content.Context;
import android.net.wifi.WifiManager;

import androidx.lifecycle.Observer;

import com.msr.bine_sdk.cloud.models.Token;
import com.msr.bine_sdk.hub.FileDownloadException;
import com.msr.bine_sdk.hub.HubConnectionCallback;
import com.msr.bine_sdk.hub.DeviceConnect;
import com.msr.bine_sdk.hub.model.ConnectedHub;
import com.msr.bine_sdk.hub.model.DownloadResponse;
import com.msr.bine_sdk.hub.model.FolderDownloadRequest;
import com.msr.bine_sdk.cloud.models.Hub;
import com.msr.bine_sdk.cloud.models.Source;
import com.msr.bine_sdk.secure.BineSharedPreference;
import com.msr.bine_sdk.secure.KeystoreManager;

import java.io.IOException;

import static android.content.Context.WIFI_SERVICE;

public class BineConnect {
    private DeviceConnect deviceConnect;
    private BineSharedPreference sharedPreference;

    private static BineConnect sharedInstance;

    private Observer<Boolean> logoutObserver;

    private BineConnect(Context context) {
        //Init
        sharedPreference = new BineSharedPreference(context);
        KeystoreManager.init(context);
    }

    public static BineConnect getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new BineConnect(context);
        }
        return sharedInstance;
    }

    private DeviceConnect getHubManager(Context context) {
        if (null == deviceConnect) {
            deviceConnect = new DeviceConnect(context, sharedPreference);
            deviceConnect.setWifiManager((WifiManager) context.getApplicationContext()
                    .getSystemService(WIFI_SERVICE));
        } else {
            deviceConnect.context = context;
        }
        return deviceConnect;
    }

    /**
     * This is used to notify logout when refresh token expires
     * @param logoutObserver
     */
    public void setLogoutObserver(Observer<Boolean> logoutObserver) {
        this.logoutObserver = logoutObserver;
    }

    /**
     * Connect to the provided hub automatically
     *
     * @param context
     * @param hub      {@Hub}
     * @param callback {@HubSocketCallback}
     */
    public void autoConnect(Context context, Hub hub, HubConnectionCallback callback) {
        getHubManager(context).connect(hub, callback);
    }

    /**
     * Returns the connected state to the provided hub
     *
     * @param context - context
     * @param hub - {@Hub}
     * @return
     */
    public ConnectedHub isConnected(Context context, Hub hub) {
        return getHubManager(context).isConnected(hub);
    }

    /**
     * Get IP Address of connected Wifi network
     * @param context - context
     * @return - IP Address
     */
    public String getConnectedWifiIP(Context context) {
        return getHubManager(context).getConnectedWifiIP();
    }

    /**
     * Adds a network config in the wifi settings
     *
     * @param context
     * @param hub
     * @return
     */
    public boolean addNetwork(Context context, Hub hub) {
        return getHubManager(context).addWifiNetwork(hub);
    }

    /**
     * Disconnect from wifi
     *
     * @param context - context
     * @return true if disconnect was successful else false
     */
    public boolean disconnect(Context context) {
        return getHubManager(context).close();
    }

    public Token getToken(Source source) {
        String token = source == Source.HUB ? sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_HUB)
                : sharedPreference.getSecure(BineSharedPreference.KEY_TOKEN_CLOUD);
        String refresh = source == Source.HUB ? sharedPreference.getSecure(BineSharedPreference.KEY_RTOKEN_HUB)
                : sharedPreference.getSecure(BineSharedPreference.KEY_RTOKEN_CLOUD);
        return new Token(refresh, token);
    }

    /*
     * Synchronously Download the requested bulk file
     *
     * @param context - context
     * @param folderDownloadRequest - {@Link FolderDownloadRequest}
     */
    public DownloadResponse downloadBulkfile(Context context,
                                             FolderDownloadRequest folderDownloadRequest) throws IOException, FileDownloadException {
        return getHubManager(context).getBulkFilesOfFolder(getToken(Source.HUB),
                folderDownloadRequest);
    }
}
