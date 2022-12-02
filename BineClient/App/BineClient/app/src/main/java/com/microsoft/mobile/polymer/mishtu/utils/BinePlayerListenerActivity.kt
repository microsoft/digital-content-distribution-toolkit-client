// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.offline.DownloadManager
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.ui.activity.CustomBinePlayerActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.TeachingBaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity
import com.microsoft.mobile.polymer.mishtu.ui.fragment.ContentFragment
import com.microsoft.mobile.polymer.mishtu.ui.fragment.SearchResultFragment
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentDetailsViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DownloadManagerListener
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.hub.model.DownloadStatus
import com.msr.bine_sdk.player.BinePlayerViewActivity
import java.util.concurrent.TimeUnit

abstract class BinePlayerListenerActivity : TeachingBaseActivity(), DownloadManagerListener {

    private var playbackPosition: Long = 0
    lateinit var content: ContentDownload
    private val viewModel by viewModels<ContentDetailsViewModel>()
    internal val contentViewModel by viewModels<ContentViewModel>()
    internal val deviceViewModel by viewModels<DeviceViewModel>()

    fun startPlayer(contentDownload: ContentDownload) {
        this.content = contentDownload
        registerPlayerListener(content)
        val intent = Intent(this@BinePlayerListenerActivity, CustomBinePlayerActivity::class.java)
        if ((contentDownload.downloadStatus == DownloadStatus.DOWNLOADED.value || contentDownload.downloadStatus == DownloadStatus.IN_PROGRESS.value) &&
            !contentDownload.downloadUrl.isNullOrEmpty()) {
            intent.putExtra(BinePlayerViewActivity.keyExtraVideoURL, contentDownload.downloadUrl)
            intent.putExtra(BinePlayerViewActivity.keyExtraDashURL, content.dashUrl)
            intent.putExtra(CustomBinePlayerActivity.extraShouldShowDownloads, false)

        } else {
            intent.putExtra(BinePlayerViewActivity.keyExtraVideoURL, contentDownload.dashUrl)
            intent.putExtra(CustomBinePlayerActivity.extraShouldShowDownloads,contentDownload.downloadStatus == DownloadStatus.NOT_DOWNLOADED.value)
        }
        intent.putExtra("Content", BOConverter.getContentFromContentDownload(content))
        intent.putExtra(Constants.TITLE, content.title)
        intent.putExtra(Constants.FOLDER_ID, content.contentId)
        intent.putExtra(Constants.PLAY_BACK_POSITION, content.playBackPosition)
        startActivityForResult(intent, 1001)
    }

    private fun registerPlayerListener(content: ContentDownload){
        BinePlayerViewActivity.registerListener(object : BinePlayerViewActivity.BinePlayerListener{
            override fun onTokenFailure(p0: String?) {
                Log.d("BinePlayerViewActivity", "onTokenFailure - $p0")
            }

            override fun onReceivedToken() {
                Log.d("BinePlayerViewActivity", "onReceivedToken")
            }

            override fun onStreamingBegan() {
                content.let {
                    AnalyticsLogger.getInstance().logLongFormContentView(BOConverter.getContentFromContentDownload(it), false)
                }
            }
            override fun onOfflinePlayBegan() {
                content.let {
                    AnalyticsLogger.getInstance().logLongFormContentView(BOConverter.getContentFromContentDownload(it), true)
                }
            }

            override fun onPauseClicked(p0: Long) {
                content.let {
                    AnalyticsLogger.getInstance().logLongFormContentPause(BOConverter.getContentFromContentDownload(it))
                }
            }

            override fun onRequestToken() {
                Log.d("BinePlayerViewActivity", "onRequestToken")
            }

            override fun onPlayError(p0: String?) {
                Log.d("BinePlayerViewActivity", "onPlayError - $p0")
            }

            override fun onStreamingInitiated(p0: String?) {
                Log.d("BinePlayerViewActivity", "onStreamingInitiated")
            }

            override fun onPlayClicked(p0: Long) {
                content.let {
                    AnalyticsLogger.getInstance().logLongFormContentView(BOConverter.getContentFromContentDownload(it),
                        it.downloadStatus == DownloadStatus.DOWNLOADED.value)
                }
            }
            override fun onPlayEnded(p0: Long) {
            }
        })
    }

