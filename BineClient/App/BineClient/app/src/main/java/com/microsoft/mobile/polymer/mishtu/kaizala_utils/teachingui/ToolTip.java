package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

/*
 * Copyright 2013 Niek Haarman
 *
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils;

public class ToolTip {

    public enum Type {
        Contextual,
        FullPage,
        Inline
    }

    // Content View for this FRE type. Client can supply this view.
    // If not, ToolTipStore will initialize with default layout, depending on type of tooltip
    private View mContentView;

    private View mOverlayView;

    // Color of the layout
    private int mColor;

    public enum AnimationType {
        SLIDE_HORIZONTAL,
        SLIDE_VERTICAL,
        FADE,
        NONE
    }

    private CharSequence mText;
    private int mTextResId;
    private int mTextColor;
    private ToolTip.AnimationType mAnimationType;
    private boolean mShouldShowShadow;
    private Typeface mTypeface;
    private Type mType;
    private boolean mShouldShowBeak;
    private View mAnchorView;
    private View mParentLayout;
    private int dx;
    private int dy;
    private boolean mShouldSetMargin;
    private int mLeftMargin;
    private int mTopMargin;
    private int mRightMargin;
    private int mBottomMargin;
    public boolean mShowFullWidth;
    private Context mContext;
    private boolean mShouldShowDismissButton;
    private boolean mShouldShowQuickTip;
    public boolean mShouldSoftDismiss;
    private boolean mToolTipPointingUp;
    private boolean mShowToolTipOverlay;
    private int anchorViewX;
    private int anchorViewY;

    /**
     * Creates a new ToolTip without any values.
     */
    public ToolTip(Context context) {
        mContext = context;
        mColor = ContextCompat.getColor(context, R.color.tooltip_color);
        mText = null;
        mTypeface = null;
        mTextResId = 0;
        mContentView = null;
        mAnimationType = AnimationType.NONE;
        mShouldShowBeak = true;
        mShouldShowDismissButton = true;
        mShowFullWidth = false;
        mShouldSoftDismiss = true;
        mShouldSetMargin = false;
        mLeftMargin = 0;
        mTopMargin = 0;
        mRightMargin = 0;
        mBottomMargin = 0;
    }

    /**
     * Set the text to show. Has no effect when a content View is set using setContentView().
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withText(final CharSequence text) {
        mText = text;
        mTextResId = 0;
        return this;
    }

    /**
     * Set the text resource id to show. Has no effect when a content View is set using setContentView().
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withText(final int resId) {
        mTextResId = resId;
        mText = null;
        return this;
    }

    /**
     * Set the text resource id to show and the custom typeface for that view. Has no effect when a content View is set using setContentView().
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withText(final int resId, final Typeface tf) {
        mTextResId = resId;
        mText = null;
        withTypeface(tf);
        return this;
    }

    /**
     * Set the color of the ToolTip. Default is white.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withColor(final int color) {
        mColor = color;
        if (mContentView != null) {
            mContentView.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * Set the text color of the ToolTip. Default is white.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withTextColor(final int color) {
        mTextColor = color;
        return this;
    }

    /**
     * Set a custom content View for the ToolTip. This will cause any text that has been set to be ignored.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withContentView(final View view) {
        mContentView = view;
        return this;
    }

    public ToolTip withOverlayView(final View view) {
        mOverlayView = view;
        return this;
    }

    /**
     * Set the animation type for the ToolTip.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withAnimationType(final ToolTip.AnimationType animationType) {
        mAnimationType = animationType;
        return this;
    }

    /**
     * Set to show a shadow below the ToolTip.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withShadow() {
        mShouldShowShadow = true;
        return this;
    }

    /**
     * Set to NOT show a shadow below the ToolTip.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withoutShadow() {
        mShouldShowShadow = false;
        return this;
    }

    /**
     * Set to show Quicktip in text ToolTip.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withQuickTip() {
        mShouldShowQuickTip = true;
        return this;
    }

    public ToolTip withToolTipPointingTop() {
        mToolTipPointingUp = true;
        return this;
    }

    public ToolTip withOverlayblocked() {
        mShowToolTipOverlay = true;
        return this;
    }

    public ToolTip withLocationParameters(Context context, int anchorViewX, int anchorViewY) {
        if(LanguageUtils.isRtlLayout(context)) {
            this.anchorViewX = (-1) *anchorViewX;
        } else {
            this.anchorViewX = anchorViewX;
        }
        this.anchorViewY = anchorViewY;
        return this;
    }

    public int getAnchorViewX() {
        return anchorViewX;
    }

    public int getAnchorViewY() {
        return anchorViewY;
    }

    /**
     * Set no soft dismissal for tooltip.
     *
     * @return this ToolTip to build upon.
     */
    public ToolTip withoutSoftDismissal() {
        mShouldSoftDismiss = false;
        return this;
    }

    /**
     * @param typeface the typeface to set
     */
    public void withTypeface(final Typeface typeface) {
        mTypeface = typeface;
    }

    public CharSequence getText() {
        return mText;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public int getColor() {
        return mColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public View getContentView() {
        return mContentView;
    }

    public View getOverlayView(){
        return  mOverlayView;
    }

    public View getAnchorView() {
        return mAnchorView;
    }

    public View getParentLayout() {
        return mParentLayout;
    }

    public ToolTip.AnimationType getAnimationType() {
        return mAnimationType;
    }

    public boolean shouldShowShadow() {
        return mShouldShowShadow;
    }

    public boolean shouldShowDismissButton() {
        return mShouldShowDismissButton;
    }

    public boolean shouldShowQuickTip() {
        return mShouldShowQuickTip;
    }

    public boolean shouldShowToolTipPointerup() {
        return mToolTipPointingUp;
    }

    public boolean shouldShowgreyOverlay() {
        return mShowToolTipOverlay;
    }

    /**
     * @return the typeface
     */
    public Typeface getTypeface() {
        return mTypeface;
    }

    public Type getType() {
        return mType;
    }

    @SuppressLint("InflateParams")
    public ToolTip withContextualView(String headingText, String subHeadingText, View anchorView) {
        mType = Type.Contextual;
        mAnchorView = anchorView;
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.tooltip_contextual, null);
        setTextInsideTooltip(headingText, subHeadingText);
        return this;
    }

    @SuppressLint("InflateParams")
    public ToolTip withFloatieView(String headingText, String subHeadingText, View anchorView) {
        mType = Type.Contextual;
        mShouldShowBeak = false;
        mAnchorView = anchorView;
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.tooltip_floatie, null);
        setTextInsideTooltip(headingText, subHeadingText);
        return this;
    }

    @SuppressLint("InflateParams")
    public ToolTip withInlineView(String headingText, String subHeadingText, View anchorView, View parentLayout) {
        mType = Type.Inline;
        mAnchorView = anchorView;
        mParentLayout = parentLayout;
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.tooltip_contextual, null);
        setTextInsideTooltip(headingText, subHeadingText);
        return this;
    }

    @SuppressLint("InflateParams")
    public ToolTip withInlineViewWithIcon(String headingText, String subHeadingText, int iconResource, View anchorView, View parentLayout) {
        mType = Type.Inline;
        mAnchorView = anchorView;
        mParentLayout = parentLayout;
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.tooltip_inline_icon, null);
        setIconInsideTooltip(iconResource);
        setTextInsideTooltip(headingText, subHeadingText);
        return this;
    }

    private void setIconInsideTooltip(int iconResource) {
        ImageView icon = mContentView.findViewById(R.id.icon);
        icon.setImageResource(iconResource);
    }

    private void setTextInsideTooltip(String headingText, String subHeadingText) {
        TextView heading = mContentView.findViewById(R.id.heading);
        TextView subHeading = mContentView.findViewById(R.id.subheading);
        View quicktip = mContentView.findViewById(R.id.quicktip);
        heading.setText(headingText);
        subHeading.setText(subHeadingText);
        if (TextUtils.isEmpty(headingText)) {
            heading.setVisibility(View.GONE);
            if (mShouldShowDismissButton) {
                // set subheading padding right to avoid overlap with dismiss button
                // not do the same thing for heading text because heading text is short in most case and has low probability to overlap with dismiss button
                subHeading.setPadding(0, 0, (int) mContext.getResources().getDimension(R.dimen.dp_20), 0);
            }
        } else {
            if (mShouldShowDismissButton) {
                // set heading padding right to avoid overlap with dismiss button
                heading.setPadding(0, 0, (int) mContext.getResources().getDimension(R.dimen.dp_20), 0);
            }
        }
        if (mShouldShowQuickTip) {
            quicktip.setVisibility(View.VISIBLE);
        } else {
            quicktip.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(subHeadingText)) {
            subHeading.setVisibility(View.GONE);
        }
    }

    public ToolTip withGravity(int gravity) {
        if (mContentView != null) {
            ((LinearLayout) mContentView).setGravity(gravity);
        }
        return this;
    }


    public ToolTip withoutBeak() {
        mShouldShowBeak = false;
        return this;
    }

    public ToolTip withoutDismissButton() {
        mShouldShowDismissButton = false;

        // reset subheading padding to make subheading is center aligned
        // subheading padding right may set before to avoid overlap with dismiss button but now is not necessary
        TextView subHeading = mContentView.findViewById(R.id.subheading);
        subHeading.setPadding(0, 0, 0, 0);

        return this;
    }

    public boolean shouldShowBeak() {
        return mShouldShowBeak;
    }

    public ToolTip withDeltaShift(Context context, int dx, int dy) {
        // Android system does not mirror absolute co-ordinates in RTL layouts i.e x scale would
        // would still start 0 on the left. In RTL layout, the delta correction is mirrored so that
        // it aligns with the mirrored anchor views.
        if(LanguageUtils.isRtlLayout(context)){
            this.dx = (-1) *dx;
        } else {
            this.dx = dx;
        }
        this.dy = dy;
        return this;
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public ToolTip withFullWidth(boolean fullWidth) {
        mShowFullWidth = fullWidth;
        return this;
    }

    public boolean showFullWidth() {
        return mShowFullWidth;
    }

    public boolean shouldSetMargin() {
        return mShouldSetMargin;
    }

    public int getLeftMargin() {
        return mLeftMargin;
    }

    public int getTopMargin() {
        return mTopMargin;
    }
    public int getRightMargin() {
        return mRightMargin;
    }

    public int getBottomMargin() {
        return mBottomMargin;
    }

    public ToolTip withWidth(int width) {
        if (mContentView != null) {
            TextView heading = mContentView.findViewById(R.id.heading);
            TextView subHeading = mContentView.findViewById(R.id.subheading);
            heading.setMaxWidth(width);
            subHeading.setMaxWidth(width);
        }
        return this;
    }

    public ToolTip withMinWidth(int width){
        if (mContentView != null) {
            mContentView.setMinimumWidth(width);
        }
        return this;
    }

    public ToolTip withMargins(int left, int top, int right, int bottom) {
        mShouldSetMargin = true;
        mLeftMargin = left;
        mTopMargin = top;
        mRightMargin = right;
        mBottomMargin = bottom;
        return this;
    }

    public ToolTip withMinHeight(int height){
        if (mContentView != null) {
            mContentView.setMinimumHeight(height);
        }
        return this;
    }

    public ToolTip withFullPageTeachingUI() {
        mType = Type.FullPage;
        return this;
    }


}
