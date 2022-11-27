package com.msr.bine_android.test

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.msr.bine_android.R
import com.msr.bine_android.base.BaseInstrumentationTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule

class TnCSteps: BaseInstrumentationTest() {
    @Rule
    var activityTestRule = ActivityTestRule(TermsAndConditionsActivity::class.java)

    private var activity: Activity? = null

    @Before("@tnc-feature")
    override fun setup() {
        super.setup()

        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity

        unlockScreen(activity)
    }

    @After("@tnc-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()
    }

    @Given("^I am on Terms and Conditions screen")
    fun I_am_on_tnc_screen() {
        Assert.assertNotNull(activity)
    }

    @When("^I read conditions until bottom")
    fun I_read_until_bottom() {
        val floatingActionButton = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.button_scroll_down),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(android.R.id.content),
                                        0),
                                2),
                        ViewMatchers.isDisplayed()))
        floatingActionButton.perform(ViewActions.click())
    }

    @When("^I accept terms and conditions to proceed")
    fun I_accept_tnc() {
        I_should_be_allowed()
    }

    @When("^I opt to reject the conditions")
    fun I_want_to_reject() {
        val appCompatButton4 = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.button_reject), ViewMatchers.withText("Reject"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.bottom_sheet),
                                        childAtPosition(
                                                ViewMatchers.withClassName(Matchers.`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                                                1)),
                                0),
                        ViewMatchers.isDisplayed()))

        onView(withId(R.id.terms_scrollview))
                .perform(ViewActions.swipeDown())
        appCompatButton4.perform(ViewActions.click())
    }

    @Then("^I should not able to accept before reading")
    fun I_should_not_allowed() {
        onView(ViewMatchers.withId(R.id.button_accept)).check(matches(isEnabled()))
    }

    @Then("^I should be able to accept and proceed")
    fun I_should_be_allowed() {
        Thread.sleep(1000)
        onView(ViewMatchers.withId(R.id.button_accept)).perform(ViewActions.click())
    }

    @Then("^I should be shown a prompt to terminate")
    fun I_should_see_prompt_to_exit() {
        onView(ViewMatchers.withText("Select accept to proceed")).check(matches(isDisplayed()))
    }
}