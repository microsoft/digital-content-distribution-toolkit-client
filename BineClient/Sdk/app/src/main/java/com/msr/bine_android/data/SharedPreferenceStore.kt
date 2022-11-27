package com.msr.bine_android.data

import android.content.Context
import com.msr.bine_android.BuildConfig
import com.msr.bine_sdk.secure.BineSharedPreference

class SharedPreferenceStore(context: Context): BineSharedPreference(context) {

    companion object{
        val KEY_SHAREDPREF_HUB = BuildConfig.APPLICATION_ID + ".connected_hub"
        val KEY_SHAREDPREF_HUB_ID = BuildConfig.APPLICATION_ID + ".connected_hub_id"
        val KEY_SHAREDPREF_USER_ID = BuildConfig.APPLICATION_ID + ".token"
        val KEY_SHAREDPREF_COUPON = BuildConfig.APPLICATION_ID + ".coupon"
        val KEY_SHAREDPREF_FIRST_INSTALL = BuildConfig.APPLICATION_ID + ".install"
        val KEY_SHAREDPREF_FIRST_DOWNLOAD = BuildConfig.APPLICATION_ID + ".first_download"
        val KEY_SHAREDPREF_HUB_SSID = BuildConfig.APPLICATION_ID + ".hup_ssid"
        val KEY_SHAREDPREF_FIRST_LOGIN = BuildConfig.APPLICATION_ID + ".first_login"
        val KEY_SHAREDPREF_LANGUAGE_LOCALE = BuildConfig.APPLICATION_ID + ".language_locale"
        val KEY_SHAREDPREF_TNC_ACCEPTED = BuildConfig.APPLICATION_ID + ".tnc"
        val KEY_SHAREDPREF_MEDIA_HOUSE = BuildConfig.APPLICATION_ID + ".mediaHouse"
        val KEY_SHAREDPREF_DOWNLOAD_TYPE = BuildConfig.APPLICATION_ID + ".downloadType"
        val PREFERENCE_NAME = BuildConfig.APPLICATION_ID + ".SharedPrefs"
        val KEY_REFERRAL_CODE = BuildConfig.APPLICATION_ID + ".code"
        val KEY_LAST_DELETED_DATE = BuildConfig.APPLICATION_ID + ".deleted_date"
        val KEY_SHAREDPREF_IS_CONTENT_FETCHED = BuildConfig.APPLICATION_ID + ".isContentFetched"

    }

    /**
     * Call this method at after app initialise
     */
    init {
        sharedpreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
}