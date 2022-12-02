// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.msr.bine_sdk.cloud.models.Hub
import com.msr.bine_sdk.hub.DeviceConnect
import com.msr.bine_sdk.hub.model.ConnectedHub

class DeviceConnect(val context: Context) {
    private var wifiManager: WifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager


    fun isConnected(hub: Hub): ConnectedHub? {
        val connectedHub = ConnectedHub(hub)
        val is5GSupported = DeviceConnect.isRunningTest() || wifiManager.is5GHzBandSupported
        if (is5GSupported && isWifiConnected("\"" + hub.wifi5GSSID + "\"", wifiManager)) {
            connectedHub.SSID = hub.wifi5GSSID
            return connectedHub
        }
        if (isWifiConnected("\"" + hub.wifi2GSSID + "\"", wifiManager)) {
            connectedHub.SSID = hub.wifi2GSSID
            return connectedHub
        }
        return null
    }

    private fun isWifiConnected(ssid: String, wifiManager: WifiManager): Boolean {
        return if (wifiManager.isWifiEnabled) {
            wifiManager.connectionInfo.ssid.equals(ssid, true)
        } else false
    }

    fun isDeviceDetected(hub: Hub): Boolean {
        val mScanResults: List<ScanResult> = wifiManager.scanResults

        for (results in mScanResults) {
            if (results.SSID.equals(hub.wifi2GSSID) || results.SSID.equals(hub.wifi5GSSID)) {
                return true
            }
        }
        return false
    }

    fun getWifiInfoFrequencyValue(): Int {
        if (wifiManager.isWifiEnabled) {
            return wifiManager.connectionInfo.frequency
        }
        return 0
    }

    fun getWifiInfoFrequency(): String? {
        return if (wifiManager.isWifiEnabled) {
            val frequency = wifiManager.connectionInfo.frequency
            return when {
                is5GHz(frequency) -> "5GHz"
                is24GHz(frequency) -> "2.4GHz"
                else -> return null
            }
        } else null
    }

    private fun is5GHz(freqMhz: Int): Boolean {
        return freqMhz in 5160..5865
    }

    private fun is24GHz(freqMhz: Int): Boolean {
        return freqMhz in 2412..2484
    }
}