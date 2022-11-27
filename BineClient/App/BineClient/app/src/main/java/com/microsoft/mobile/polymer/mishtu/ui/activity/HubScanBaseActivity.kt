package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.location.*
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.DialogInStoreAreaBinding
import com.microsoft.mobile.polymer.mishtu.databinding.DialogNoStoreAreaBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.LocationScanViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.LocationUtil
import com.msr.bine_sdk.models.Error
import java.text.SimpleDateFormat
import java.util.*

abstract class HubScanBaseActivity: BaseActivity() {

    private lateinit var locationUtil: LocationUtil
    private val locationScanViewModel by viewModels<LocationScanViewModel>()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var userCurrentLat = 0.0
    private var userCurrentLng = 0.0

    private var silentScan = false

    private val formatter = SimpleDateFormat(BNConstants.formatter, Locale.getDefault())

    enum class HubScanStatus(val value:String) {
        NO_PERMISSION_GRANTED("NoPermission"),
        NO_HUB_EXIST("NoHubExist"),
        HUB_EXIST("HubExist"),
        PERMISSION_DENIED("PermissionDenied")
    }

    companion object {
        const val KEY_HUB_LAST_SCAN_DATE = "LastScanDate"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationUtil = LocationUtil.getNewInstance()
    }

    internal fun startHubScanIfTime() {
        if (AppUtils.shouldRefresh(formatter, KEY_HUB_LAST_SCAN_DATE, 24)) {
            val status = getLastStatus()
            if (status == HubScanStatus.PERMISSION_DENIED ||
                    status == HubScanStatus.NO_PERMISSION_GRANTED) return

            startHubScanWithCurrentLocation(true)
        }
    }

    internal fun startHubScanWithCurrentLocation(silentScan: Boolean) {
        this@HubScanBaseActivity.silentScan = silentScan
        setHubScanDate()
        if (!AppUtils.isGPSLocationEnabled(this)) {
            locationUtil.showNoGPSDialogDialog(this, resolutionForResult)
        } else {
            checkForLocationPermissionAndProceed()
        }
    }

    private fun checkForLocationPermissionAndProceed() {
        when {
            locationUtil.isLocationPermissionsGranted(this) -> {
                requestLocationUpdates()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    internal fun showNoLocationPermissionDialog(onClickListener: View.OnClickListener?) {
        saveLastStatus(HubScanStatus.PERMISSION_DENIED)
        val additionalParam = HashMap<String,Any>()
        additionalParam["bold_text"] = getString(R.string.location_scan_denied_steps)
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.bn_location_pin,
                null,
                getString(R.string.location_scan_denied),
                true,
                getString(R.string.bn_okay),
                null,
                {
                    onClickListener?.onClick(it)
                }, null,
                additionalParam
            )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun showDeniedLocationPermissionDialog() {
        saveLastStatus(HubScanStatus.PERMISSION_DENIED)
        val additionalParam = HashMap<String,Any>()
        additionalParam["bold_text"] = getString(R.string.location_permission_steps)
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.bn_location_pin,
                null,
                getString(R.string.location_permission_denied),
                true,
                getString(R.string.go_to_settings),
                null,
                {
                    AppUtils.goToSettings(this)
                }, null,
                additionalParam
            )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    checkForLocationPermissionAndProceed()
                }
                Activity.RESULT_CANCELED -> {
                    showNoLocationPermissionDialog(null)
                }
                else -> {}
            }
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //Get nearby stores
            requestLocationUpdates()
        }
        else {
            //Show dialog requesting permission
            showDeniedLocationPermissionDialog()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (!silentScan) showProgress()
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                if (locationResult.locations.isNotEmpty()) {
                    userCurrentLat = locationResult.locations[0].latitude
                    userCurrentLng = locationResult.locations[0].longitude
                    locationScanViewModel.checkRetailersWithHubExist(userCurrentLat,
                        userCurrentLng,
                        20000.0)
                    observeData()
                }
                else {
                    hideProgress()
                }
                fusedLocationClient?.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
    }

