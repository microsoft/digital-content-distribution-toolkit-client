package com.msr.bine_android.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.msr.bine_android.R
import com.msr.bine_android.base.BaseInstrumentationTest
import com.msr.bine_sdk.BineConnect
import com.msr.bine_sdk.models.Hub
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.Is
import org.junit.Rule
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert

class HomeHubConnectionSteps : BaseInstrumentationTest() {

    @Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    private var activity: Activity? = null

    @Before("@homeHub-feature")
    override fun setup() {
        super.setup()
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        val info = wifiManager!!.connectionInfo
        val ssid = info.ssid
        if(!TextUtils.isEmpty(ssid)) {
            val wifiName = ssid.substring(1, ssid.length - 1);
            Log.e("here wifiname", "-- $wifiName")
            dataRepository?.setHubSSID(wifiName)
            val hub = Hub()
            hub.wifi2GSSID = wifiName
            hub.wifi5GSSID = wifiName
            bineConnect = BineConnect.getInstance(context)
            bineConnect?.isConnected(context, Hub())
        }

        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity
        unlockScreen(activity)
    }

    @After("@homeHub-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()
    }

    @When("open Home Screen")
    fun open_home_screen() {
        Assert.assertNotNull(activity)
        try {
            val skipButton = onView(ViewMatchers.withText("skip"))
            skipButton.perform(click())
        }
        catch (e: NoMatchingViewException) { }
        Thread.sleep(2000)
    }

    @Then("change ip to hub ip")
    fun I_should_set_hub_ip_address() {
        val constraintLayout = onView(
                withId(com.msr.bine_android.R.id.action_profile))
        constraintLayout.perform(click())
        Thread.sleep(2000)
        val appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Settings"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()))
        appCompatTextView.perform(click())
        Thread.sleep(2000)
        onView(withId(com.msr.bine_android.R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(6, click()))
        onView(withId(R.id.idInput)).perform(replaceText("10.0.2.2"))
        onView(withId(R.id.idInput)).perform(closeSoftKeyboard())

        val appCompatTextView4 = onView(
                allOf(withId(R.id.idBtnOk), withText("Ok"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(Is.`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                2),
                        isDisplayed()))
        appCompatTextView4.perform(click())

        val appCompatImageButton = onView(
                allOf(withContentDescription("\u200E\u200F\u200E\u200E\u200E\u200E\u200E\u200F\u200E\u200F\u200F\u200F\u200E\u200E\u200E\u200E\u200E\u200E\u200F\u200E\u200E\u200F\u200E\u200E\u200E\u200E\u200F\u200F\u200F\u200F\u200F\u200F\u200F\u200F\u200F\u200F\u200E\u200F\u200E\u200E\u200E\u200F\u200F\u200E\u200F\u200E\u200E\u200E\u200F\u200F\u200E\u200E\u200E\u200F\u200F\u200F\u200F\u200E\u200F\u200E\u200E\u200E\u200E\u200F\u200F\u200E\u200F\u200F\u200E\u200F\u200E\u200E\u200F\u200E\u200E\u200F\u200E\u200E\u200E\u200E\u200E\u200E\u200F\u200E\u200F\u200E\u200E\u200E\u200E\u200F\u200F\u200F\u200E\u200E\u200E\u200E\u200ENavigate up\u200E\u200F\u200E\u200E\u200F\u200E"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withId(R.id.idParentView),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton.perform(click())
        Thread.sleep(5000)
    }

    @Then("check is hub connected")
    fun check_is_hub_connected() {
        val textView = onView(
                allOf(withId(R.id.toolbar_title), withText("Connection Active"),
                        withParent(allOf(withId(R.id.toolbar),
                                withParent(withId(R.id.action_bar)))),
                        isDisplayed()))
        textView.check(matches(withText("Connection Active")))
    }


    @Then("check is hub content displaying")
    fun check_is_hub_content_displaying() {
        Thread.sleep(5000)
        onView(allOf(withId(R.id.btnDownload), withText("DOWNLOAD"),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.recyclerView),
                                0),
                        9),
                isDisplayed()))
    }


    @Then("check content is download is working")
    fun check_content_is_download_working() {
        Thread.sleep(5000)
        val appCompatButton6 = onView(
                allOf(withId(R.id.btnDownload), withText("DOWNLOAD"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerView),
                                        0),
                                9),
                        isDisplayed()))
        appCompatButton6.perform(click())
        val tabView = onView(
                allOf(withContentDescription("Downloads"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tabs),
                                        0),
                                1),
                        isDisplayed()))
        tabView.perform(click())
        Thread.sleep(5000)
        val textView = onView(
                allOf(withId(R.id.folder_item_cancel), withText("CANCEL"),
                        withParent(withParent(withId(R.id.downloads_recyclerview))),
                        isDisplayed()))
        textView.check(matches(withText("CANCEL")))
    }

}