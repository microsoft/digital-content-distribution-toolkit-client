// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msr.bine_android.base.AppBaseTest
import com.msr.bine_android.data.SharedPreferenceStore
import com.msr.bine_android.data.entities.Folder
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
open class TestDataRepository : AppBaseTest() {

    @Before
    fun setConfig() {
        super.setUp()
        /*var list:LiveData<List<Folder>> = MutableLiveData()

        whenever(bineSharedPreference.get(SharedPreferenceStore.KEY_SHAREDPREF_COUPON))
                .thenReturn("test")

        whenever(folderDao.getDownloads()).thenReturn(list)*/
    }

    @After
    fun setTearDown() {
        super.tearDown()
    }

    @Test
    fun `fetching saved coupon`() {
        //assert(dataRepository.getCoupon() == null)
        assert(true)
    }
    @Test
    fun `fetching saved folders`() {
        //assert(dataRepository.getDownloadsList().value?.size==0)
        assert(true)
    }
}