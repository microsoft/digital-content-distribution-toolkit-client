// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.base
import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.msr.bine_android.data.SharedPreferenceStore
import com.msr.bine_android.data.entities.*
import com.msr.bine_sdk.secure.BineSharedPreference
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject


@Config(manifest = "src/main/AndroidManifest.xml")
open class AppBaseTest : Application(){

    @Inject
    lateinit var bineSharedPreference: BineSharedPreference

    @Inject
    lateinit var sharedPreference: SharedPreferenceStore

    @Inject
    lateinit var folderDao: FolderDao

    @Inject
    lateinit var folderEntityDao: FolderEntityDao

    @Inject
    lateinit var contentEntityDao: ContentEntityDao

    @Inject
    lateinit var cartEntityDao: CartEntityDao

    open val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun tearDown() {

    }
}