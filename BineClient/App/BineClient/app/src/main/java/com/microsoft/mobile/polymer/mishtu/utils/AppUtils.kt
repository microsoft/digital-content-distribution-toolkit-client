// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Typeface
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.tabs.TabLayout
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.ui.activity.*
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentDetails
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetPayAtStore
import com.microsoft.mobile.polymer.mishtu.ui.activity.SubscriptionPackListActivity
import com.msr.bine_sdk.hub.model.DownloadStatus
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class AppUtils {
    companion object {

        fun getNextIntent(context: Context): Intent {
            val userId: String? =
                SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_USER_ID)
            if (userId.isNullOrEmpty()) {
                return Intent(context, PhoneLoginActivity::class.java)
            }
            else {
                val inHubArea = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_LAST_HUB_SCAN_STATUS)
                if (inHubArea.isNullOrEmpty()) {
                    return Intent(context, LocationCaptureActivity::class.java)
                }
                return Intent(context, MainActivity::class.java)
            }
        }

        private fun checkIsPermissionsEnabled(context: Context): Boolean {
            val permissionsRequired = arrayOf(
                permission.ACCESS_FINE_LOCATION,
                permission.READ_CONTACTS,
                permission.WRITE_EXTERNAL_STORAGE,
                permission.ACCESS_COARSE_LOCATION)
            for (s in permissionsRequired) {
                if(ActivityCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED)
                    return false
            }
            return true
        }

        fun isOreoOrAbove(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        fun loadImage(context: Context, imageUrl: String, imageView: ImageView) {
            val cornerRadius = 20
            val transformations = mutableListOf<Transformation<Bitmap>>(CenterCrop())
            if (cornerRadius > 0) transformations.add(RoundedCorners(cornerRadius))

            Glide.with(context)
                .load(imageUrl)
                .dontAnimate()
                .placeholder(R.drawable.bg_shimmer_gradient)
                .into(imageView)
        }

        fun loadImageWithRoundedCorners(context: Context, imageUrl: String?, imageView: ImageView) {
            val cornerRadius = 20
            val transformations = mutableListOf<Transformation<Bitmap>>(CenterCrop())
            if (cornerRadius > 0) transformations.add(RoundedCorners(cornerRadius))

            imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(imageUrl)
                .dontAnimate()
                .placeholder(R.drawable.bg_shimmer_gradient)
                .apply {
                    if (imageView.width > 0 && imageView.height > 0) {
                        this.override(imageView.width, imageView.height)
                    }
                    this.transform(*transformations.toTypedArray())
                }
                .into(imageView)
        }

        fun dipToPx(context: Context, dipValue: Float): Float {
            val density = context.resources.displayMetrics.density
            return dipValue * density
        }

        fun showDialog(context: Context,
            title: String,
            message: String,
            positiveButton: String,
            negativeButton: String,
                       dialogClickListener: DialogInterface.OnClickListener) {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, dialogClickListener)
                .setNegativeButton(negativeButton, dialogClickListener)
                .show()
        }

        fun showConfirmRedeemOfferDialog(context: Context,
            subscriptionPack: SubscriptionPack,
                                         dialogClickListener: DialogInterface.OnClickListener) {
            showDialog(context, context.getString(R.string.redeem_now),
                String.format(context.getString(R.string.confirm_redeem_order, subscriptionPack.durationDays)),
                context.getString(R.string.bn_proceed),
                context.getString(R.string.bn_cancel), dialogClickListener)
        }

        fun analyseStorage(context: Context) {
            val appBaseFolder: File = context.filesDir.parentFile
            val totalSize = browseFiles(appBaseFolder)
            Log.d("STORAGE_TAG", "App uses ${toMB(totalSize)} total bytes")
        }

        private fun browseFiles(dir: File): Long {
            var dirSize: Long = 0
            for (f in dir.listFiles()) {
                if (f.isDirectory) {
                    dirSize += browseFiles(f)
                    continue
                }
                dirSize += f.length()
                Log.d("STORAGE_TAG_FILE",
                    dir.absolutePath
                        .toString() + "/" + f.name + " uses " + toMB(f.length()) + " bytes"
                )
            }
            Log.d("STORAGE_TAG", dir.absolutePath.toString() + " uses " + toMB(dirSize) + " bytes")
            return dirSize
        }

        fun toMB(b: Long): String {
            var bytes = b
            if (bytes < 1024)
                return "$bytes bytes"

            bytes /= 1024
            if (bytes < 1024)
                return "$bytes  KB"

            bytes /= 1024
            if (bytes < 1024)
                return "$bytes MB"

            bytes /= 1024
            if (bytes < 1024)
                return "$bytes GB"

            return "$bytes"
        }

        fun setStyleForTab(context: Context, tab: TabLayout.Tab, style: Int) {
            tab.view.children.find {
                it is TextView
            }?.let { tv ->
                (tv as TextView).post {
                    val typeface: Typeface? = ResourcesCompat.getFont(context, R.font.mukta_medium)
                    if (style == Typeface.BOLD) {
                        tv.setTextColor(ContextCompat.getColor(context, R.color.tab_selected_color))
                    }
                    else {
                        tv.setTextColor(ContextCompat.getColor(context, R.color.tab_unselected_color))
                    }
                    tv.setTypeface(typeface, style)
                }
            }
        }

        fun getLanguageToShow(): String {
            val languageEntryValue = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_LANGUAGE)
            if (!languageEntryValue.isNullOrEmpty()) {
                return languageEntryValue
            }
            return LanguageUtils.getDeviceOrFallbackLanguage(LanguageUtils.getDefaultLanguage())
        }

        fun startOrderFlow(fragmentManager: FragmentManager, context: Context, contentProviderId: String?) {
                if (contentProviderId.isNullOrEmpty() || SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_ORDER_ID+"_$contentProviderId").isNullOrEmpty()) {
                    showSubscriptionPacks(context,contentProviderId)
                } else {
                    BottomSheetPayAtStore(contentProviderId).show(fragmentManager,"TAG")
                }
        }

        fun showSubscriptionPacks(context: Context, contentProviderId: String?) {
            val intent = Intent(context, SubscriptionPackListActivity::class.java)
            contentProviderId?.let {
                intent.putExtra(SubscriptionPackListActivity.CONTENTPROVIDER_ID, it)
            }
            context.startActivity(intent)
        }

        fun startActiveOrderDialog(fragmentManager: FragmentManager, contentProviderId: String) {
            val bottomSheetFragment = BottomSheetPayAtStore(contentProviderId)
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }

        fun showContentDetails(fragmentManager: FragmentManager, content: ContentDownload, additionalAttr: HashMap<String, Any?>?) {

            val bottomSheetContentDetail = BottomSheetContentDetails(content, additionalAttr)
            bottomSheetContentDetail.show(fragmentManager, bottomSheetContentDetail.tag)
        }

        fun showNearbyStore(context: Context, content: Content?, showRetailersWithHub: Boolean, contentProviderId: String?, selectedPack: String?) {
            val intent = Intent(context, NearbyStoresActivity::class.java)
            content.let {
                intent.putExtra(NearbyStoresActivity.extraContent, content)
            }
            intent.putExtra(NearbyStoresActivity.extraShowRetailerWithHub, showRetailersWithHub)
            contentProviderId?.let {
                intent.putExtra(NearbyStoresActivity.EXTRA_CONTENT_PROVIDER_ID, it)
            }
            selectedPack?.let {
                intent.putExtra(NearbyStoresActivity.EXTRA_SUBSCRIPTION_ID, it)
            }
            context.startActivity(intent)
        }

        fun shouldShowDownloadInstructions(): Boolean {
            SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_DOWNLOAD_INSTRUCTION)?.let {
                    return it.isEmpty()
                } ?: run {
                return true
            }
        }

        fun openPrivacyPolicy(context: Context?) {
            context?.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(BNConstants.PRIVACY_POLICY_URL)))
        }

        fun openTermsOfUse(context: Context?) {
            val fullPath: String = BNConstants.TERMS_OF_USE_URL
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullPath))
            context?.startActivity(browserIntent)
        }

        fun getHash(text: String): String {
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash: ByteArray = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
            return Base64.encodeToString(hash, Base64.NO_WRAP)
        }

        /**
         * Converting dp to pixel
         */
        fun dpToPx(dp: Float, resources: Resources): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
            ).roundToInt()
        }

        internal fun getOrangeTextSpannable(context: Context, string: String, boldText: String): SpannableString {
            val txtSpannable = SpannableString(string)
            if (boldText.length > string.length) return txtSpannable
            if (string.indexOf(boldText) == -1) return txtSpannable
            txtSpannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(context,
                        R.color.offline_highlighted_color)), string.indexOf(boldText), string.indexOf(boldText) + boldText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return txtSpannable
        }

        internal fun getOrangeTextSpannable(context: Context,
                                            string: String,
                                            boldTextArr: Array<String>): SpannableString {
            val txtSpannable = SpannableString(string)
            if (boldTextArr.isNullOrEmpty()) return txtSpannable
            for (boldText in boldTextArr) {
                if (boldText.length > string.length) return txtSpannable
                val startIndex = string.indexOf(boldText)
                if (startIndex == -1) continue
                txtSpannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(context,
                            R.color.offline_highlighted_color)), string.indexOf(boldText), string.indexOf(boldText) + boldText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return txtSpannable
        }

        internal fun getBoldTextSpannable(string: String, boldTextArr: Array<String>): SpannableString {
            val txtSpannable = SpannableString(string)
            if (boldTextArr.isNullOrEmpty()) return txtSpannable
            for (boldText in boldTextArr) {
                if (boldText.length > string.length) return txtSpannable
                val startIndex = string.indexOf(boldText)
                if (startIndex == -1) continue
                txtSpannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + boldText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return txtSpannable
        }

        internal fun shareContent(contentName: String,
                                  providerId: String,
                                  contentId: String,
                                  type: String,
                                  context: Context) {
            AnalyticsLogger.getInstance().logContentShare(contentId,
                providerId,
                contentName,
                type)

            val shareText = String.format(context.getString(R.string.share_movie_text), contentName)
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "$shareText${BuildConfig.APPLICATION_ID}"
            )
            sendIntent.type = "text/plain"
            context.startActivity(Intent.createChooser(sendIntent, null))
        }

        fun startViewAllContentActivity(
            context: Context,
            isFree: Boolean,
            hubConnected: Boolean,
            title: String,
            activePack: Boolean,
            additionalAttr: HashMap<String, Any?>,
        ) {
            Log.d("AppUtils",additionalAttr.toString())
            val intent = Intent(context, ViewAllContentActivity::class.java)
            intent.putExtra(ViewAllContentActivity.EXTRA_IS_FREE, isFree)
                .putExtra(ViewAllContentActivity.EXTRA_HUB_CONNECTED, hubConnected)
                .putExtra(ViewAllContentActivity.EXTRA_TITLE, title)
                .putExtra(ViewAllContentActivity.EXTRA_BOOLEAN_ACTIVE_SUBS, activePack)

            if(!additionalAttr.isNullOrEmpty()){
                if(additionalAttr.containsKey(ViewAllContentActivity.EXTRA_CONTENT)){
                    intent.putExtra(ViewAllContentActivity.EXTRA_CONTENT, additionalAttr[ViewAllContentActivity.EXTRA_CONTENT] as ContentDownload)
                }
                if(additionalAttr.containsKey(ViewAllContentActivity.EXTRA_CALLED_FROM)){
                    intent.putExtra(ViewAllContentActivity.EXTRA_CALLED_FROM, additionalAttr[ViewAllContentActivity.EXTRA_CALLED_FROM] as String)
                }
            }
            context.startActivity(intent)
        }

        internal fun isGPSLocationEnabled(activity: Activity): Boolean {
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return true
            }
            return false
        }

        fun storageFullBottomSheet(
            fragmentManager: FragmentManager,
            activity: Activity
        ) {
            val additionalParam = HashMap<String,Any>()
            additionalParam["icon_padding"] = 15
            val bottomSheet =
                BottomSheetGenericMessage(
                    R.drawable.ic_exclamation,
                    activity.getString(R.string.storage_almost_full),
                    activity.getString(R.string.free_storage_to_download),
                    true,
                    activity.getString(R.string.bn_proceed),
                    null,
                    { (activity as? MainActivity)?.selectTab(2) },
                    null
                    ,additionalParam
                )
            bottomSheet.show(fragmentManager,"TAG")
        }

        fun goToSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
        }

        fun shouldRefresh(formatter: SimpleDateFormat, key: String, cutOffHours: Int): Boolean {
            val dateString = SharedPreferenceStore.getInstance().get(key)
            if (dateString.isNullOrEmpty()) {
                return true
            }
            else {
                try {
                    val date = formatter.parse(dateString)
                    date?.let {
                        val difference: Long = Date().time - it.time
                        val hours = difference / (60 * 60 * 1000)
                        if (hours >= cutOffHours) return true
                    }
                }
                catch (e: Exception) {
                    return true
                }
            }
            return false
        }

        fun getContentProviderSquareLogoURL(contentProviderId: String): String{
            val url = BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/logos/" + BNConstants.SQUARE_LOGO_NAME
            Log.d("logoUrl" ,url)
            return url
        }

        fun getContentProviderWaterMarkLogoURL(contentProviderId: String): String{
            val url = BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/logos/" + BNConstants.WATERMARK_LOGO_NAME
            return url
        }

        fun getContentProviderNameFromId(contentProviderId: String): String? {
            return SharedPreferenceStore.getInstance().get(SharedPreferenceStore.PREFIX_CONTENT_PROVIDER + contentProviderId)
        }

        fun updateDownloadStatus(context: Context, content: ContentDownload, playButton: ImageView) {
            when (content.downloadStatus) {
                DownloadStatus.QUEUED.value -> {
                    playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_download_cancel
                        )
                    )
                }
                DownloadStatus.IN_PROGRESS.value -> {
                    playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bn_ic_play
                        )
                    )
                }
                DownloadStatus.DOWNLOADED.value -> {
                    playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                }
            }
        }


    }
}