package com.microsoft.mobile.polymer.mishtu.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import java.lang.ClassCastException

class LocationUtil {

    companion object {
        var instance: LocationUtil? = null

        @Synchronized
        fun getNewInstance(): LocationUtil {
            if (instance == null) {
                instance = LocationUtil()
            }
            return instance as LocationUtil
        }
    }

    internal fun isLocationPermissionsGranted(context: Context): Boolean {
        val permissionFineLocCheck = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseLocCheck = ContextCompat.checkSelfPermission(
           context, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionFineLocCheck == PackageManager.PERMISSION_GRANTED ||
            permissionCoarseLocCheck == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    internal fun isLocationPermissionsDenied(context: Context): Boolean {
        val permissionFineLocCheck = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseLocCheck = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionFineLocCheck == PackageManager.PERMISSION_DENIED ||
            permissionCoarseLocCheck == PackageManager.PERMISSION_DENIED) {
            return true
        }
        return false
    }

    internal fun showNoGPSDialogDialog(context: Context,
                                       resolutionForResult: ActivityResultLauncher<IntentSenderRequest>) {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = 5 * 1000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) //this is the key ingredient

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

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

    fun showNoLocationPermissionDialog(context: Context,
                                               fragment: Fragment,
                                               resolutionForResult: ActivityResultLauncher<IntentSenderRequest>) {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.bn_location_pin,
                context.getString(R.string.share_location),
                context.getString(R.string.permission_location),
                true,
                context.getString(R.string.btn_no),
                context.getString(R.string.btn_yes),
                null,
                {
                    if (!AppUtils.isGPSLocationEnabled(fragment.requireActivity()))
                        showNoGPSDialogDialog(context, resolutionForResult)
                    else if (!isLocationPermissionsGranted(context)) {
                        AppUtils.goToSettings(fragment.requireActivity())
                    }
                }, null
            )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            fragment.childFragmentManager,
            bottomSheetFragment.tag
        )
    }
}