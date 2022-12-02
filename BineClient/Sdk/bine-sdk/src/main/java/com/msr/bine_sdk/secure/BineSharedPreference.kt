// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.secure

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import com.msr.bine_sdk.BuildConfig
import com.msr.bine_sdk.Constants
import javax.inject.Inject

open class BineSharedPreference @Inject constructor(context: Context) {
    val TAG =  BineSharedPreference::class.java.simpleName

    companion object{
        const val PREFERENCE_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".SharedPrefs"
        const val KEY_TOKEN_HUB = BuildConfig.LIBRARY_PACKAGE_NAME + ".token_hub"
        const val KEY_TOKEN_CLOUD = BuildConfig.LIBRARY_PACKAGE_NAME + ".token_cloud"
        const val KEY_RTOKEN_HUB = BuildConfig.LIBRARY_PACKAGE_NAME + ".rtoken_hub"
        const val KEY_RTOKEN_CLOUD = BuildConfig.LIBRARY_PACKAGE_NAME + ".rtoken_cloud"
        const val KEY_TOKEN_ASSET = BuildConfig.LIBRARY_PACKAGE_NAME + ".token_asset"
        const val KEY_RTOKEN_ASSET = BuildConfig.LIBRARY_PACKAGE_NAME + ".rtoken_asset"

        const val KEY_HUB_IP = BuildConfig.LIBRARY_PACKAGE_NAME + ".hubIP"
        const val KEY_PARALLEL_COUNT = BuildConfig.LIBRARY_PACKAGE_NAME + ".parallelCount"
    }

    var sharedpreferences: SharedPreferences? = null

    /**
     * Call this method at after app initialise
     */
    init {
        sharedpreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save key-value pair to SharedPreferences
     */
    fun save(key: String, value: String): Boolean {
        if (sharedpreferences == null) {
            Log.d(TAG, "SharedPreferences not initialised")
            return false
        }
        val editor = sharedpreferences!!.edit()
        editor.putString(key, value)
        editor.apply()
        return true
    }

    /**
     * GET Value stored in SharedPreferences using the pre-defined key
     */
    fun get(key: String): String? {
        if (sharedpreferences == null) {
            Log.d(TAG, "SharedPreferences not initialised")
            return null
        }
        return sharedpreferences!!.getString(key, "")
    }

    /**
     * GET Value stored in SharedPreferences, if empty returns default specified
     */
    fun getDefaultIfNull(key: String): String {
        if (sharedpreferences == null) {
            Log.d(TAG, "SharedPreferences not initialised")
            return ""
        }
        val value = sharedpreferences!!.getString(key, "")
        if(TextUtils.isEmpty(value)) {
            if (key == KEY_HUB_IP) return Constants.HUB_IP
        }
        return value ?: ""
    }

    fun saveSecure(key: String, value: String): Boolean {
        if (sharedpreferences == null) {
            Log.d(TAG, "SharedPreferences not initialised")
            return false
        }
        val encryptedValue = KeystoreManager.encrypt(value);
        val editor = sharedpreferences!!.edit()
        editor.putString(key, encryptedValue)
        editor.apply()
        return true
    }

    /**
     * GET Value stored in SharedPreferences using the pre-defined key
     */
    fun getSecure(key: String): String? {
        if (sharedpreferences == null) {
            Log.d(TAG, "SharedPreferences not initialised")
            return ""
        }
        val encryptedVal = sharedpreferences!!.getString(key, "")
        if (encryptedVal == null || encryptedVal.isEmpty()) return encryptedVal

        return KeystoreManager.decrypt(encryptedVal)
    }

    fun reset() {
        sharedpreferences?.edit()?.clear()?.apply()
    }
}