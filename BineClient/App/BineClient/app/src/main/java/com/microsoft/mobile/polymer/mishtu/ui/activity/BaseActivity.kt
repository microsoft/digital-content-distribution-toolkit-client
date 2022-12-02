// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentProvider
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetOrderComplete
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.OrderCompleteEvent
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    lateinit var dialog: AlertDialog
    private lateinit var dialogNoInternet: Dialog
    private lateinit var dialogServiceError: Dialog
    private var mActivityVisible = false
    val cpIdToActiveSubscriptionMap = HashMap<String, ActiveSubscription>()
    val cpIdToContentProviderMap = HashMap<String, ContentProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setProgressDialog()
        // Has no instance showing this as dialogs hence commenting the code
        //setNoInternetDialog()
        //setserviceUnavailableDialog()
    }

    override fun onResume() {
        super.onResume()
        mActivityVisible = true
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        mActivityVisible = false
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    open fun isActivityVisible(): Boolean {
        return mActivityVisible
    }

    fun finishActivityOnBackPressed(){
        dialogNoInternet.setOnCancelListener {
            finish()
        }
        dialogServiceError.setOnCancelListener {
            finish()
        }
    }
    /*private fun setserviceUnavailableDialog() {
        dialogNoInternet = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialogNoInternet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogNoInternet.setContentView(R.layout.dialog_no_internet_connection)
        dialogNoInternet.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    private fun setNoInternetDialog() {
        dialogServiceError = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialogServiceError.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogServiceError.setContentView(R.layout.dialog_server_error)
        dialogServiceError.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }*/

    open fun setProgressDialog() {
        val llPadding = 30
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(llPadding, llPadding, llPadding, llPadding)
        linearLayout.gravity = Gravity.CENTER
        var layoutParams1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams1.gravity = Gravity.CENTER
        linearLayout.layoutParams = layoutParams1
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = layoutParams1
        layoutParams1 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams1.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = getString(R.string.please_wait)
        tvText.setTextColor(ContextCompat.getColor(this, R.color.black))
        tvText.textSize = 18f
        tvText.setTypeface(ResourcesCompat.getFont(this, R.font.mukta_medium))
        tvText.layoutParams = layoutParams1
        linearLayout.addView(progressBar)
        linearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_light))
        linearLayout.addView(tvText)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(linearLayout)
        dialog = builder.create()

        dialog.window?.let {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(it.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }

    open fun showProgress() {
        if (!dialog.isShowing) dialog.show()
    }

    open fun hideProgress() {
        if (dialog.isShowing) dialog.dismiss()
    }

    /*fun showNoInternetConnection() {
        dialogNoInternet.show()
    }

    fun showServiceError() {
        dialogServiceError.show()
    }*/

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base, AppUtils.getLanguageToShow()))
    }

    private fun updateBaseContextLocale(context: Context, language: String): Context? {
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

    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOrderCompleted(event: OrderCompleteEvent) {
        val bottomSheet = BottomSheetOrderComplete(event.subscriptionId)
        bottomSheet.show(supportFragmentManager,bottomSheet.tag)
    }


}