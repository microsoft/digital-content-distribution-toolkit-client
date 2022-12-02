// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.base

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import com.msr.bine_sdk.BineConnect
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
open class BaseInstrumentationTest {

    lateinit var context: Context

    var dataRepository: DataRepository? = null

    var bineConnect: BineConnect? = null

    /*@BindValue
    lateinit var appDataBase: AppDatabase

    @BindValue
    lateinit var folderDao: FolderDao

    @BindValue
    lateinit var dataRepository: DataRepository

    @BindValue
    lateinit var bineAPI: BineAPI

    @BindValue
    lateinit var bineConnect: BineConnect

    @BindValue
    lateinit var downloader: Downloader

    @BindValue
    lateinit var contentEntityDao: ContentEntityDao

    @BindValue
    lateinit var cartEntityDao: CartEntityDao

    @BindValue
    lateinit var folderEntityDao: FolderEntityDao

    @BindValue
    lateinit var sharedPreferenceStore: SharedPreferenceStore

    @BindValue
    lateinit var sharedPrefs: BineSharedPreference*/

    @Rule(order = 2)
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_WIFI_STATE",
                    "android.permission.WRITE_EXTERNAL_STORAGE")



    @Before
    open fun setup() {
        context = getInstrumentation().targetContext
        /*dataRepository = mock(DataRepository::class.java)
        bineConnect = mock(BineConnect::class.java)*/

        /*sharedPrefs = mock()
        appDataBase = mock()
        folderDao = mock()
        contentEntityDao = mock()
        cartEntityDao = mock()
        folderEntityDao = mock()
        sharedPreferenceStore = mock()
        dataRepository = mock()
        bineAPI = mock()
        bineConnect = mock()
        downloader = mock()*/

        /*
       dataRepository = DataRepository.getInstance(AppDatabase.getDatabase(context).folderDao(),
               SharedPreferenceStore(context))
       dataRepository.tncAccepted(false)
       dataRepository.saveUserId("")
       dataRepository.firstLogin(true)
       val app = androidx.test.InstrumentationRegistry.getTargetContext().applicationContext as BineApplication
      DaggerUITestAppComponent
               .builder()
               .appModule(UITestAppModule(app))
               .build().into(this)*/

        /*runBlocking {
            Mockito.`when`(bineAPI.verifyPhone(LoginRequest("9823820919", ""))).thenReturn(LoginResponse(false, "", Error.NETWORK_ERROR))
        }*/
    }

    @After
    open fun tearDown() {
        // Required for test scenarios with multiple activities or scenarios with more cases
    }

    fun waitFor(i: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for " + i + "milliseconds"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(i.toLong())
            }
        }
    }

    fun hasText(expectedError: String): Matcher<in View?>? {
        return TextMatcher(expectedError)
    }

    internal fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    fun ViewInteraction.waitUntilVisible(timeout: Long): ViewInteraction {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeout

        do {
            try {
                check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                return this
            } catch (e: NoMatchingViewException) {
                Thread.sleep(50)
            }
        } while (System.currentTimeMillis() < endTime)

        throw TimeoutException()
    }

    open fun unlockScreen(activity: Activity?) {
        val wakeUpDevice = Runnable {
            activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        activity?.runOnUiThread(wakeUpDevice)
        //onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(1).perform(click());
    }
}