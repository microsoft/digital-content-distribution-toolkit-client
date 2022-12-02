// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityFullScreenTogglePlayerBinding
import kotlin.math.max

class FullScreenPlayerActivity : BaseActivity() {
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var url: String? = null
    private lateinit var binding: ActivityFullScreenTogglePlayerBinding
    private val stateResumeWindow = "resumeWindow"
    private val stateResumePosition = "resumePosition"
    private val stateResumeFullScreen = "playerFullscreen"
    private var mExoPlayerFullscreen = false
    private lateinit var mFullScreenIcon: ImageView
    private var mFullScreenDialog: Dialog? = null
    private var mResumeWindow = 0
    private var mResumePosition: Long = 0
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    companion object {
        private const val LOG_TAG = "FullScreenTogglePlayerActivity"
        const val VIDEO_URL_EXTRA_KEY = "VideoURL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_toggle_player)
        url = intent.getStringExtra(VIDEO_URL_EXTRA_KEY)

        val closeButton = findViewById<View>(R.id.exo_close) as ImageButton
        closeButton.setOnClickListener { closePlayer() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(stateResumeWindow, mResumeWindow)
        outState.putLong(stateResumePosition, mResumePosition)
        outState.putBoolean(stateResumeFullScreen, mExoPlayerFullscreen)
        super.onSaveInstanceState(outState)
    }

    private fun initFullscreenDialog() {
        mFullScreenDialog = object : Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (mExoPlayerFullscreen) closeFullscreenDialog()
                closePlayer()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun openFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        (binding.playerView.parent as ViewGroup).removeView(binding.playerView)
        mFullScreenDialog?.addContentView(binding.playerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink))
        mExoPlayerFullscreen = true
        if (!isFinishing) {
            mFullScreenDialog?.show()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun closeFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (binding.playerView.parent as ViewGroup).removeView(binding.playerView)
        (findViewById<View>(R.id.main_media_frame) as FrameLayout).addView(binding.playerView)
        mExoPlayerFullscreen = false
        mFullScreenDialog?.dismiss()
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand))
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            playWhenReady = simpleExoPlayer!!.playWhenReady
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    private fun initFullscreenButton() {
        val controlView: PlayerControlView = binding.playerView.findViewById(R.id.exo_controller)
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon)
        val mFullScreenButton = controlView.findViewById<FrameLayout>(R.id.exo_fullscreen_button)
        mFullScreenButton.setOnClickListener {
            if (!mExoPlayerFullscreen) {
                openFullscreenDialog()
            }
            else {
                closeFullscreenDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (simpleExoPlayer == null) {
            initializePlayer()
        }

        initFullscreenDialog()
        initFullscreenButton()
        openFullscreenDialog()

        if (mExoPlayerFullscreen) {
            (binding.playerView.parent as ViewGroup).removeView(binding.playerView)
            mFullScreenDialog?.addContentView(binding.playerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink))
            mFullScreenDialog?.show()
        }
    }

    override fun onPause() {
        if (simpleExoPlayer != null) {
            mResumeWindow = simpleExoPlayer!!.currentWindowIndex
            mResumePosition = max(0, simpleExoPlayer!!.contentPosition)
            simpleExoPlayer!!.release()
        }
        if (mFullScreenDialog != null) mFullScreenDialog?.dismiss()
        super.onPause()
    }

    override fun onBackPressed() {
        closePlayer()
    }

    fun closePlayer() {
        mResumePosition = max(0, simpleExoPlayer!!.contentPosition)
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun initializePlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = simpleExoPlayer

        url?.let { prepareMedia(it) }
    }

    private fun prepareMedia(linkUrl: String) {
        val uri = Uri.parse(linkUrl)
        val mMediaDataSourceFactory = DefaultDataSourceFactory(this, LOG_TAG)
                val mediaSource = ProgressiveMediaSource.Factory(mMediaDataSourceFactory)
                        .createMediaSource(uri)

        simpleExoPlayer?.prepare(mediaSource, true, true)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        simpleExoPlayer?.playWhenReady = playWhenReady
        simpleExoPlayer?.seekTo(currentWindow, playbackPosition)
        simpleExoPlayer?.prepare(mediaSource, false, false)
    }
}