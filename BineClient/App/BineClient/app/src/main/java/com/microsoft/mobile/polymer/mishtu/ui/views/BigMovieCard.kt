// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BigMovieCardBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.msr.bine_sdk.hub.model.DownloadStatus


@SuppressLint("ViewConstructor")
class BigMovieCard @JvmOverloads constructor(private var onPlayClickListener: OnPlayClickListener?,
                                             private var canDownload: Boolean,
                                             context: Context,
                                             val fragmentManager: FragmentManager,
                                             attrs: AttributeSet? = null,
                                             defStyle: Int = 0):
    FrameLayout(context, attrs, defStyle) {
    private var bigMovieCardBinding: BigMovieCardBinding
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    var content: ContentDownload? = null

    private var runnableAnim: Runnable? = null
    companion object{
        val CALLED_FROM = "BigMovieCard"
    }

    private fun getRunnableAnim(): Runnable {
        if (runnableAnim == null)
            runnableAnim = Runnable {
                bigMovieCardBinding.pulseAnimationImage1.visibility = View.VISIBLE
                bigMovieCardBinding.pulseAnimationImage2.visibility = View.VISIBLE
                kotlin.run {
                    bigMovieCardBinding.pulseAnimationImage1.animate()
                        .scaleX(4f)
                        .scaleY(4f)
                        .alpha(0f)
                        .setDuration(2000).withEndAction {
                        kotlin.run {
                            bigMovieCardBinding.pulseAnimationImage1.scaleX = 1f
                            bigMovieCardBinding.pulseAnimationImage1.scaleY = 1f
                            bigMovieCardBinding.pulseAnimationImage1.alpha = 1f
                        }
                    }

                    bigMovieCardBinding.pulseAnimationImage2.animate()
                        .scaleX(4f)
                        .scaleY(4f)
                        .alpha(0f)
                        .setDuration(1000).withEndAction {
                        kotlin.run {
                            bigMovieCardBinding.pulseAnimationImage2.scaleX = 1f
                            bigMovieCardBinding.pulseAnimationImage2.scaleY = 1f
                            bigMovieCardBinding.pulseAnimationImage2.alpha = 1f
                        }
                    }
                }
            }
        return runnableAnim as Runnable
    }

    var hubConnected = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
        }

    var hasActiveSubscription = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
        }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bigMovieCardBinding = BigMovieCardBinding.inflate(inflater, this, true)
        bigMovieCardBinding.bigMovieCardParent.visibility = View.GONE
    }

    fun initBigMovieCardView(content: ContentDownload) {
        this.content = content
        bigMovieCardBinding.bigMovieCardParent.visibility = View.VISIBLE
        val imageUrl = content.getThumbnailLandscapeImage() ?: content.getThumbnailImage()

        Glide.with(this)
            .load(imageUrl)
            .dontAnimate()
            .into(bigMovieCardBinding.banner)

        AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(content.contentProviderId),
            bigMovieCardBinding.logo)

        bigMovieCardBinding.title.text = content.getContentTitle()
        bigMovieCardBinding.subTitle.visibility = View.VISIBLE
        bigMovieCardBinding.subTitle.text = getSubTitleString(content)
        if (content.free || hasActiveSubscription) {
            bigMovieCardBinding.freeLayout.visibility = if (content.free) View.VISIBLE else View.GONE
            setBMCFooterForActiveSubscription()
        } else {
            bigMovieCardBinding.freeLayout.visibility = View.GONE
            setBMCFooterForNoSubscription()
        }

        if (content.hasTrailers()) {
            initializePlayer(content.getTrailer())
        }

        bigMovieCardBinding.shareMovie.setOnClickListener {
            AppUtils.shareContent(content.title,
                content.contentId,
                content.contentProviderId,
                AnalyticsLogger.LongForm,
                context)
        }
        bigMovieCardBinding.downloadMovie.setOnClickListener {
            onPlayClickListener?.onDownloadClicked(content)
        }
    }

    private fun initializePlayer(url: String) {
        simpleExoPlayer = SimpleExoPlayer.Builder(context).build()
        bigMovieCardBinding.playerView.player = simpleExoPlayer

        prepareMedia(url)
    }

    fun releasePlayer() {
        if (simpleExoPlayer != null) {
            playWhenReady = simpleExoPlayer!!.playWhenReady
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    fun hideView() {
        bigMovieCardBinding.bigMovieCardParent.visibility = View.GONE
    }

    private fun prepareMedia(linkUrl: String) {
        val uri = Uri.parse(linkUrl)
        val mMediaDataSourceFactory = DefaultDataSourceFactory(context, "Tag")
        val mediaSource = ProgressiveMediaSource.Factory(mMediaDataSourceFactory)
            .createMediaSource(uri)

        simpleExoPlayer?.prepare(mediaSource, true, true)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.seekTo(currentWindow, playbackPosition)
        simpleExoPlayer?.prepare(mediaSource, false, false)
    }

    private fun startMedia() {
        simpleExoPlayer?.playWhenReady = true
    }

    fun pauseMedia() {
        simpleExoPlayer?.playWhenReady = false
    }

    private fun getSubTitleString(content: ContentDownload): String {
        val subTitle = StringBuffer("")

        if (!content.yearOfRelease.isNullOrEmpty()) {
            subTitle.append(content.yearOfRelease)
        }
        if (!content.ageAppropriateness.isNullOrEmpty()) {
            subTitle.append(" • ").append(content.ageAppropriateness)
        }
        if (content.genre.isNotEmpty()) {
            subTitle.append(" • ").append(content.genre)
        }
        if(content.language.isNotEmpty()){
            subTitle.append(" • ").append(content.language)
        }
        return subTitle.toString()
    }

    fun setBMCFooterForNoSubscription() {
        bigMovieCardBinding.shareMovie.visibility = INVISIBLE
        bigMovieCardBinding.downloadMovie.visibility = INVISIBLE
        bigMovieCardBinding.buttonBuyPack.visibility = VISIBLE
        bigMovieCardBinding.buttonBuyPack.text = context.getString(R.string.bn_select_a_pack)
        bigMovieCardBinding.playButton.visibility = GONE
        bigMovieCardBinding.playButtonText.visibility = GONE

        bigMovieCardBinding.playButton.setOnClickListener {
            this.content?.let { it1 -> onPlayClickListener?.onPaidContentClicked(it1) }
        }
        bigMovieCardBinding.buttonBuyPack.setOnClickListener {
            this.content?.let { it1 -> onPlayClickListener?.onPaidContentClicked(it1) }
        }
        bigMovieCardBinding.playerView.setOnClickListener {
            if (content != null && content!!.isMovie) {
                startViewAllActivity()
            } else {
                content?.let { it1 -> showContentInfo() }
            }
        }
        bigMovieCardBinding.banner.setOnClickListener {
            if (content != null && content!!.isMovie) {
                startViewAllActivity()
            } else {
                content?.let { it1 ->
                    showContentInfo() }
            }

        }
    }

    private fun showContentInfo() {
        pauseMedia()
        val additionalAttr = HashMap<String, Any?>()
        additionalAttr[ViewAllContentActivity.EXTRA_HUB_CONNECTED] = hubConnected
        additionalAttr[ViewAllContentActivity.EXTRA_BOOLEAN_ACTIVE_SUBS] = hasActiveSubscription
        content?.let {
            AppUtils.showContentDetails(fragmentManager,it, additionalAttr)
        }
    }

    private fun startViewAllActivity() {
        val map = HashMap<String,Any?>()
        content?.let {
            map[ViewAllContentActivity.EXTRA_CONTENT] = it
            map[ViewAllContentActivity.EXTRA_CALLED_FROM] = BigMovieCard.CALLED_FROM
        }
        AppUtils.startViewAllContentActivity(
            context,
            isFree = false,
            hubConnected = false,
            title = context.getString(R.string.bn_paid_movies),
            activePack = hasActiveSubscription
        ,map)
    }

    fun setBMCFooterForActiveSubscription() {

        bigMovieCardBinding.shareMovie.visibility = VISIBLE
        bigMovieCardBinding.buttonBuyPack.visibility = GONE
        bigMovieCardBinding.playButtonText.visibility = GONE
        this.content?.let { updateDownloadStatus(it) }

        bigMovieCardBinding.playButton.setOnClickListener {
            this.content?.let { con ->
                if (con.downloadStatus == DownloadStatus.QUEUED.value) {
                    onPlayClickListener?.onCancelDownloadClicked(con)
                } else {
                    if (content != null && content!!.isMovie) {
                        onPlayClickListener?.onPlayClicked(con)
                    } else {
                        content?.let { it1 -> showContentInfo() }
                    }
                }
            }
        }
        bigMovieCardBinding.playerView.setOnClickListener {
            if (!hubConnected) this.content?.let { it1 ->
                if (it1.isMovie) {
                    if (!hubConnected) onPlayClickListener?.onPlayClicked(it1)
                } else {
                    showContentInfo()
                }
            }
        }

        bigMovieCardBinding.banner.setOnClickListener {

            this.content?.let { it1 ->
                if (it1.isMovie) {
                    if (!hubConnected) onPlayClickListener?.onPlayClicked(it1)
                } else {
                    showContentInfo()
                }
            }
        }
    }

    fun updateDownloadStatus(content: ContentDownload) {
        this.content = content
        when (content.downloadStatus) {
            DownloadStatus.QUEUED.value -> {
                bigMovieCardBinding.playButton.visibility = VISIBLE
                bigMovieCardBinding.downloadMovie.visibility = INVISIBLE
                bigMovieCardBinding.inProgressStatus.visibility = VISIBLE
                bigMovieCardBinding.downloadingQueue.visibility = VISIBLE
                bigMovieCardBinding.downloadingProgress.visibility = GONE
                bigMovieCardBinding.inProgressText.text = context.getString(R.string.queued)
                bigMovieCardBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download_cancel))
            }
            DownloadStatus.IN_PROGRESS.value -> {
                bigMovieCardBinding.playButton.visibility = VISIBLE
                bigMovieCardBinding.downloadMovie.visibility = INVISIBLE
                bigMovieCardBinding.inProgressStatus.visibility = VISIBLE
                bigMovieCardBinding.downloadingQueue.visibility = GONE
                bigMovieCardBinding.downloadingProgress.visibility = VISIBLE
                bigMovieCardBinding.downloadingProgress.progress = content.downloadProgress
                bigMovieCardBinding.inProgressText.text = String.format(
                    context.getString(R.string.text_download_status),
                    content.downloadProgress, "%")
                bigMovieCardBinding.playButtonText.text = context.getString(R.string.button_play_now)
                bigMovieCardBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
            }
            else -> {
                bigMovieCardBinding.inProgressStatus.visibility = INVISIBLE
                if (content.free || hasActiveSubscription) {
                    if(canDownload) {
                        bigMovieCardBinding.downloadMovie.visibility = View.VISIBLE
                    } else {
                        bigMovieCardBinding.downloadMovie.layoutParams.width =  resources.getDimensionPixelSize(R.dimen.dp_2)
                        bigMovieCardBinding.downloadMovie.layoutParams.height =  resources.getDimensionPixelSize(R.dimen.dp_2)
                    }
                    if (hubConnected) {
                        bigMovieCardBinding.playButton.visibility = INVISIBLE
                    }
                    else {
                        bigMovieCardBinding.playButton.visibility = VISIBLE
                        bigMovieCardBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                    }
                }
            }
        }
    }

    fun pauseBigMovieCard() {
        val card: BigMovieCard = this
        card.bigMovieCardBinding.bannerGradient.visibility = VISIBLE
        card.bigMovieCardBinding.banner.visibility = VISIBLE
        card.bigMovieCardBinding.logo.visibility = VISIBLE
        card.pauseMedia()
    }

    fun playBigMovieCard() {
        val card: BigMovieCard = this
        if (card.content?.hasTrailers() == true) {
            card.bigMovieCardBinding.bannerGradient.visibility = INVISIBLE
            card.bigMovieCardBinding.banner.visibility = INVISIBLE
            card.bigMovieCardBinding.logo.visibility = INVISIBLE
            card.startMedia()
        }
    }

    fun animateDownload() {
        if (!canDownload) return
        content?.let {
            if (it.downloadStatus != DownloadStatus.NOT_DOWNLOADED.value) return
            if (it.free || hasActiveSubscription) {
                getRunnableAnim().run()
            }
        }
    }

    fun stopDownloadAnimation() {
        bigMovieCardBinding.pulseAnimationImage1.visibility = View.GONE
        bigMovieCardBinding.pulseAnimationImage2.visibility = View.GONE
    }

    interface OnPlayClickListener {
        fun onPlayClicked(content: ContentDownload)
        fun onPaidContentClicked(content: ContentDownload)
        fun onDownloadClicked(content: ContentDownload)
        fun onCancelDownloadClicked(content: ContentDownload)
    }

    //releasePlayer and set the content to null
    fun resetBigMovieCard(){
        releasePlayer()
        this.content = null
    }
}