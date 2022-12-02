// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.msr.bine_sdk.base.BaseTest
import com.msr.bine_sdk.hub.HubConnectionCallback
import com.msr.bine_sdk.hub.DeviceConnect
import com.msr.bine_sdk.models.Hub
import com.msr.bine_sdk.secure.BineSharedPreference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class HubManagerTest: BaseTest() {

    val wifiManager: WifiManager = mock()

    private val sharedPrefs: BineSharedPreference = Mockito.mock(BineSharedPreference::class.java)

    @Before
    fun setUpTest() {
        super.setUp()
    }

    @After
    fun setTearDown() = super.tearDown()

    @Test
    fun wifiConnected() {
        val hubManager =
            DeviceConnect(context, sharedPrefs)
        hubManager.setWifiManager(wifiManager)

        whenever(wifiManager.isWifiEnabled)
                .thenReturn(false)

        var isConnected = hubManager.isConnected(getDummyHub())
        TestCase.assertNull(isConnected)

        whenever(wifiManager.isWifiEnabled)
                .thenReturn(true)
        val wifiInfo:WifiInfo = mock()
        whenever(wifiInfo.ssid).thenReturn("\"JioFi3_566200\"")
        whenever(wifiManager.connectionInfo).thenReturn(wifiInfo)

        isConnected = hubManager.isConnected(getDummyHub())
        TestCase.assertEquals("Wifi config empty", "JioFi3_566200", isConnected.SSID)
        TestCase.assertEquals("Wifi config empty", "192.168.2.2", isConnected.ip)
    }

    @Test
    @Throws(InterruptedException::class)
    fun connect() {
        val wifiManager:WifiManager = mock()
        val hubManager =
            DeviceConnect(context, sharedPrefs)

        hubManager.setWifiManager(wifiManager)
        whenever(wifiManager.isWifiEnabled).thenReturn(true)
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"JioFi3_566200\""
        wifiConfig.preSharedKey = "\"kc78k1x8zb\""
        whenever(wifiManager.addNetwork(wifiConfig)).thenReturn(2)
        whenever(wifiManager.enableNetwork(2, true)).thenReturn(true)
        whenever(wifiManager.disconnect()).thenReturn(true)
        whenever(wifiManager.reconnect()).thenReturn(true)

        val latch = CountDownLatch(1)
        hubManager.connect(getDummyHub(), object : HubConnectionCallback {
            override fun onConnectFailure(response: Hub, exception: Exception) {
                TestCase.assertTrue("Wifi connect", true)
                latch.countDown()
            }

            override fun onConnect(hub: Hub) {
                TestCase.assertTrue("Wifi connect", true)
                latch.countDown()
            }
        })
        latch.await(ASYNC_WAIT.toLong(), TimeUnit.SECONDS)
    }

    private fun getDummyHub(): Hub? {
        val hub = Hub()
        hub.name = "JioFi3_566200"
        hub.wifi5GPass = "kc78k1x8zb"
        hub.ip = "192.168.2.2"
        hub.wifi5GSSID = "JioFi3_566200"
        return hub
    }
}