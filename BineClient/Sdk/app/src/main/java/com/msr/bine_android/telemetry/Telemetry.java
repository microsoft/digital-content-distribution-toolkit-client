package com.msr.bine_android.telemetry;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.microsoft.appcenter.analytics.Analytics;
import com.msr.bine_android.BuildConfig;
import com.msr.bine_android.data.DataRepository;
import com.msr.bine_android.utils.AppUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Telemetry {
    private static String TAG = Telemetry.class.getSimpleName();
    private static String KEY_APP_ACTIVE = BuildConfig.APPLICATION_ID + ".app_active";
    private static String KEY_HUB_CONNECTED = BuildConfig.APPLICATION_ID + ".hub_connect";
    private static String KEY_APP_LOGIN = BuildConfig.APPLICATION_ID + ".app_login";
    private static String KEY_PLAY_CONTENT = BuildConfig.APPLICATION_ID + ".play_content";

    private static String ID = "Id";
    private static String ASSET_ID = "AssetId";
    private static String HUB_ID = "DeviceId";
    private static String DEVICE_ID = "PhoneDeviceId";

    private static Telemetry sharedInstance;
    private DataRepository dataRepository;
    private String deviceId;

    public static Telemetry getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new Telemetry();
        }
        return sharedInstance;
    }
    public void init(Context context, DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private HashMap<String, String> addDefaultParams( HashMap<String, String> params) {
        params.put("AndroidOS", Build.VERSION.RELEASE);
        params.put("PhoneModel", Build.MODEL);
        params.put("UserId", dataRepository.getUserId() == null ? "" : dataRepository.getUserId());
        params.put(DEVICE_ID, deviceId);
        params.put("MessageType", "Telemetry");
        params.put("Timestamp", String.valueOf(new Date().getTime()));
        return params;
    }

    private void send(String event, HashMap<String, String> message) {
        if (BuildConfig.ENABLE_ANALYTICS) {
            Analytics.trackEvent(event, message);
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Sending Telemetry - "+ event + ":" + message);
        }
    }

    private void send(Events event, HashMap<String, String> message) {
        send(event.getValue(), message);
    }

    private void sendMessage(Events event, HashMap<String, String> params) {
        addDefaultParams(params);
        send(event, addDefaultParams(params));
    }

    private void sendMessage(Events event) {
        HashMap<String, String> params = new HashMap<>();
        send(event, addDefaultParams(params));
    }

    public void sendContentDownload(long startTime,
                                    String folderId,
                                    long size) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Duration", String.valueOf(new Date().getTime() - startTime));
        params.put(ASSET_ID, folderId);
        params.put(HUB_ID, dataRepository.getHub() == null ? "" : dataRepository.getHub().id);
        params.put("Count", "1");
        params.put("AssetSize", String.valueOf(size));
        params.put(ID, dataRepository.getTelemetryId(KEY_HUB_CONNECTED));
        if (dataRepository.getHub() != null) {
            /*ConnectedHub connectedHub = dataRepository.getHub();
            params.put("HubSSID", connectedHub.SSID);*/
        }
        sendMessage(Events.DOWNLOAD_MOVIE, params);
    }

    public void sendPlayContent(String folderId) {
        String uuid = UUID.randomUUID().toString();

        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        params.put(ASSET_ID, folderId);
        sendMessage(Events.PLAY_MOVIE, params);

        dataRepository.saveTelemetryId(KEY_PLAY_CONTENT, uuid);
    }

    public void sendPauseContent(String folderId) {
        String uuid = dataRepository.getTelemetryId(KEY_PLAY_CONTENT);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        params.put(ASSET_ID, folderId);
        sendMessage(Events.PAUSE_MOVIE, params);
    }

    public void sendDeleteContent(String folderId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(ASSET_ID, folderId);
        sendMessage(Events.DELETE_MOVIE, params);
    }

    public void sendGenerateVoucher(String hubId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(HUB_ID, hubId);
        params.put(ID, dataRepository.getTelemetryId(KEY_HUB_CONNECTED));
        sendMessage(Events.GENERATE_CODE, params);
    }

    public void sendAppLogin() {
        String uuid = UUID.randomUUID().toString();

        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        sendMessage(Events.LOGIN_APP, params);

        dataRepository.saveTelemetryId(KEY_APP_LOGIN, uuid);
    }
    public void sendAppLogout() {
        String uuid = dataRepository.getTelemetryId(KEY_APP_LOGIN);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        sendMessage(Events.LOGOUT_APP, params);
    }

    public void sendAppInstall() {
        sendMessage(Events.APP_INSTALL);
    }

    public void sendAppInForeground() {
        if (TextUtils.isEmpty(dataRepository.getUserId())) {
            return;
        }
        String uuid = UUID.randomUUID().toString();

        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        sendMessage(Events.APP_ACTIVE, params);

        dataRepository.saveTelemetryId(KEY_APP_ACTIVE, uuid);
    }

    public void sendAppInBackground() {
        if (TextUtils.isEmpty(dataRepository.getUserId())) {
                return;
        }
        String uuid = dataRepository.getTelemetryId(KEY_APP_ACTIVE);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(ID, uuid);
        sendMessage(Events.APP_BACKGROUND, params);
    }

    public void sendLanguageSelected(Context context) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Language", AppUtils.languageForLocale(dataRepository.getLanguageLocale(), context));
        sendMessage(Events.LANGUAGE_SELECTED, params);
    }

    public void sendHubConnectStatus(String hubId, boolean connected) {
        String uuid;
        if (connected) {
            uuid =  UUID.randomUUID().toString();
            dataRepository.saveTelemetryId(KEY_HUB_CONNECTED, uuid);
        }
        else{
            uuid = dataRepository.getTelemetryId(KEY_HUB_CONNECTED);
            if (TextUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(HUB_ID, hubId);
        params.put(ID, uuid);
        sendMessage(connected ? Events.HUB_CONNECT : Events.HUB_DISCONNECT, params);
    }

    public void sendDownloadClicked(String folderId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(ASSET_ID, folderId);
        params.put(HUB_ID, dataRepository.getHubId());
        sendMessage(Events.DOWNLOAD_CLICKED, params);
    }
    public void sendDownloadLicenseStart() {
        sendMessage(Events.DOWNLOAD_DRM_LICENSE);
    }
    public void sendDownloadLicenseDone() {
        sendMessage(Events.DOWNLOAD_DRM_LICENSE_DONE);
    }

    public void sendAssetTokenReceived(String token) {
        HashMap<String, String> params = new HashMap<>();
        params.put("TOKEN", token);
        sendMessage(Events.ASSET_TOKEN, params);
    }
    public void sendAssetTokenRefreshed(String token) {
        HashMap<String, String> params = new HashMap<>();
        params.put("TOKEN", token);
        sendMessage(Events.ASSET_TOKEN_REFRESH, params);
    }

    public void sendHubTokenReceived(String token) {
        HashMap<String, String> params = new HashMap<>();
        params.put("TOKEN", token);
        sendMessage(Events.HUB_TOKEN, params);
    }
    public void sendHubTokenRefreshed(String token) {
        HashMap<String, String> params = new HashMap<>();
        params.put("TOKEN", token);
        sendMessage(Events.HUB_TOKEN_REFRESH, params);
    }

    public void sendFindOnBoothClicked() {
        sendMessage(Events.FIND_ON_BOOTH);
    }

    public void sendUserStats(String folderId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(ASSET_ID, folderId);
        params.put(HUB_ID, dataRepository.getHubId());
        sendMessage(Events.USER_STATS, params);
    }

    public void sendFailureEvent(String type, String exception) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Type", type);
        String hubID = dataRepository.getHubId();
        params.put("Exception", type + ": " + exception + "|" + hubID);
        sendMessage(Events.FAILURE_EXCEPTION, params);
    }

    public void sendHubContentFetch(String Id, int count) {
        HashMap<String, String> params = new HashMap<>();
        params.put(HUB_ID, Id + "|Contents:" + count);
        sendMessage(Events.HUB_CONTENT_FETCH, params);
    }
    public void sendContentFilterSearch(String filter) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Filter", filter);
        sendMessage(Events.CONTEXT_FILTER_SEARCH, params);
    }
    public void sendContentTextSearch(String keyword) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Filter", keyword);
        sendMessage(Events.CONTENT_TEXT_SEARCH, params);
    }

    public void sendContentSelected(String filter, String keyword, String folderId) {
        String param = String.format("%s, %s, %s", folderId,
                filter.isEmpty() ? "" : filter,
                keyword.isEmpty() ? "" : "Query:" + keyword);

        HashMap<String, String> params = new HashMap<>();
        params.put("Filter", param);
        sendMessage(Events.CONTEXT_SELECTED, params);
    }

    public void clearIds() {
        dataRepository.saveTelemetryId(KEY_APP_ACTIVE, "");
    }

    public void sendCustomEvent(String key, HashMap<String, String> params) {
        send(key, addDefaultParams(params));
    }
}