    private fun observeData() {
        locationScanViewModel.progress.observe(this, {
            if (it) showProgress()
            else hideProgress()
        })

        locationScanViewModel.error.observe(this, {
            hideProgress()
            if (it == Error.NETWORK_ERROR) {
                Toast.makeText(this, getString(R.string.bn_no_internet_subtitle), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.bn_server_error), Toast.LENGTH_SHORT).show()
            }
        })

        locationScanViewModel.hubExist.observe(this, {
            hideProgress()
            if (it) showInHubAreaDialog()
            else showNoHubDialog()
        })
    }

    open fun locatedInHubArea(boolean: Boolean) {}

    private fun showNoHubDialog() {
        if (silentScan && getLastStatus() == HubScanStatus.NO_HUB_EXIST)
            return //If silent scan(on app start), show dialog only if status changed

        saveLastStatus(HubScanStatus.NO_HUB_EXIST)

        val binding = DialogNoStoreAreaBinding.inflate(layoutInflater)
        val dialog = object: Dialog(this, android.R.style.Theme_Translucent_NoTitleBar) {
            override fun onBackPressed() {}
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        dialog.show()

        binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(this,
            getString(R.string.free_download_not_available),
            getString(R.string.free_download))

        binding.locationCaptureProceed.setOnClickListener {
            AnalyticsLogger.getInstance().logEvent(Event.INTERESTED_OFFLINE_DOWNLOAD)
            Toast.makeText(this, getString(R.string.thank_you_for_interest), Toast.LENGTH_SHORT).show()
            startHomeScreen(false)
        }
        binding.locationCaptureCancel.setOnClickListener {
            startHomeScreen(false)
        }
    }

    private fun showInHubAreaDialog() {
        if (silentScan && getLastStatus() == HubScanStatus.HUB_EXIST)
            return //If silent scan(i.e. on app start), show dialog only if status changed

        setVisitedHubArea()
        saveLastStatus(HubScanStatus.HUB_EXIST)

        val binding = DialogInStoreAreaBinding.inflate(layoutInflater)
        val dialog = object: Dialog(this, android.R.style.Theme_Translucent_NoTitleBar) {
            override fun onBackPressed() {}
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        dialog.show()

        binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(this,
            getString(R.string.free_download_is_available),
            arrayOf(getString(R.string.free_download),
                getString(R.string.percent_data_download)))

        binding.locationCaptureProceed.setOnClickListener {
            startHomeScreen(true)
        }
        binding.locationCaptureCancel.setOnClickListener {
            startHomeScreen(false)
        }
    }

    private fun startHomeScreen(showNearbyStore: Boolean) {
        BootTelemetryLogger.getInstance().recordIntermediaryEvent(
            BootTelemetryLogger.BootMarker.REQUEST_LOCATION_COMPLETE)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(MainActivity.extraShowHubsNearby, showNearbyStore)
        startActivity(intent)
    }

    private fun saveLastStatus(status: HubScanStatus) {
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_LAST_HUB_SCAN_STATUS, status.name)
    }

    fun getLastStatus(): HubScanStatus {
        val status = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_LAST_HUB_SCAN_STATUS)
        return if (status.isNullOrEmpty()) HubScanStatus.NO_PERMISSION_GRANTED
        else HubScanStatus.valueOf(status)
    }

    fun previouslyVisitedHubArea(): Boolean {
        val status = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_WAS_IN_HUB_AREA)
        return !status.isNullOrEmpty()
    }

    private fun setVisitedHubArea() {
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_WAS_IN_HUB_AREA, "true")
    }

    internal fun setHubScanDate() {
        SharedPreferenceStore.getInstance().save(KEY_HUB_LAST_SCAN_DATE, formatter.format(Date()))
    }
}