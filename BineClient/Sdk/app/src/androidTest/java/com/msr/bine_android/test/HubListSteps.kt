package com.msr.bine_android.test

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.msr.bine_android.R
import com.msr.bine_android.base.BaseInstrumentationTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Assert
import org.junit.Rule

class HubListSteps : BaseInstrumentationTest() {
    @Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    private var activity: Activity? = null

    @Before("@hub-feature")
    override fun setup() {
        super.setup()
        activityTestRule.launchActivity(Intent())
        activity = activityTestRule.activity
    }

    @After("@hub-feature")
    override fun tearDown() {
        super.tearDown()
        activityTestRule.finishActivity()

    }
    @Given("I am seen the content list")
    fun I_see_content_list() {
        Assert.assertNotNull(activity)
        try {
            val skipButton = onView(ViewMatchers.withText("skip"))
            skipButton.perform(click())
        }
        catch (e: NoMatchingViewException) { }
    }

    @Given("I want to find a movie booth")
    fun I_want_to_find_a_movie_booth() {
        Thread.sleep(2000)
        val appCompatButton6 = onView(
                allOf(withId(R.id.btnDownload), withText("Find Movie Booth"),
                        withParent(withParent(withId(R.id.recyclerView))),
                        isDisplayed()))
        appCompatButton6.perform(click())
    }


    @Then("I should see hubs list")
    fun I_should_see_hublist() {
        val title = onView(withText("Nearby Movie Booths"))
        title.waitUntilVisible(5000)
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        val textView = onView(
                allOf(withId(R.id.idAddress), withText("Zen Gardens, Artillery Road, Jeevan Kendra Layout, Jogupalya, Bengaluru, Karnataka, India"),
                        withParent(allOf(withId(R.id.idParentView),
                                withParent(withId(R.id.recyclerView)))),
                        isDisplayed()))
        textView.check(matches(withText("Zen Gardens, Artillery Road, Jeevan Kendra Layout, Jogupalya, Bengaluru, Karnataka, India")))
    }

    @Given("I see list of hubs near me")
    fun i_see_list_of_hubs_near_me() {
        I_see_content_list()
        I_want_to_find_a_movie_booth()
    }

    @Given("I want to see a hub details")
    fun I_should_list_clicked() {
        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }

    @Then("I should see hubs detailed info")
    fun I_should_see_hub_list_screen() {
        val textView4 = onView(
                allOf(withId(R.id.idTitle), withText("Apurv-Dev-Hub"),
                        withParent(allOf(withId(R.id.idRootView),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        textView4.check(matches(withText("Apurv-Dev-Hub")))

        val textView5 = onView(
                allOf(withId(R.id.idAddress), withText("Zen Gardens, Artillery Road, Jeevan Kendra Layout, Jogupalya, Bengaluru, Karnataka, India"),
                        withParent(allOf(withId(R.id.idRootView),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        textView5.check(matches(withText("Zen Gardens, Artillery Road, Jeevan Kendra Layout, Jogupalya, Bengaluru, Karnataka, India")))

        val textView6 = onView(
                allOf(withId(R.id.idDistance), withText(startsWith("Approx")),
                        withParent(allOf(withId(R.id.idRootView),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        textView6.check(matches(withText(startsWith("Approx"))))

        val textView7 = onView(
                allOf(withId(R.id.idLblPaymentOpt), withText("Payment Options"),
                        withParent(allOf(withId(R.id.idRootView),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        textView7.check(matches(withText("Payment Options")))

        val button = onView(
                allOf(withId(R.id.idBtnNavigation), withText("START NAVIGATION"), withParent(allOf(withId(R.id.idRootView),
                        withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))
    }

    @Then("Allowed to navigate to hub")
    fun check_hub_navigation_clickable() {
        val button = onView(
                allOf(withId(R.id.idBtnNavigation), withText("START NAVIGATION"), withParent(allOf(withId(R.id.idRootView),
                        withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        button.perform(click())
      /*  Thread.sleep(5000)
        val imageView3 = onView(
                allOf(withParent(allOf(withContentDescription("Navigate up"),
                        withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java)))),
                        isDisplayed()))
        imageView3.check(matches(isDisplayed()))
        imageView3.perform(click())*/
    }

    @Then("Allowed to call the hub contact")
    fun check_call_dial_clickable() {
        val textView9 = onView(
             allOf(withId(R.id.idBtnCall),
                       withParent(allOf(withId(R.id.idRootView),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()))
        textView9.perform(click())
    }

}