    private fun unRegisterPlayerListener() {
        if (BinePlayerViewActivity.isListening()) {
            BinePlayerViewActivity.unregisterListener()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                val result = data?.getLongExtra(Constants.PLAY_BACK_POSITION, 0)
                result?.let {
                    playbackPosition = result
                    content.let {
                        it.playBackPosition = playbackPosition
                        if (it.playBackPosition?.toInt() != 0) {
                            viewModel.addContinueWatchList(it)
                        }
                        if (!it.free && TimeUnit.MILLISECONDS.toMinutes(playbackPosition) >= 2) {
                            viewModel.recordContentWatchEvent(it.contentId)
                        }
                    }
                }
                val downloadClicked = data?.getBooleanExtra(CustomBinePlayerActivity.extraDownloadClicked, false)
                if (downloadClicked == true) {
                    if (AppUtils.shouldShowDownloadInstructions()) {
                        //(this as? MainActivity)?.selectTab(2)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(MainActivity.extraShiftTab, 2)
                        startActivity(intent)
                    } else {
                        if (contentViewModel.hasAvailableStorage(content.getSize().toLong())) {
                            contentViewModel.beginDownload(BOConverter.getContentFromContentDownload(
                                content), this)
                        } else {
                            AppUtils.storageFullBottomSheet(supportFragmentManager,
                                this)
                        }
                    }
                }
                unRegisterPlayerListener()
            }
        }
    }

    /**
     * if downloadIn process -> startPlayer
     * else
     *      if content is series -> show the contentDetailSheet only
     *      if its not called from the contentDetailbottomsheet
     *
     *      if content is movie -> show the detailBottomSheet if pack is not active
     *
     * */

    internal fun onItemClicked(
        contentDownload: ContentDownload,
        hubConnected: Boolean,
        additionalAttrMap: Map<String, Any?>,

        ) {
        val hasActiveSubscription = contentViewModel.hasValidSubscriptionForContent(contentDownload.contentProviderId, contentDownload.contentId)
        when (contentDownload.downloadStatus) {
            DownloadStatus.QUEUED.value -> {
                cancelClicked(contentDownload)
            }
            DownloadStatus.IN_PROGRESS.value -> {
                startPlayer(contentDownload)
            }
            DownloadStatus.DOWNLOADED.value -> {
                startPlayerOrShowContentDetails(contentDownload,
                    additionalAttrMap,
                    hasActiveSubscription,
                    hubConnected)
            }
            else -> {
                if (contentDownload.free || hasActiveSubscription) {
                    if (hubConnected) {
                        if (contentViewModel.hasAvailableStorage(contentDownload.getSize()
                                .toLong())
                        ) {
                            var calledFrom = additionalAttrMap.get(ViewAllContentActivity.EXTRA_CALLED_FROM) as String
                            contentViewModel.setBinePlayerActivityListener(this)
                            contentViewModel.beginDownload(contentDownload.contentId, isBulkDownLoad(contentDownload,additionalAttrMap), this)
                        } else {
                            AppUtils.storageFullBottomSheet(supportFragmentManager,
                                this)
                        }
                    } else {
                        startPlayerOrShowContentDetails(contentDownload,
                            additionalAttrMap,
                            hasActiveSubscription,
                            hubConnected)
                    }
                } else {
                    if (shouldShowContentDetailBottomSheet(contentDownload,
                            additionalAttrMap,
                            hasActiveSubscription))
                        showSeriesDetails(contentDownload, hubConnected, contentViewModel.hasValidSubscriptionForContent(contentDownload.contentProviderId, contentDownload.contentId))
                    else
                        AppUtils.startOrderFlow(supportFragmentManager, this,
                            contentDownload.contentProviderId)
                }
            }
        }
    }

    private fun isBulkDownLoad(
        contentDownload: ContentDownload,
        additionalAttrMap: Map<String, Any?>,
    ): Boolean {
        if (contentDownload.isMovie || additionalAttrMap.isNullOrEmpty()) {
            return false
        }
        var calledFrom = additionalAttrMap.get(ViewAllContentActivity.EXTRA_CALLED_FROM) as String
        return ((calledFrom.equals(ViewAllContentActivity.CALLED_FROM)) ||
                (calledFrom.equals(ContentFragment.CALLED_FROM)) ||
                (calledFrom.equals(SearchResultFragment.CALLED_FROM)))
    }

    private fun startPlayerOrShowContentDetails(
        contentDownload: ContentDownload,
        additionalAttrMap: Map<String, Any?>,
        hasActiveSubscription: Boolean,
        hubConnected: Boolean,
    ) {
        if (shouldShowContentDetailBottomSheet(contentDownload,
                additionalAttrMap,
                hasActiveSubscription)
        )
            showSeriesDetails(contentDownload,
                hubConnected,
                hasActiveSubscription)
        else
            startPlayer(contentDownload)
    }

    private fun shouldShowContentDetailBottomSheet(
        contentDownload: ContentDownload,
        additionalAttrMap: Map<String, Any?>,
        hasActiveSubscription: Boolean,
    ): Boolean {
        var calledFrom = additionalAttrMap.get(ViewAllContentActivity.EXTRA_CALLED_FROM) as String
        if(contentDownload.isMovie) {
            return !hasActiveSubscription && !contentDownload.free
        }
        if (((calledFrom.equals(SearchResultFragment.CALLED_FROM)
            || calledFrom.equals(ContentFragment.CALLED_FROM) ||
                    calledFrom.equals(ViewAllContentActivity.CALLED_FROM)))
        ) {
            return true
        }
        return false
    }


    private fun showSeriesDetails(
        contentDownload: ContentDownload,
        hubConnected: Boolean,
        hasActiveSubscription: Boolean
    ) {
        val map = HashMap<String, Any?>()
        map[ViewAllContentActivity.EXTRA_HUB_CONNECTED] = hubConnected
        map[ViewAllContentActivity.EXTRA_BOOLEAN_ACTIVE_SUBS] = hasActiveSubscription
        AppUtils.showContentDetails(supportFragmentManager, contentDownload, map)
    }

    fun cancelClicked(content: ContentDownload) {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.ic_delete,
                getString(R.string.confirm_cancel),
                null, false,
                getString(R.string.btn_no),
                getString(R.string.btn_yes),
                null,
                {
                    contentViewModel.cancelDownload(this, content)
                }, null
            )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    override fun registerDownloadManagerListener(downloadManager: DownloadManager) {
        downloadManager.setListener(deviceViewModel)
    }
}