// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityLocationCaptureBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

class LocationCaptureActivity : HubScanBaseActivity() {

    lateinit var binding: ActivityLocationCaptureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_capture)

        binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(this,
            getString(R.string.request_share_location),
            getString(R.string.download_films_free))

        binding.locationCaptureProceed.setOnClickListener { yesClicked() }
        binding.locationCaptureCancel.setOnClickListener { noClicked() }
    }

    private fun yesClicked() {
        startHubScanWithCurrentLocation(false)
    }

    private fun noClicked() {
        setHubScanDate()
        showNoLocationPermissionDialog {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        BootTelemetryLogger.getInstance().recordIntermediaryEvent(
            BootTelemetryLogger.BootMarker.REQUEST_LOCATION_COMPLETE)
    }

    override fun locatedInHubArea(boolean: Boolean) {
        finish()
    }
}