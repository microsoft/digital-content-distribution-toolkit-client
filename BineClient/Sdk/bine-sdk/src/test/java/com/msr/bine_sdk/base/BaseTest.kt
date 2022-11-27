package com.msr.bine_sdk.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.auth.AuthService
import com.msr.bine_sdk.cloud.CloudService
import com.msr.bine_sdk.cloud.DeviceLocalService
import com.msr.bine_sdk.secure.BineSharedPreference
import com.msr.bine_sdk.test_di.DaggerBineTestComponent
import com.msr.bine_sdk.test_di.TestBineModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.mockito.MockitoAnnotations
import javax.inject.Inject

open class BaseTest {
    val ASYNC_WAIT = 10

    val context: Context = ApplicationProvider.getApplicationContext<Context>()

    @ExperimentalCoroutinesApi
    val testDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    val testScope = TestCoroutineScope(testDispatcher)

    @Inject
    lateinit var appAPIs: BineAPI

    @Inject
    lateinit var sharedPreferences: BineSharedPreference

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var cloudService: CloudService

    @Inject
    lateinit var deviceLocalService: DeviceLocalService

    @Inject
    lateinit var hubManagementService: HubManagementService

    @ExperimentalCoroutinesApi
    @Before
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
        Dispatchers.setMain(testDispatcher)
        DaggerBineTestComponent
                .builder()
                .bineModule(TestBineModule(context))
                .build().into(this)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }

}