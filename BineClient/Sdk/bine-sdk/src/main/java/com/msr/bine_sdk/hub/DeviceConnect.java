package com.msr.bine_sdk.hub;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.msr.bine_sdk.hub.model.ConnectedHub;
import com.msr.bine_sdk.hub.model.DownloadResponse;
import com.msr.bine_sdk.hub.model.FolderDownloadRequest;
import com.msr.bine_sdk.cloud.models.Token;
import com.msr.bine_sdk.cloud.models.Hub;
import com.msr.bine_sdk.network_old.NetworkAuthService;
import com.msr.bine_sdk.network_old.Request;
import com.msr.bine_sdk.secure.BineSharedPreference;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Response;

import static android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS;

public class DeviceConnect {
    private static final String TAG = DeviceConnect.class.getSimpleName();
    private static final String QUOTE = "\"";

    public Context context;
    private WifiManager wifiManager;

    private final NetworkAuthService networkService;
    private final TokenAuthenticator hubTokenAuthenticator;

    public DeviceConnect(Context context, BineSharedPreference sharedPreference) {
        this.context = context;
        hubTokenAuthenticator = new TokenAuthenticator(sharedPreference);
        networkService = new NetworkAuthService();
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public void connect(Hub hub, HubConnectionCallback callback) {
        connectWifi(hub, callback);
    }

    public ConnectedHub isConnected(Hub hub) {
        if (wifiManager == null) {
            return null;
        }

        ConnectedHub connectedHub = new ConnectedHub(hub);
        boolean is5GSupported = isRunningTest() || wifiManager.is5GHzBandSupported();
        if (is5GSupported && isWifiConnected("\"" + hub.wifi5GSSID + "\"", wifiManager)) {
            connectedHub.SSID = hub.wifi5GSSID;
            return connectedHub;
        }
        if (isWifiConnected("\"" + hub.wifi2GSSID + "\"", wifiManager)) {
            connectedHub.SSID = hub.wifi2GSSID;
            return connectedHub;
        }
        return null;
    }

    public String getConnectedWifiIP() {
        if (wifiManager == null) {
            return "";
        }
        return getIPAddress(wifiManager.getConnectionInfo());
    }

    public boolean addWifiNetwork(Hub hub) {
        if (wifiManager == null) {
            return false;
        }
        String ssid = isRunningTest() ?  hub.wifi5GSSID  : (wifiManager.is5GHzBandSupported() ? hub.wifi5GSSID : hub.wifi2GSSID);
        String password = isRunningTest() ?  hub.wifi5GPass  : (wifiManager.is5GHzBandSupported() ? hub.wifi5GPass : hub.wifi2GPass);
        if (ssid == null || password == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
            if (configurations == null){
                return false;
            }
            for (WifiConfiguration configuration : configurations) {
                Log.e(TAG, "SAVED NET ID: " + configuration.SSID + " " + configuration.preSharedKey +
                        " desired SSID: " + ssid);
                if (configuration.SSID == null) {
                    continue;
                }
                if (configuration.SSID.toLowerCase().replaceAll("\"", "").equals(ssid.toLowerCase())) {
                    Log.i(TAG, "Found matching mSSID with netId: " + configuration.networkId);
                    wifiManager.removeNetwork(configuration.networkId);
                }
            }
            int netId = wifiManager.addNetwork(generateWPA2NetworkConfiguration(ssid, password));
            return wifiManager.enableNetwork(netId, false);
        } else {
            WifiNetworkSuggestion wifiNetwork = new WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(hub.name)
                    .build();

            List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
            suggestions.add(wifiNetwork);
            int status = wifiManager.addNetworkSuggestions(suggestions);

            return status == STATUS_NETWORK_SUGGESTIONS_SUCCESS;
        }
    }

    public boolean close() {
        if (wifiManager == null) {
            return false;
        }
        return wifiManager.disconnect();
    }

    public DownloadResponse getBulkFilesOfFolder(
            Token token,
            FolderDownloadRequest folderDownloadRequest) throws IOException, FileDownloadException {
        HashMap<String, String> queryParams = new HashMap<>();
        if (folderDownloadRequest.mediaHouse != null)
            queryParams.put("mediaHouse", folderDownloadRequest.mediaHouse);
        if (folderDownloadRequest.path != null) queryParams.put("path", folderDownloadRequest.path);
        if (folderDownloadRequest.bulkFileName != null)
            queryParams.put("file", folderDownloadRequest.bulkFileName);

        Request request = new Request(folderDownloadRequest.hubUrl,
                folderDownloadRequest.endpoint,
                null,
                queryParams,
                null,
                token);
        Response response = networkService.getSync(request, hubTokenAuthenticator);

        if (response.isSuccessful()) {
            return new DownloadResponse(Objects.requireNonNull(response.body()).byteStream(),
                    Long.parseLong(Objects.requireNonNull(response.headers().get("Content-Length"))));
        } else {
            throw new FileDownloadException(response.message(), response.code());
        }
    }


    //Private Methods
    private void connectWifi(Hub hub, HubConnectionCallback callback) {
        if (wifiManager == null) {
            return;
        }

        String ssid = isRunningTest() ? hub.wifi5GSSID :  (wifiManager.is5GHzBandSupported() ? hub.wifi5GSSID : hub.wifi2GSSID);
        String password = isRunningTest() ?  hub.wifi5GPass : (wifiManager.is5GHzBandSupported() ? hub.wifi5GPass : hub.wifi2GPass);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + ssid + "\"";
                wifiConfig.preSharedKey = "\"" + password + "\"";
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                Thread.sleep(3 * 1000);
                if (isWifiConnected("\"" + ssid + "\"", wifiManager)) {
                    Log.d("TAG", "Connected");
                }
                /*NotificationHelper.getInstance(context).show("",
                        String.format("Connected to %s", hub.name),
                        Constants.ID_NETWORK_CHANNEL);*/
                callback.onConnect(hub);
            } catch (Exception e) {
                callback.onConnectFailure(hub, e);
            }
        } else {
            WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                cm.requestNetwork(networkRequest, networkCallback);
                //TODO: test with > Q
                callback.onConnect(hub);
            }
        }
    }

    private boolean isWifiConnected(String ssid, WifiManager wifiManager) {
        if (wifiManager.isWifiEnabled()) {
            return wifiManager.getConnectionInfo().getSSID().equals(ssid);
        }
        return false;
    }

    private String getIPAddress(WifiInfo wifiInfo) {
        int ipAddress = wifiInfo.getIpAddress();
        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }
        return ipAddressString;
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            /*NotificationHelper.getInstance(context).show("",
                    String.format("Connected to %s", network.toString()),
                    Constants.ID_NETWORK_CHANNEL);*/
        }
    };

    private WifiConfiguration generateWPA2NetworkConfiguration(String ssid, String password) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = QUOTE + ssid + QUOTE;
        wifiConfiguration.preSharedKey = QUOTE + password + QUOTE;
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.priority = 40;
        return wifiConfiguration;
    }

    private static AtomicBoolean isRunningTest;
    public static synchronized boolean isRunningTest () {
        if (isRunningTest == null) {
            boolean istest;
            try {
                Class.forName ("com.msr.bine_sdk.base.BaseTest");
                istest = true;
            } catch (ClassNotFoundException e) {
                istest = false;
            }
            isRunningTest = new AtomicBoolean(istest);
        }
        return isRunningTest.get();
    }
}
