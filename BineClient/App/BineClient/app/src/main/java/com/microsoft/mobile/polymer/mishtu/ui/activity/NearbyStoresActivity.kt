// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException

import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityNearbyStoresBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.fragment.NearbyStoreFragment
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.RetailerViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.msr.bine_sdk.cloud.models.Retailer
import com.msr.bine_sdk.models.Error
import java.lang.ClassCastException


class NearbyStoresActivity : HubScanBaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    val retailerViewModel by viewModels<RetailerViewModel>()
    private lateinit var nearbyStoreFragment: NearbyStoreFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityNearbyStoresBinding

    private var userCurrentLat = 0.0
    private var userCurrentLng = 0.0
    var content: Content? = null
    var contentProviderId: String? = null
    var subscriptionId: String? = null
    var showRetailerWithHub: Boolean = false

    companion object {
        const val extraShowRetailerWithHub = "ShowRetailerWithHub"
        const val extraContent = "Content"
        const val EXTRA_CONTENT_PROVIDER_ID = "ContentProviderId"
        const val EXTRA_SUBSCRIPTION_ID = "SubscriptionId"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_stores)
        content = intent.getSerializableExtra(extraContent) as Content?
        contentProviderId = intent.getStringExtra(EXTRA_CONTENT_PROVIDER_ID)
        subscriptionId = intent.getStringExtra(EXTRA_SUBSCRIPTION_ID)
        showRetailerWithHub = intent.getBooleanExtra(extraShowRetailerWithHub, false)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nearby_stores)
        if(contentProviderId.isNullOrEmpty()){
            contentProviderId = content?.contentProviderId
        }
        nearbyStoreFragment = NearbyStoreFragment(contentProviderId,subscriptionId)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.nearby_store_layout, nearbyStoreFragment)
                .commit()


        showProgress()

        if (getLastStatus() == HubScanStatus.HUB_EXIST || !showRetailerWithHub) {
            observeData()
            if (AppUtils.isGPSLocationEnabled(this)) {
                locateAndRequestRetailers()
            } else {
                showNoGPSDialogDialog()
            }
        }
        else {
            startHubScanWithCurrentLocation(false)
        }
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    fun startStoreDetails(retailer: Retailer) {
        if (isGoogleMapsInstalled()) {
            if (checkPermissions()) {
                // mService?.requestLocationUpdates()
            }
            val url = "http://maps.google.com/maps?saddr=" + userCurrentLat + "," + userCurrentLng + "&daddr=" + retailer.address.mapLocation.latitude.toString() + "," + retailer.address.mapLocation.longitude.toString()
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(url))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)
        } else {
            showSnackBar()
        }
    }

    private fun observeData() {
        if (content == null) {            //NearBy screen type-1,3
            observeAllRetailers()
        } else {                        //NearBy screen type-2
            observeRetailersWithSelectedContent()
        }

        retailerViewModel.error.observe(this, {
            hideProgress()
            if (it == Error.NETWORK_ERROR) {
                binding.networkError.visibility = View.VISIBLE
                binding.networkError.findViewById<TextView>(R.id.btnRetry).setOnClickListener { retryServiceClicked() }
            } else {
                binding.serviceError.visibility = View.VISIBLE
                binding.serviceError.findViewById<TextView>(R.id.btnRetry).setOnClickListener { retryServiceClicked() }
            }
        })
    }


    private fun observeAllRetailers() {
        retailerViewModel.retailers.observe(this, {
            hideProgress()
            binding.nearbyStoreLayout.visibility = View.VISIBLE
            if (it.isNullOrEmpty()) {
                nearbyStoreFragment.setNoRetailersFound(!showRetailerWithHub)
            } else {
                nearbyStoreFragment.setRetailerListData(it, showRetailerWithHub)
            }
        })
    }

    private fun observeRetailersWithSelectedContent() {
        retailerViewModel.retailerWithSelectedContent.observe(this, {
            hideProgress()
            binding.nearbyStoreLayout.visibility = View.VISIBLE
            if (it.isNullOrEmpty()) {
                nearbyStoreFragment.setNoRetailersFound(!showRetailerWithHub)
            } else {
                nearbyStoreFragment.setRetailerListDataForSelectedContent(it, content!!)
            }
        })
    }

    private fun locateAndRequestRetailers() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 111)
            return
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    userCurrentLat = location.latitude
                    userCurrentLng = location.longitude
                    retailerViewModel.getRetailers(location.latitude,
                        location.longitude,
                        showRetailerWithHub,
                        content?.contentId)
                } else {
                    requestLocationUpdates()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults.isNotEmpty()) {
            val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (locationAccepted) {
                fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        userCurrentLat = location.latitude
                        userCurrentLng = location.longitude
                        retailerViewModel.getRetailers(location.latitude,
                            location.longitude,
                            showRetailerWithHub,
                            content?.contentId)
                    } else {
                        requestLocationUpdates()
                    }
                }
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                if (locationResult.locations.isNotEmpty()) {
                    userCurrentLat = locationResult.locations[0].latitude
                    userCurrentLng = locationResult.locations[0].longitude
                    retailerViewModel.getRetailers(locationResult.locations[0].latitude,
                        locationResult.locations[0].longitude,
                        showRetailerWithHub,
                        content?.contentId)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
    }

    private fun isGoogleMapsInstalled(): Boolean {
        return try {
            val info = packageManager.getApplicationInfo("com.google.android.apps.maps", 0)
            info.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showSnackBar() {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }

    private fun showPermissionDeniedDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

        val alertDialog = builder.setTitle(resources.getString(R.string.bn_help_support_title))
            .setTitle(R.string.permission_denied)
            .setMessage(R.string.permission_location)
            .setPositiveButton(R.string.settings
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.cancel) {
                _, _ ->
                finish()
            }
            .create()

        alertDialog.show()

        (alertDialog.findViewById(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }

    override fun showProgress() {
        binding.nearbyStoresLoader.visibility = View.VISIBLE
        binding.nearbyStoresLoaderText.visibility = View.VISIBLE
        binding.nearbyStoreLayout.visibility = View.INVISIBLE
        binding.networkError.visibility = View.INVISIBLE
        binding.serviceError.visibility = View.INVISIBLE

        binding.nearbyStoresLoader.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in_out))
    }

    override fun hideProgress() {
        binding.nearbyStoresLoader.clearAnimation()
        binding.nearbyStoresLoader.visibility = View.GONE
        binding.nearbyStoresLoaderText.visibility = View.GONE
    }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    locateAndRequestRetailers()
                }
                Activity.RESULT_CANCELED -> {
                    showPermissionDeniedDialog()
                }
                else -> {}
            }
        }

    private fun showNoGPSDialogDialog() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = 5 * 1000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) //this is the key ingredient

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            exception as ResolvableApiException
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                            resolutionForResult.launch(intentSenderRequest)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private fun retryServiceClicked() {
        showProgress()
        findViewById<View>(R.id.nearby_store_layout).visibility = View.INVISIBLE
        retailerViewModel.getRetailers(userCurrentLat,
            userCurrentLng,
            showRetailerWithHub,
            content?.contentId)
    }
}