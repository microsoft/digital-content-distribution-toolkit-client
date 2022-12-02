// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.telemetry

enum class Events(val value:String) {
    APP_INSTALL("AppInstall"),
    APP_UNINSTALL("AppUninstall"),
    DOWNLOAD_MOVIE("ContentDownload"),
    PLAY_MOVIE("PlayMovie"),
    PAUSE_MOVIE("PauseMovie"),
    ORDER_MOVIE("OrderMovie"),
    GENERATE_CODE("GenerateVoucherCode"),
    DELETE_MOVIE("DeleteMovie"),
    LOGIN_APP("LoginApp"),
    LOGOUT_APP("LogoutApp"),
    LANGUAGE_SELECTED("LanguageSelected"),
    HUB_CONNECT("HubConnect"),
    HUB_DISCONNECT("HubDisconnect"),
    APP_ACTIVE("AppStarted"),
    APP_BACKGROUND("AppClosed"),
    FIND_ON_BOOTH("FindOnBooth"),
    DOWNLOAD_CLICKED("DownloadBegin"),
    DOWNLOAD_DRM_LICENSE("DownloadLicenseStarted"),
    DOWNLOAD_DRM_LICENSE_DONE("DownloadLicenseDone"),
    ASSET_TOKEN("AssetToken"),
    ASSET_TOKEN_REFRESH("AssetTokenRefresh"),
    HUB_TOKEN("HubToken"),
    HUB_TOKEN_REFRESH("HubTokenRefresh"),
    USER_STATS("UserStats"),
    HUB_CONTENT_FETCH("HubContentFetch"),
    CONTENT_TEXT_SEARCH("ContentTextSearch"),
    CONTEXT_FILTER_SEARCH("ContentFilterSearch"),
    CONTEXT_SELECTED("ContentSelected"),
    FAILURE_EXCEPTION("FailureException")
}

//• Phone model and OS version - DONE
//• Clicking download movie button
//• Clicking play movie button - DONE
//• App installation Date - DONE
//• App un-installation Date
//• Event to order a movie on a Hub - NA
//• Event to generate voucher code
//• Time taken to download a movie along with movie size //TODO: Add Movie Size
//• Event when movie is deleted from storage (along with asset id) - DONE
//• Logout event from the Applications - DONE

//TODO:
//Hub IDs accessed
//Language selected
