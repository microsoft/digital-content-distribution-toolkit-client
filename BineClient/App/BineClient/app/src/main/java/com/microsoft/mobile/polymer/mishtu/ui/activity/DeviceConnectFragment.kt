// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.NetworkCapabilities
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.NetworkConnectivity

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

import com.google.android.gms.location.*
import com.microsoft.mobile.polymer.mishtu.utils.LocationUtil


abstract class DeviceConnectFragment: Fragment(), NetworkCapabilities.INetworkCapabilitiesListener, NetworkConnectivity.INetworkChangedListener {
    val deviceViewModel by activityViewModels<DeviceViewModel>()

    lateinit var locationUtil: LocationUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUtil = LocationUtil.getNewInstance()

        if (isDeviceConnectionObserver()) {
            observeConnectionState()
            //Check for connection
            if (isLocationPermissionsGranted()) deviceViewModel.isHubDeviceConnected()
        }


        NetworkConnectivity.getInstance(requireContext()).registerListener(this)
        NetworkCapabilities.getInstance().registerListener(this)
    }

    internal fun isLocationPermissionsGranted(): Boolean {
        return locationUtil.isLocationPermissionsGranted(requireContext())
    }

    private fun observeConnectionState() {
        deviceViewModel.deviceConnected.observe(viewLifecycleOwner, {
            if (it != null && it.id.isNotEmpty()) {
                deviceViewModel.browseDeviceContent(it.id)
                requestRetailerName(it.id)
                onDeviceConnectionChanged(true, it.id)
            } else {
                onDeviceConnectionChanged(false, null)
            }
        })

        deviceViewModel.deviceConnectError.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), "${getString(R.string.device_connection_failed)} - $it", Toast.LENGTH_LONG).show()
        })

        deviceViewModel.retailerName.observe(viewLifecycleOwner, {
            onConnectedRetailerNameRetrieved(it)
        })
    }

    private fun showNoLocationPermissionDialog() {
        locationUtil.showNoLocationPermissionDialog(requireContext(),
            this,
            resolutionForResult)
    }

    internal fun showNoGPSDialogDialog() {
        locationUtil.showNoGPSDialogDialog(requireContext(), resolutionForResult)
    }

    @SuppressLint("MissingPermission")
    private fun requestRetailerName(deviceId: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (isLocationPermissionsGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) {
                if (it != null) {
                    deviceViewModel.getRetailerName(it.latitude, it.longitude, deviceId)
                }
            }
        }
    }

    override fun onNetworkBecameCaptive() {}

    override fun onNetworkHasNoInternet() {
        onNetworkConnectionChanged(false)
    }

    override fun onNetworkConnected(networkType: NetworkConnectivity.NetworkType?) {
        if (isDeviceConnectionObserver())
            deviceViewModel.isHubDeviceConnected()
        onNetworkConnectionChanged(true)
    }

    override fun onNetworkDisconnected() {
        if (isDeviceConnectionObserver())
            deviceViewModel.isHubDeviceConnected()
        onNetworkConnectionChanged(false)
    }

    override fun onNetworkTypeChanged(networkType: NetworkConnectivity.NetworkType?) {
        activity?.runOnUiThread  {
            if (isDeviceConnectionObserver()) deviceViewModel.isHubDeviceConnected()
        }
    }

    abstract fun onDeviceConnectionChanged(isConnected: Boolean, hubId: String?)
    abstract fun onNetworkConnectionChanged(isConnected: Boolean)
    abstract fun isDeviceConnectionObserver(): Boolean
    abstract fun onConnectedRetailerNameRetrieved(retailerName: String)
    abstract fun getCurrentContentProviderId(): String

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        if (!isLocationPermissionsGranted()) {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        showNoLocationPermissionDialog()
                    }
                    else -> {}
                }
        }

    internal val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if(!deviceViewModel.isHubDeviceConnected()) {
                // Go to store with movie instance - TYPE-3
                AppUtils.showNearbyStore(requireContext(), null, true, null, null)
            }
        }
        else {
            //Show dialog requesting permission
            showNoLocationPermissionDialog()
        }
    }
}