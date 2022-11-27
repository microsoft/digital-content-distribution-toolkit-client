package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentShortInfo
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.msr.bine_sdk.player.BinePlayerViewActivity
import dagger.hilt.android.AndroidEntryPoint
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.OnDismissCallback
import com.msr.bine_sdk.models.Error
import com.msr.bine_sdk.network_old.Connectivity
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class CustomBinePlayerActivity : BinePlayerViewActivity() {

    lateinit var content: Content
    private var countdownTimer: CountDownTimer? = null
    private val deviceViewModel by viewModels<DeviceViewModel>()
    private val formatter = SimpleDateFormat(BNConstants.formatter, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_exo_player_view)
        super.onCreate(savedInstanceState)
        content = intent.getSerializableExtra("Content") as Content
        val shareBtn: ImageButton = findViewById(R.id.btn_share)
        shareBtn.setOnClickListener {
            AppUtils.shareContent(content.title,
                content.contentId,
                content.contentProviderId,
                AnalyticsLogger.LongForm,
                this)
        }
        val infoIconBtn: ImageButton = findViewById(R.id.exo_btn_info)
        val offlineDownloadPromoCardContainer: ConstraintLayout = findViewById(R.id.offline_download_promocard_container)

        infoIconBtn.setOnClickListener {
            showContentDetail(content)
        }
        val cardInfoText: TextView = findViewById(R.id.offline_card_info_text)
        cardInfoText.text = getSpannable(this.getString(R.string.find_a_nearest_shop_and_save_data),
            this.getString(R.string.up_to_100))

        val hubScanStatus = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_LAST_HUB_SCAN_STATUS)
        val status =
            if (hubScanStatus.isNullOrEmpty()) HubScanBaseActivity.HubScanStatus.PERMISSION_DENIED
            else {
                HubScanBaseActivity.HubScanStatus.valueOf(hubScanStatus)
            }

        val saveDataCloseBtn: TextView = findViewById(R.id.savedata_close)
        val goToShopBtn: Button = findViewById(R.id.button_go_to_store)
        saveDataCloseBtn.setOnClickListener {
            offlineDownloadPromoCardContainer.visibility = View.GONE
        }
        goToShopBtn.setOnClickListener {
            finish()
            AppUtils.showNearbyStore(this@CustomBinePlayerActivity, content, true, null, null)
        }

        val downloadBtn: ImageButton = findViewById(R.id.btn_downlowd)
        if (status != HubScanBaseActivity.HubScanStatus.HUB_EXIST)  downloadBtn.visibility = View.GONE

        when(intent.getBooleanExtra(extraShouldShowDownloads, false)) {
            false -> {
                downloadBtn.visibility = View.GONE
            }
            true -> {
                if(status != HubScanBaseActivity.HubScanStatus.NO_HUB_EXIST && shouldShowSaveDataDialog()) {
                    SharedPreferenceStore.getInstance().save(saveDataDialogLastShown, formatter.format(Date()))
                    showSaveDataDialog(offlineDownloadPromoCardContainer)
                }
                downloadClicked(downloadBtn)
            }
        }
        animateDownload(downloadBtn)
    }

    private fun shouldShowSaveDataDialog(): Boolean {
        val dateString = SharedPreferenceStore.getInstance().get(saveDataDialogLastShown)
        if (dateString.isNullOrEmpty()) {
            return true
        }
        else {
            try {
                val date = formatter.parse(dateString)
                date?.let {
                    val difference: Long = Date().time - it.time
                    val hours = difference / (60 * 60 * 1000)
                    if (hours >= 24) return true
                }
            }
            catch (e: Exception) {
                return true
            }
        }
        return false
    }

    private fun downloadClicked(downloadBtn: ImageButton) {
        downloadBtn.setOnClickListener {
            deviceViewModel.getConnectedDevice()?.let {
                val intent = Intent()
                intent.putExtra(extraDownloadClicked, true)
                this.setResult(-1, intent)
                finish()
            } ?: run {
                if(AppUtils.shouldShowDownloadInstructions()) {
                    val intent = Intent()
                    intent.putExtra(extraDownloadClicked, true)
                    this.setResult(-1, intent)
                    finish()
                }
                else {
                    AppUtils.showNearbyStore(this@CustomBinePlayerActivity, content, true, null, null)
                }
            }
        }
    }

    private fun showSaveDataDialog(offlineDownloadPromoCardContainer: ConstraintLayout) {
        countdownTimer = object : CountDownTimer(10000, 10000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("Custom", "$millisUntilFinished _ ")
            }

            override fun onFinish() {
                pausePlayer()
                offlineDownloadPromoCardContainer.visibility = View.VISIBLE
            }
        }
        countdownTimer?.start()
    }

    private fun showContentDetail(content: Content) {
        pausePlayer()
        val bottomSheetContentDetail = BottomSheetContentShortInfo(content)
        bottomSheetContentDetail.show(supportFragmentManager, bottomSheetContentDetail.tag)
    }

    private fun getSpannable(string: String, boldText: String): SpannableString {
        val txtSpannable = SpannableString(string)
        if (boldText.length > string.length) return txtSpannable
        if (string.indexOf(boldText) == -1) return txtSpannable
        txtSpannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(this,
                    R.color.offline_highlighted_color)), string.indexOf(boldText), string.indexOf(boldText) + boldText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return txtSpannable
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        super.onDestroy()
    }

    companion object {
        const val extraDownloadClicked = "DownloadClicked"
        const val saveDataDialogLastShown = "SaveDataDialogLastShown"
        const val extraShouldShowDownloads = "ShouldShowDownloads"
    }

    private fun animateDownload(imageButton: ImageButton) {
        if(deviceViewModel.isConnectionActive()) {
            imageButton.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in_out_download))
        }
    }

    override fun fetchTokenFailedForKeyExpiry(error: Error?, message: String?) {
        super.fetchTokenFailedForKeyExpiry(error, message)

        if (!Connectivity.isConnected(this)) {
            Handler(Looper.getMainLooper()).post {
                showLicenseExpiredOfflineDialog()
                AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "TokenFailedForKeyExpiry", "UserOffline", message)
            }
        }
        else {
            AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "TokenFailedForKeyExpiry", error?.name, message)
        }
    }

    override fun onPlaybackFailed(error: Error?, message: String?) {
        super.onPlaybackFailed(error, message)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Error playing content - $message", Toast.LENGTH_LONG).show()
        }
        AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "PlaybackFailed", error?.name, message)
    }

    //For localization, since this activity is subclassing from SDK
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

    private fun showLicenseExpiredOfflineDialog() {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.ic_refresh,
                null,
                getString(R.string.key_expired_error),
                true,
                getString(R.string.button_title_mobile_data),
                null,
                {
                    startActivity(Intent(Settings.ACTION_SETTINGS)
                        .setAction(Settings.ACTION_WIRELESS_SETTINGS))
                    finish()
                },
                null,null)
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.setOnDismissListener(object: OnDismissCallback{
            override fun onDismiss() {
                finish()
            }
        })
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }
}