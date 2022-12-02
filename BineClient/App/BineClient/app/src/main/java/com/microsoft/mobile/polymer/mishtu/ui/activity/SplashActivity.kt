// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.collections.ArrayList


class SplashActivity : BaseActivity(){
    private var mAppUpdateManager: AppUpdateManager? = null
    private val RC_APP_UPDATE = 11

    private var installStateUpdatedListener: InstallStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            when {
                state.installStatus() == InstallStatus.DOWNLOADED -> {
                    // Commenting this as IMMEDIATE update would auto install
                    // popupSnackBarForCompleteUpdate()
                }
                state.installStatus() == InstallStatus.INSTALLED -> {
                    mAppUpdateManager?.completeUpdate()
                }
                else -> {
                    Log.i("TAG", "InstallStateUpdatedListener: state: " + state.installStatus())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        when {
            isDeviceRooted() -> {
                closeApp()
            }
            cutoffDatePast() -> {
                showBlockUserDialog()
            }
            else -> {
                mAppUpdateManager = AppUpdateManagerFactory.create(this)
                mAppUpdateManager?.registerListener(installStateUpdatedListener)

                checkUpdates()
            }
        }
    }

    private fun closeApp() {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.info_icon,
                getString(R.string.app_force_stop_rooted_device),
                null, false,
                getString(R.string.button_ok),
                null,
                { finish() },
                null,
                null)
        bottomSheetFragment.isCancelable = false
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    override fun onResume() {
        super.onResume()

        mAppUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    mAppUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        RC_APP_UPDATE
                    )
                }
            }
    }

    override fun onDestroy() {
        mAppUpdateManager?.unregisterListener(installStateUpdatedListener)
        super.onDestroy()
    }

    private fun checkUpdates() {
        mAppUpdateManager?.appUpdateInfo?.addOnFailureListener {
            //Toast.makeText(this, getString(R.string.app_update_failed), Toast.LENGTH_SHORT).show()
            startApp()
        }
        mAppUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    mAppUpdateManager?.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE,
                            this@SplashActivity, RC_APP_UPDATE)
                } catch (e: SendIntentException) {
                    Toast.makeText(this, getString(R.string.app_update_failed), Toast.LENGTH_SHORT).show()
                    startApp()
                }
            }  else {
                startApp()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, getString(R.string.app_update_failed), Toast.LENGTH_SHORT).show()
            }
            startApp()
        }
    }

    private fun isDeviceRooted(): Boolean {
        return detectRootManagementApps() || detectPotentiallyDangerousApps() ||
                detectTestKeys() || checkRootMethod2() || checkSuExists()    }

    private fun detectTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    /**
     * A variation on the checking for SU, this attempts a 'which su'
     * @return true if su found
     */
    private fun checkSuExists(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun detectRootManagementApps(): Boolean {
        // Create a list of package names to iterate over from constants any others provided
        val packages: ArrayList<String> = ArrayList(BNConstants.knownRootAppsPackages.toList())
        return isAnyPackageFromListInstalled(packages)
    }

    /**
     * Check if any package in the list is installed
     * @param packages - list of packages to search for
     * @return true if any of the packages are installed
     */
    private fun isAnyPackageFromListInstalled(packages: List<String>): Boolean {
        var result = false
        val pm: PackageManager = this.packageManager
        for (packageName in packages) {
            try {
                // Root app detected
                pm.getPackageInfo(packageName, 0)
                Log.i("Rooted device","$packageName ROOT management app detected!")
                result = true
            } catch (e: PackageManager.NameNotFoundException) {
                // Exception thrown, package is not installed into the system
            }
        }
        return result
    }

    /**
     * Using the PackageManager, check for a list of well known apps that require root. @link {Const.knownRootAppsPackages}
     * @return true if one of the apps it's installed
     */
    private fun detectPotentiallyDangerousApps(): Boolean {
        // Create a list of package names to iterate over from constants any others provided
        val packages: ArrayList<String> = ArrayList()
        packages.addAll(BNConstants.knownDangerousAppsPackages.toList())
        return isAnyPackageFromListInstalled(packages)
    }

    private fun cutoffDatePast(): Boolean {
        val date = TimestampUtils.getDateFromUTC(BNConstants.CUT_OFF_DATE)
        // Removing this condition as it affects the cut-off date updates.
        //val hasPassedLaunchDate = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_PAST_LAUNCH_DATE)
        //if (!hasPassedLaunchDate.isNullOrEmpty() && hasPassedLaunchDate == "1") return true
        if(TimestampUtils.isDatePastOrToday(date)) {
            //Cut Off reached
            //SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_PAST_LAUNCH_DATE, "1")
            return true
        }
        return false
    }

    private fun showBlockUserDialog() {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.ic_bottom_sheet_time_bomb,
                getString(R.string.app_force_stop_message),
                null,
                false,
                getString(R.string.button_ok),
                null,
                { finish() },
                null, null
            )
        bottomSheetFragment.isCancelable = false
        bottomSheetFragment.show(
            supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun startApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(AppUtils.getNextIntent(this))
            finish()
        }, 1000)
    }
}
