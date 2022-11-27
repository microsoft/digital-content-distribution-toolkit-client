package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

/*
 * Copyright 2013 Niek Haarman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file has been modified by MICROSOFT on 1/21/2017
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ToolTipRelativeLayout extends RelativeLayout {

    public static final String LOG_TAG = "ToolTipRelativeLayout";
    public static final String ID = "id";

    public boolean mShouldSoftDismiss = true;

    public ToolTipRelativeLayout(final Context context) {
        super(context);
    }

    public ToolTipRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolTipRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Shows a {@link ToolTipView} based on given {@link ToolTip} at the proper
     * location relative to given {@link View}.
     *
     * @param toolTip
     *            the ToolTip to show.
     *
     * @return the ToolTipView that was created.
     */
    public ToolTipView showToolTipForView(final ToolTip toolTip) {
        final ToolTipView toolTipView = new ToolTipView(getContext());
        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = toolTipView.getLayoutParams();
        if (!toolTip.mShowFullWidth) {
            width *= 0.65;
        }
        mShouldSoftDismiss = toolTip.mShouldSoftDismiss;
        params.width = width;
        toolTipView.setLayoutParams(params);
        toolTipView.setToolTip(toolTip);
        addView(toolTipView);
        return toolTipView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mShouldSoftDismiss && ToolTipManager.getInstance().clearToolTipsOnScreen()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
