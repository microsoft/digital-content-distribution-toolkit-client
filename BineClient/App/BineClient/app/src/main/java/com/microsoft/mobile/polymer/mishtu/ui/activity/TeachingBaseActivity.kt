// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper

abstract class TeachingBaseActivity: HubScanBaseActivity() {
    private lateinit var tooltipDisplayHelper: TooltipDisplayHelper

    private var mShowToolTipHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tooltipDisplayHelper = TooltipDisplayHelper(this,
            teachingContainer(),
            completionCallback())

        mShowToolTipHandler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        if (shouldShowOnResume()) {
            mShowToolTipHandler?.postDelayed({
                tooltipDisplayHelper.start(
                    teachingScreenName()
                )
            }, 500)
        }
    }

    fun start(screen: TooltipDisplayHelper.FTUScreen) {
        tooltipDisplayHelper.start(screen)
    }

    abstract fun teachingScreenName(): TooltipDisplayHelper.FTUScreen
    abstract fun teachingContainer(): Int
    abstract fun completionCallback(): TooltipDisplayHelper.TooltipDisplayCompletion?
    abstract fun shouldShowOnResume(): Boolean
}