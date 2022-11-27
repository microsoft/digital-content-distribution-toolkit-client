package com.microsoft.mobile.polymer.mishtu.storage

import android.content.Context
import android.content.SharedPreferences

import com.microsoft.mobile.polymer.mishtu.BuildConfig

class SharedPreferenceStore{
    lateinit var sharedpreferences: SharedPreferences

    companion object{
        private const val PREFERENCE_NAME = BuildConfig.APPLICATION_ID + ".SharedPrefs"
        const val KEY_USER_LOGGED_IN = BuildConfig.APPLICATION_ID + ".loggedIn"
        const val KEY_USER_ID = BuildConfig.APPLICATION_ID + ".userId"
        const val KEY_CLIENT_USER_ID = BuildConfig.APPLICATION_ID + ".clientUserId"
        const val KEY_REFERRAL_DIALOG_SHOWN = BuildConfig.APPLICATION_ID + "isReferralDialogShown"
        const val KEY_REFERRAL_CODE = BuildConfig.APPLICATION_ID + "referralCode"
        const val KEY_FCM_TOKEN = BuildConfig.APPLICATION_ID + "FCMToken"
        const val KEY_FCM_TOKEN_FAILED = BuildConfig.APPLICATION_ID + "FCMTokenFailed"
        const val KEY_LANGUAGE = BuildConfig.APPLICATION_ID + "Language"
        const val KEY_SHORT_PLAY_TRACKER = BuildConfig.APPLICATION_ID + "ShortForm"

        const val KEY_ORDER_ID = BuildConfig.APPLICATION_ID + "OrderId"
        const val KEY_ACTIVE_SUBSCRIPTION_ID_SVOD = BuildConfig.APPLICATION_ID + "SVOD_SubscriptionId"
        const val KEY_ACTIVE_SUBSCRIPTION_ID_TVOD = BuildConfig.APPLICATION_ID + "TVOD_SubscriptionId"
        const val KEY_FW_CATEGORY = BuildConfig.APPLICATION_ID + "FWCategory"
        const val KEY_DOWNLOAD_INSTRUCTION = BuildConfig.APPLICATION_ID + "DownloadInstructions"
        const val KEY_CONNECTED_HUB = BuildConfig.APPLICATION_ID + "ConnectedHub"
        const val KEY_REWARDS_COINS = BuildConfig.APPLICATION_ID + "RewardsCoins"
        const val KEY_REWARDS_UNLOCKED = BuildConfig.APPLICATION_ID + "RewardsUnlocked"
        const val KEY_AVATAR_INDEX = BuildConfig.APPLICATION_ID + "AvatarIndex"
        const val KEY_LAST_HUB_SCAN_STATUS = BuildConfig.APPLICATION_ID + "HubScanStatus"
        const val KEY_WAS_IN_HUB_AREA = BuildConfig.APPLICATION_ID + "WasInHubArea"
        const val KEY_REDEEM_OFFER_DONE = BuildConfig.APPLICATION_ID + "RedeemOfferDone"
        const val PREFIX_CONTENT_PROVIDER = BuildConfig.APPLICATION_ID + "ContentProviderId_"
        const val SELECTED_CONTENT_PROVIDER = BuildConfig.APPLICATION_ID + "Selected_ContentProviderId_"
        const val NOTIFICATION_ORDER_COMPLETE_SUBSCRIPTIONID = BuildConfig.APPLICATION_ID + "NotificationOrderCompleteProviderId"
        const val NOTIFICATION_ORDER_COMPLETE_PROVIDERID = BuildConfig.APPLICATION_ID + "NotificationOrderCompleteProviderId"
        const val DOWNLOAD_COUNT = BuildConfig.APPLICATION_ID + "DownloadCount"
        const val DOWNLOAD_COUNT_START_DATE = BuildConfig.APPLICATION_ID + "DownloadCountStartDate"
        const val LOGFILE_START_DATE = BuildConfig.APPLICATION_ID + "LogFileStartDate"


        private var instance: SharedPreferenceStore = SharedPreferenceStore()

        fun init(context: Context) {
            instance.sharedpreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        @Synchronized
        fun getInstance(): SharedPreferenceStore {
            return instance
        }
    }

    /**
     * Save key-value pair to SharedPreferences
     */
    fun save(key: String, value: String): Boolean {
        val editor = sharedpreferences.edit()
        editor.putString(key, value)
        editor.apply()
        return true
    }

    /**
     * GET Value stored in SharedPreferences using the pre-defined key
     */
    fun get(key: String): String? {
        return sharedpreferences.getString(key, "")
    }

    fun reset() {
        sharedpreferences.edit().clear().apply()
    }
    fun getAll(): MutableMap<String, *>? {
        return sharedpreferences.all
    }
    fun containsKey(key: String): Boolean {
        return sharedpreferences.contains(key)
    }
}