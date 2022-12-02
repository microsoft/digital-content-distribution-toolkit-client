// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.test

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
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
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert

class HomeSteps : BaseInstrumentationTest() {
    @Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    private var activity: Activity? = null

    @Before("@home-feature")
    override fun setup() {
        super.setup()
        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity
        unlockScreen(activity)
    }

    @After("@home-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()
    }

    @When("I am on Home Screen")
    fun I_am_on_home_screen() {
        Assert.assertNotNull(activity)
        try {
            val skipButton = onView(ViewMatchers.withText("skip"))
            skipButton.perform(click())
        }
        catch (e: NoMatchingViewException) { }
    }

    @Then("I should see Home Screen")
    fun I_should_see_home_screen() {
    }

    @Then("I should switch tabs")
    fun I_should_switch_tabs() {
        val tabView = onView(
                allOf(withContentDescription("Downloads"),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.msr.bine_android.R.id.tabs),
                                        0),
                                1),
                        isDisplayed()))
        tabView.perform(click())

        val tabView2 = onView(
                allOf(withContentDescription("Home"),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.msr.bine_android.R.id.tabs),
                                        0),
                                0),
                        isDisplayed()))
        tabView2.perform(click())
    }


    @Then("I should see category list")
    fun I_should_see_category() {
        onView(withId(com.msr.bine_android.R.id.categoryRecyclerview)).check(matches(isDisplayed()))
        val appCompatTextView = onView(
                allOf(withId(R.id.idTitle), withText("HINDI"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categoryRecyclerview),
                                        1),
                                0),
                        isDisplayed()))
        appCompatTextView.perform(click())

        val appCompatTextView2 = onView(
                allOf(withId(R.id.idTitle), withText("ALL"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categoryRecyclerview),
                                        0),
                                0),
                        isDisplayed()))
        appCompatTextView2.perform(click())
    }

    @Then("check category selection")
    fun check_category_selection() {
        Thread.sleep(8000)
        val appCompatTextView2 = onView(
                allOf(withId(R.id.idTitle), withText("KANNADA"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categoryRecyclerview),
                                        2),
                                0),
                        isDisplayed()))
        appCompatTextView2.perform(click())

        val textView = onView(
                allOf(withId(R.id.idTitle), withText("Anjada Gandu"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        textView.check(matches(withText("Anjada Gandu")))

        val appCompatTextView3 = onView(
                allOf(withId(R.id.idTitle), withText("TAMIL"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.categoryRecyclerview),
                                        3),
                                0),
                        isDisplayed()))
        appCompatTextView3.perform(click())

        val textView3 = onView(
                allOf(withId(R.id.idTitle), withText("Kochadaiiyaan - The Legend -Tamil"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        textView3.check(matches(withText("Kochadaiiyaan - The Legend -Tamil")))
        Thread.sleep(1000)
        onView(withId(com.msr.bine_android.R.id.categoryRecyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(4, click()))

        val textView5 = onView(
                allOf(withId(R.id.idNoData), withText("ENGLISH movies coming soon"),
                        withParent(allOf(withId(R.id.idParentView),
                                withParent(withId(R.id.idViewPager)))),
                        isDisplayed()))
        textView5.check(matches(withText("ENGLISH movies coming soon")))

    }

    @Then("I should see content list")
    fun I_should_see_content() {
        Thread.sleep(8000)
        onView(withId(com.msr.bine_android.R.id.recyclerView)).check(matches(isDisplayed()))
        val button = onView(
                allOf(withId(R.id.btnDownload), withText("FIND MOVIE BOOTH"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))
    }


    @Then("I should set network ssid id")
    fun I_should_set_network_ssid_id() {
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

        onView(withId(com.msr.bine_android.R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(6, click()))
        onView(withId(R.id.idInput)).perform(replaceText("Network5"))
        onView(withId(R.id.idInput)).perform(closeSoftKeyboard())
        Espresso.pressBack()
        val appCompatTextView2 = onView(
                allOf(withId(R.id.idBtnOk), withText("Ok"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                2),
                        isDisplayed()))
        appCompatTextView2.perform(click())

    }

    @Then("check is search is working")
    fun check_is_search_is_working() {
        val appCompatImageView = onView(
                allOf(withId(R.id.search_button), withContentDescription("\u200E\u200F\u200E\u200E\u200E\u200E\u200E\u200F\u200E\u200F\u200F\u200F\u200E\u200E\u200E\u200E\u200E\u200E\u200F\u200E\u200E\u200F\u200E\u200E\u200E\u200E\u200F\u200F\u200F\u200F\u200F\u200F\u200E\u200F\u200E\u200F\u200F\u200F\u200F\u200E\u200F\u200F\u200E\u200F\u200F\u200E\u200F\u200F\u200F\u200F\u200F\u200F\u200E\u200F\u200E\u200E\u200F\u200E\u200F\u200E\u200E\u200F\u200F\u200E\u200E\u200E\u200E\u200E\u200E\u200E\u200E\u200E\u200F\u200F\u200E\u200F\u200E\u200F\u200F\u200F\u200E\u200E\u200F\u200F\u200F\u200F\u200F\u200E\u200E\u200F\u200F\u200F\u200E\u200E\u200E\u200ESearch\u200E\u200F\u200E\u200E\u200F\u200E"),
                        childAtPosition(
                                allOf(withId(R.id.search_bar),
                                        childAtPosition(
                                                withId(R.id.action_search),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageView.perform(click())

        val searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete.perform(replaceText("fffff"), closeSoftKeyboard())

        val textView = onView(
                allOf(withId(R.id.idNoData), withText("No search results found…"),
                        withParent(allOf(withId(R.id.idParentView),
                                withParent(withId(R.id.idViewPager)))),
                        isDisplayed()))
        textView.check(matches(withText("No search results found…")))


        val textView3 = onView(
                allOf(withId(R.id.idNoData), withText("No search results found…"),
                        withParent(allOf(withId(R.id.idParentView),
                                withParent(withId(R.id.idViewPager)))),
                        isDisplayed()))
        textView3.check(matches(withText("No search results found…")))
    }

    @Then("checking content list click")
    fun checking_content_list_click() {
        val appCompatButton6 = onView(
                allOf(withId(R.id.btnDownload), withText("Find Movie Booth"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        appCompatButton6.perform(click())
    }


    @Then("I should see search content")
    fun I_should_see_search_content() {
        onView(withId(com.msr.bine_android.R.id.action_search)).check(matches(isDisplayed()))
        onView(withId(com.msr.bine_android.R.id.action_search)).perform(click())
        Thread.sleep(2000)
        val searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete.perform(replaceText("Kochadaiiyaan"), closeSoftKeyboard())
        Thread.sleep(1000)
        val textView3 = onView(
                allOf(withId(R.id.idTitle), withText("Kochadaiiyaan - The Legend -Tamil"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        textView3.check(matches(withText("Kochadaiiyaan - The Legend -Tamil")))
    }


    @Then("I should view settings")
    fun I_should_see_settings() {
        val constraintLayout = onView(
                withId(com.msr.bine_android.R.id.action_profile))
        constraintLayout.perform(click())

        val textView9 = onView(
                allOf(withText("Settings"),
                        isDisplayed()))
        textView9.check(matches(withText("Settings")))

        val textView = onView(
                allOf(withText("Logout"),
                        isDisplayed()))
        textView.check(matches(withText("Logout")))
    }
}