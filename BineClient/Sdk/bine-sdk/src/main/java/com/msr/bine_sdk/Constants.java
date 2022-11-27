package com.msr.bine_sdk;

public class Constants {
    public static final String HUB_GET_FOLDER = "metadata/%s/%s";
    public static final String HUB_DOWNLOAD_FILE = "download/files";
    public static final String REQUEST_GET_HUB_LIST = "hubs_rad?format=json&dist=%f&lat=%f&lng=%f";
    public static final String AUTH_HEADER = "Authorization";

    public static final String HUB_BASE_URL = "http://%s:5000/";
    public static final String HUB_IP = "192.168.2.2";

    public static final String CONTENT_GET_LIST = "list/files/%s";
    public static final String CONTENT_GET_LEAVES = "list/leaves";
    public static final String API_VALIDATE_COUPON = "coupon";
    public static final String API_USER_STATUS = "user_stat";
    public static final String GET_ASSET_TOKEN = "asset_token";
    public static final String HUB_REFRESH_TOKEN = "hub_token_refresh/";
    public static final String CLOUD_REFRESH_TOKEN = "app_token_refresh";
    public static final String ASSET_REFRESH_TOKEN = "asset_token_refresh";
    public static final String CONNECT_HUB = "Connect_hub";
    public static final String VERIFY_COUPON = "hub_voucher";
    public static final String PRE_VERIFY_COUPON = "hub_code_verify";
    public static final int RETRY_COUNT = 3;

    public static final String FOLDER_ID = "FolderID";
    public static final String TITLE = "title";
    public static final String VIDEO_URL = "videoURL";
    public static final String PLAY_BACK_POSITION = "playbackPosition";
    public static final String IS_STREAMING = "isStreaming";

    public static final String ID_NETWORK_CHANNEL = "com.msr.bine.wificonnect";
    public static final String ID_PLAY_CHANNEL = "com.msr.bine.playcontent";
    public static final String ID_PROGRESS_CHANNEL = "com.msr.bine.progress";
    public static final String ID_PROMOCODE_CHANNEL = "com.msr.bine.promocode";
     public static final String ID_PUSH_CHANNEL = "com.msr.bine.cloud";

    public static final String NETWORK_CHANNEL = "Network Connection";
    public static final String PROGRESS_CHANNEL = "Download Progress";
    public static final String PUSH_CHANNEL = "Movie Updates";
    public static final String APP_LOCALE = "app_locale";
    public static final String SCREEN_RECORDING = "screen_recording";
    public static final String SHOW_COUPON = "show_coupon";
    public static final String COUPON_VALUE = "coupon_value";
    public static final String VOUCHER_BROAD_CAST ="voucher_broadcast" ;

    public static final String TEST_MOBILE = "9441101";
    public static final String TEST_MEDIA_HOUSE = "MSR";
    public static final String TEST_TOKEN = "xxxxyyyyxxxyyy";
    public static final String TEST_OTP = "1111";
    public static final double TEST_RADIUS = 30000;
    public static final double TEST_LAT = 17.49;
    public static final double TEST_LONG = 78.0;

    public static final String TEST_HUB_ID="hub_id";
    public static final String TEST_HUB_NAME="TEST";
    public static final String TEST_VOUCHER="TEST";

    public static final String THUMBNAIL_FILENAME = "thumbnail.jpg";
    public static final String VIDEO_FILENAME = "video.mp4";
    public static final String AUDIO_FILENAME = "audio.mp4";
    public static final String MPD_FILENAME = "mpd.mpd";
    public static final String MEDIA_OUTPUT_DIR = "/output/";
    public static final String CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD";
}
