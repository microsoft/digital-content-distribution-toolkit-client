// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.test

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import com.msr.bine_android.R
import com.msr.bine_android.activities.PermissionsActivity
import com.msr.bine_android.base.BaseInstrumentationTest
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule

class PermissionSteps: BaseInstrumentationTest() {
    @Rule
    var activityTestRule = ActivityTestRule(PermissionsActivity::class.java)


    private var activity: Activity? = null

    @Before("@permissions-feature")
    override fun setup() {
        super.setup()

        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity

        unlockScreen(activity)
    }

    @After("@permissions-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()
    }

    @Given("^I am on Permissions Screen")
    fun I_am_on_permissions_screen() {
        Assert.assertNotNull(activity)
    }
    @When("^I accept permissions and proceed")
    fun I_accept_permissions() {
        //I_should_be_allowed_to_accept_permissions()
    }

    @Then("^I should be able to accept permissions and proceed")
    fun I_should_be_allowed_to_accept_permissions() {
        val appCompatButton8 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.idBtnGivePermission), ViewMatchers.withText("Give Permission"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.idParentView),
                                        childAtPosition(
                                                ViewMatchers.withId(android.R.id.content),
                                                0)),
                                10),
                        ViewMatchers.isDisplayed()))
        appCompatButton8.perform(ViewActions.click())
    }
}