// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetRateUsBinding

class BottomSheetRateOnPlayStore: BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetRateUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override  fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_rate_us, container, false)

        binding.btnRateLater.setOnClickListener { dismiss() }
        binding.btnRateUs.setOnClickListener {
            startPlayStoreIntent()
            dismiss()
        }
        binding.dialogClose.setOnClickListener { dismiss() }
        return binding.root
    }

    private fun startPlayStoreIntent() {
        val uri: Uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")))
        }
    }
}