package com.msr.bine_android.base

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.junit.internal.matchers.TypeSafeMatcher

/**
* Custom matcher to assert equal EditText.setError();
*/
class TextMatcher(private val mExpectedError: String) : TypeSafeMatcher<View?>() {
    override fun matchesSafely(view: View?): Boolean {
        if (view !is EditText) {
            return false
        }
        return mExpectedError == view.error.toString()
    }

    override fun describeTo(description: Description) {
        description.appendText("with error: $mExpectedError")
    }
}