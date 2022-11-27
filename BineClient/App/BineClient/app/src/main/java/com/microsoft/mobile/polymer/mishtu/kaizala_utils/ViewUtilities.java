package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Helper class for view related utility methods.
 */
public final class ViewUtilities {

    private ViewUtilities() {
        throw new AssertionError("Should not be instantiated.");
    }

    /**
     * If some part of this view is not clipped by any of its parents, then return that area in r
     * in global (root) coordinates.
     *
     * @throws IllegalArgumentException If the layout in null.
     */
    public static Rect getLayoutAsRectangle(View layout) {
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null.");
        }

        Rect rect = new Rect();
        layout.getGlobalVisibleRect(rect);

        return rect;
    }

    public static boolean isActivityAlive(Activity activity) {
        return activity != null &&
                !(activity.isDestroyed() || activity.isFinishing());
    }

    public static Activity getActivity(View view) {
        return getActivity(view != null ? view.getContext() : null);
    }

    public static Activity getActivity(ArrayAdapter adapter) {
        return getActivity(adapter != null ? adapter.getContext() : null);
    }

    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static void runOnUI(final View view, final Runnable runnable) {
        runOnUI(getActivity(view), runnable);
    }

    public static void runOnUI(final Fragment fragment, final Runnable runnable) {
        final Activity activity = fragment != null ? fragment.getActivity() : null;
        if (isActivityAlive(activity) && runnable != null) {
            activity.runOnUiThread(() -> {
                final Activity activity1 = fragment != null ? fragment.getActivity() : null;
                if (isActivityAlive(activity1)) {
                    runnable.run();
                }
            });
        }
    }

    public static void runOnUI(final Activity activity, final Runnable runnable) {
        if (isActivityAlive(activity) && runnable != null) {
            activity.runOnUiThread(() -> {
                if (isActivityAlive(activity)) {
                    runnable.run();
                }
            });
        }
    }

    public static void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    public static void showKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isViewPresent(Activity activity, int viewId) {
        return activity.findViewById(viewId) != null;
    }

    public static void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
