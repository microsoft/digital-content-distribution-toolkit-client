// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.test

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.msr.bine_android.R
import com.msr.bine_android.base.BaseInstrumentationTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginSteps: BaseInstrumentationTest() {

    @Rule
    var activityTestRule = ActivityTestRule(LoginActivity::class.java, false, false)

    private var activity: Activity? = null

    @Before("@login-feature")
    override fun setup() {
        super.setup()

        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity

        unlockScreen(activity)
    }

    @After("@login-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()
    }

    @Given("^I am on login screen")
    fun I_am_on_login_screen() {
        Assert.assertNotNull(activity)
    }

    @When("^I input phone number (\\S+)$")
    fun I_input_phone_number(phone: String?) {
        Espresso.onView(withId(R.id.login_phone_edit)).perform(ViewActions.replaceText(phone))
    }

    @When("^I press submit button$")
    fun I_press_submit_button() {
        Espresso.onView(withId(R.id.login_button)).perform(ViewActions.click())
    }

    @When("^I press verify button$")
    fun I_press_verify_button() {
        Espresso.onView(withId(R.id.login_button)).perform(ViewActions.click())
    }

    @When("^I confirm age consent")
    fun I_confirm_age_consent() {
        val consent = onView(
                Matchers.allOf(withId(R.id.login_otp_age_consent), withText("I confirm my age is 18 and above")))
        consent.perform(ViewActions.click())
    }

    @When("^I want to change language")
    fun I_want_to_change_language() {
        Espresso.onView(withId(R.id.login_language_select)).perform(ViewActions.click())
    }

    @When("^I select (\\S+) and (\\S+)$")
    fun I_select_language(language: String?, submit: String?) {
        Espresso.onView(ViewMatchers.withText(language)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(submit)).perform(ViewActions.click())
    }

    @When("I am on OTP Screen")
    fun I_am_on_otp_screen() {
        I_am_on_login_screen()
        I_input_phone_number("9999999999")
        I_press_submit_button()
        I_should_see_OTP_screen("9999999999")
    }

    @When("Two minutes have passed")
    fun two_minutes_have_passed(){
        val resendOTP = onView(
                Matchers.allOf(withId(R.id.login_resend_code), withText("RESEND OTP"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.login_otp_parent_layout),
                                        1),
                                6),
                        isDisplayed()))
        resendOTP.waitUntilVisible(120001)
    }

    @When("I enter OTP (\\S+)\$")
    fun I_enter_OTP(otp:String) {
        val oTPChildEditText2 = onView(
                Matchers.allOf(childAtPosition(
                        Matchers.allOf(withId(R.id.login_otp_edittext),
                                childAtPosition(
                                        withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                        3)),
                        0),
                        isDisplayed()))
        oTPChildEditText2.perform(ViewActions.click())
        oTPChildEditText2.perform(ViewActions.replaceText(otp), ViewActions.closeSoftKeyboard())
    }

    @Then("^I should see (\\S+)$")
    fun I_should_see_changed_language(language: String) {
        if (language == "हिन्दी") hasErrorText("फोन नंबर डालें") else if (language == "English") hasErrorText("Enter phone number") else if (language == "ಕನ್ನಡ") hasErrorText("ಫೋನ್ ಸಂಖ್ಯೆಯನ್ನು ನಮೂದಿಸಿ")
    }

    @Then("^I should see OTP Screen (\\S+)\$")
    fun I_should_see_OTP_screen(phone: String) {
        onView(withText("Enter 6-digit Pin")).check(matches(isDisplayed()))
        val phoneText = onView(
                Matchers.allOf(withText(endsWith("+91 9999999999"))))
        phoneText.check(matches(isDisplayed()))
    }

    @Then("^I should see the resend OTP timer")
    fun I_should_see_OTP_resent_timer() {
        val timerText = onView(
                Matchers.allOf(withId(R.id.login_otp_timer_text)))
        timerText.check(matches(isDisplayed()))
    }

    @Then("^Age consent option")
    fun I_should_see_age_consent_option() {
        val consent = onView(
                Matchers.allOf(withId(R.id.login_otp_age_consent), withText("I confirm my age is 18 and above")))
        consent.check(matches(isDisplayed()))
    }
    @Then("^I should see invalid OTP error")
    fun I_should_see_invalid_OTP_error() {
        val textView3 = onView(
                Matchers.allOf(withId(R.id.login_otp_error), withText("Wrong PIN. Please enter correct PIN")))
        textView3.check((matches(isDisplayed())))
    }

    @Then("^I want to resend OTP")
    fun I_should_resend_OTP() {
        val resendOTP = onView(
                Matchers.allOf(withId(R.id.login_resend_code), withText("RESEND OTP"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.login_otp_parent_layout),
                                        1),
                                6),
                        isDisplayed()))
        resendOTP.perform(ViewActions.click())
    }

    @Then("Receive OTP resend confirmation")
    fun Receive_OTP_resend_confirmation() {
        val textView2 = onView(
                Matchers.allOf(withText("A six digit code has been\nsent to your mobile\nnumber"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.custom),
                                        0),
                                1),
                        isDisplayed()))
        textView2.check(matches(withText("A six digit code has been\nsent to your mobile\nnumber")))
    }

    @When("^I read terms and conditions")
    fun I_read_all(){
        try {
            val timerText = onView(
                    Matchers.allOf(withId(R.id.tnc_header)))
            timerText.check(matches(isDisplayed()))
        }
        catch (e: NoMatchingViewException) {
            two_minutes_have_passed()
            I_should_resend_OTP()
            onView(withText(startsWith("A six digit code has been"))).perform(ViewActions.pressBack())
            I_enter_OTP("123456")
            I_press_submit_button()

            val headerText = onView(
                    Matchers.allOf(withId(R.id.tnc_header)))
            headerText.check(matches(isDisplayed()))
        }
        finally {
            val floatingActionButton = onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.button_scroll_down),
                            childAtPosition(
                                    childAtPosition(
                                            ViewMatchers.withId(android.R.id.content),
                                            0),
                                    2),
                            isDisplayed()))
            floatingActionButton.perform(ViewActions.click())
            Thread.sleep(2000)
        }
    }

}