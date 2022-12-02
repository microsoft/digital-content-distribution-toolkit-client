// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.base

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.msr.bine_android.data.AppDatabase.Companion.getDatabase
import com.msr.bine_android.data.DataRepository.Companion.getInstance
import com.msr.bine_android.data.SharedPreferenceStore
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
open class BaseActivity: AppCompatActivity() {
    //private var progressDialog: FullscreenProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // progressDialog = FullscreenProgressDialog(this)
    }

    fun showProgressDialog() {
       /* if (progressDialog != null) {
            progressDialog!!.show()
        }*/
    }

    fun dismissProgressDialog() {
        /*if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }*/
    }

    override fun attachBaseContext(base: Context) {
        val dataRepository = getInstance(
                getDatabase(base).folderDao(),
                getDatabase(base).contentDao(),
                getDatabase(base).cartDao(),
                getDatabase(base).folderEntityDao(),
                SharedPreferenceStore(base))
        super.attachBaseContext(updateBaseContextLocale(base, dataRepository.getLanguageLocale()))
    }

    fun updateBaseContextLocale(context: Context, language: String?): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else updateResourcesLocaleLegacy(context, locale)
    }

    private fun updateResourcesLocale(context: Context, locale: Locale): Context? {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context? {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}