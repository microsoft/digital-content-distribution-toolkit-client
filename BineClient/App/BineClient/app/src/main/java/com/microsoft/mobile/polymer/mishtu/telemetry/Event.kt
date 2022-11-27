package com.microsoft.mobile.polymer.mishtu.telemetry

enum class Event(val value:String) {
    APP_INSTALL("AppInstall"),
    ORDER_CREATED("OrderCreated"),
    LOGIN_APP("LoginApp"),
    LOGOUT_APP("LogoutApp"),
    LANGUAGE_SELECTED("LanguageSelected"),
    APP_ACTIVE("AppStarted"),
    APP_BACKGROUND("AppClosed"),
    CONTENT_SEARCH("ContentSearch"),
    CONTENT_VIEW("ContentView"),
    CONTENT_STOP("ContentStop"),
    SHORT_CONTENT_START("ShortContentStart"),
    SHORT_CONTENT_STOP("ShortContentStop"),
    SHORT_FORM_CATEGORY("ShortClipStop"),
    FIREWORK_INIT_STATUS("FireworkInitStatus"),
    NOTIFICATION_RECEIVED("NotificationReceived"),
    NOTIFICATION_CLICKED("NotificationClicked"),
    SCREEN_VIEW("ScreenView"),
    API_LOG("APILog"),
    DOWNLOAD_LOG("ContentDownloadLog"),
    LOGIN_ERROR("LoginError"),
    CONTENT_DOWNLOAD("ContentDownload"),
    SHARE("Share"),
    INTERESTED_OFFLINE_DOWNLOAD("InterestedInOfflineDownload"),
    BOOT_MARKERS("AppBootMarkers")
}