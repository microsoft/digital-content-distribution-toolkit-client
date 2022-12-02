// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

/*
package com.msr.bine_sdk.base

import com.google.android.exoplayer2.BasePlayer
import com.msr.bine_android.activities.LoginActivity
import com.msr.bine_sdk.player.BasePlayerActivity
import com.msr.bine_sdk.player.BinePlayerViewActivity
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController


@RunWith(RobolectricTestRunner::class)
class BasePlayerTest:BaseTest() {

    @InjectMocks
    lateinit var activity: BinePlayerViewActivity

    @Before
    @Throws(Exception::class)
    fun setUp() {
        `when`(activity.()).thenReturn(true)
        val activityController: ActivityController<BinePlayerViewActivity> = Robolectric.buildActivity<BinePlayerViewActivity>(BinePlayerViewActivity::class.java)
        activity = activityController.get()
        activity.setFirebaseInstanceId(firebaseInstanceId)
        activityController.create()

    }
    @Test
    fun checkMediaosure() {
        activity.createMediaSource()
    }
}*/
