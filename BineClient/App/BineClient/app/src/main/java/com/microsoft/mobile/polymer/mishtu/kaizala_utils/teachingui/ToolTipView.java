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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.ViewUtilities;


/**
 * A ViewGroup to visualize ToolTips. Use
 * ToolTipRelativeLayout.showToolTipForView() to show ToolTips.
 */
public class ToolTipView extends LinearLayout implements ViewTreeObserver.OnPreDrawListener {

    private ImageView mTopPointerView;
    private View mTopFrame;
    private ViewGroup mContentHolder;
    private TextView mToolTipTV;
    private View mBottomFrame;
    private ImageView mBottomRightPointerView;
    private ImageView mBottomLeftPointerView;
    private View mShadowView;

    private ToolTip mToolTip;
    private View mView;

    private boolean mDimensionsKnown;
    private int mRelativeMasterViewY;

    private int mRelativeMasterViewX;
    private int mWidth;
    private View mDismissIcon;

    private boolean mShouldShowBeak;

    private int dx;
    private int dy;

    private boolean mShowFullWidth;
    private ToolTip.AnimationType mAnimationType;


    private OnToolTipDismissedListener mToolTipDismissedListener;
    private OnToolTipViewDetachedListener mToolTipViewDetachedListener;

    private int anchorViewX;
    private int anchorViewY;

    public ToolTipView(final Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.tooltip, this, true);

        mShouldShowBeak = true;
        mTopPointerView = findViewById(R.id.tooltip_pointer_up);
        mTopFrame = findViewById(R.id.tooltip_topframe);
        mContentHolder = findViewById(R.id.tooltip_contentholder);
        mToolTipTV = findViewById(R.id.tooltip_contenttv);
        mBottomFrame = findViewById(R.id.tooltip_bottomframe);
        mBottomRightPointerView = findViewById(R.id.tooltip_pointer_right_down);
        mBottomLeftPointerView = findViewById(R.id.tooltip_pointer_left_down);
        mShadowView = findViewById(R.id.tooltip_shadow);
        mDismissIcon = findViewById(R.id.fre_dismiss);
        mDismissIcon.setOnClickListener(v -> onDismiss());
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        mDimensionsKnown = true;

        mWidth = mContentHolder.getWidth();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.width = mWidth;
        setLayoutParams(layoutParams);

        if (mToolTip != null) {
            applyToolTipPosition();
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mToolTipViewDetachedListener != null) {
            mToolTipViewDetachedListener.onDetached();
        }
    }

    public void setToolTip(final ToolTip toolTip) {
        mToolTip = toolTip;
        mView = toolTip.getAnchorView();

        if (mToolTip.getText() != null) {
            mToolTipTV.setText(mToolTip.getText());
        } else if (mToolTip.getTextResId() != 0) {
            mToolTipTV.setText(mToolTip.getTextResId());
        }

        if (mToolTip.getTypeface() != null) {
            mToolTipTV.setTypeface(mToolTip.getTypeface());
        }

        if (mToolTip.getTextColor() != 0) {
            mToolTipTV.setTextColor(mToolTip.getTextColor());
        }

        if (mToolTip.getColor() != 0) {
            setColor(mToolTip.getColor());
        }

        if (mToolTip.getContentView() != null) {
            setContentView(mToolTip.getContentView());
        }

        mDismissIcon.setVisibility(mToolTip.shouldShowDismissButton() ? VISIBLE : GONE);

        if (mToolTip.shouldShowQuickTip()) {
            mContentHolder.setPadding((int)getResources().getDimension(R.dimen.teachingui_bottom_margin) , (int)getResources().getDimension(R.dimen.teachingui_top_margin), (int)getResources().getDimension(R.dimen.teachingui_bottom_margin), (int)getResources().getDimension(R.dimen.teachingui_bottom_margin));
        }

        if (!mToolTip.shouldShowShadow()) {
            mShadowView.setVisibility(View.GONE);
        }

        mShouldShowBeak = mToolTip.shouldShowBeak();

        dx = mToolTip.getDeltaX();
        dy = mToolTip.getDeltaY();
        mShowFullWidth = mToolTip.showFullWidth();

        mAnimationType = mToolTip.getAnimationType();

        if (mDimensionsKnown) {
            applyToolTipPosition();
        }

        anchorViewX = mToolTip.getAnchorViewX();
        anchorViewY = mToolTip.getAnchorViewY();
    }

    private void applyToolTipPosition() {
        final int[] masterViewScreenPosition = new int[2];
        mView.getLocationOnScreen(masterViewScreenPosition);

        //This is for recyclerview. For the first time we are getting values as 0,0. So passing these values in withLocationParameters
        if(masterViewScreenPosition[0] == 0 && masterViewScreenPosition[1] == 0) {
            masterViewScreenPosition[0]= anchorViewX;
            masterViewScreenPosition[1] = anchorViewY;
        }

        final Rect viewDisplayFrame = new Rect();
        mView.getWindowVisibleDisplayFrame(viewDisplayFrame);

        final int[] parentViewScreenPosition = new int[2];
        ((View) getParent()).getLocationOnScreen(parentViewScreenPosition);

        final int masterViewWidth = mView.getWidth();
        final int parentWidth = ((View) getParent()).getWidth();
        final int masterViewHeight = mView.getHeight();

        mRelativeMasterViewX = masterViewScreenPosition[0] - parentViewScreenPosition[0];
        mRelativeMasterViewY = masterViewScreenPosition[1] - parentViewScreenPosition[1];
        final int relativeMasterViewCenterX = mRelativeMasterViewX + masterViewWidth / 2;

        int toolTipViewAboveY = mRelativeMasterViewY - getHeight();
        int toolTipViewBelowY = Math.max(0, mRelativeMasterViewY + masterViewHeight);

        int toolTipViewX = Math.max(0, relativeMasterViewCenterX - mWidth / 2);
        if (toolTipViewX + mWidth > viewDisplayFrame.right) {
            toolTipViewX = viewDisplayFrame.right - mWidth;
        }

        setX(toolTipViewX + dx);

        boolean showBelow = toolTipViewAboveY < 0;
        setPointerCenterX(relativeMasterViewCenterX);

        if(mToolTip.shouldShowToolTipPointerup()) {
            showBelow = true;
        }

        mTopPointerView.setVisibility(showBelow ? VISIBLE : GONE);
        mBottomRightPointerView.setVisibility(!showBelow && (mRelativeMasterViewX - parentWidth/2 > 0) ? VISIBLE : GONE);
        mBottomLeftPointerView.setVisibility(!showBelow && (mRelativeMasterViewX - parentWidth/2 < 0) ? VISIBLE : GONE);

        if (!mShouldShowBeak) {
            mTopPointerView.setVisibility(GONE);
            mBottomRightPointerView.setVisibility(GONE);
            mBottomLeftPointerView.setVisibility(GONE);
        }

        if (mShowFullWidth) {
            ViewGroup.LayoutParams params = mContentHolder.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mContentHolder.setLayoutParams(params);

            mTopFrame.setVisibility(GONE);
            mBottomFrame.setVisibility(GONE);

            params = getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            setLayoutParams(params);

        }

        if (mToolTip.shouldSetMargin()) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
            marginLayoutParams.leftMargin = mToolTip.getLeftMargin();
            marginLayoutParams.rightMargin = mToolTip.getRightMargin();
            marginLayoutParams.bottomMargin = mToolTip.getBottomMargin();
            marginLayoutParams.topMargin = mToolTip.getTopMargin();
            setLayoutParams(marginLayoutParams);
        }

        int toolTipViewY;
        if (showBelow) {
            toolTipViewY = toolTipViewBelowY;
        } else {
            toolTipViewY = toolTipViewAboveY;
        }

        this.setY(toolTipViewY + dy);
        this.setX(toolTipViewX + dx);

        if (mAnimationType == ToolTip.AnimationType.FADE) {
            Animation fadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            startAnimation(fadein);
        } else if (mAnimationType == ToolTip.AnimationType.SLIDE_HORIZONTAL) {
            Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.custom_slide_in_left);
            startAnimation(slideDown);
        } else if (mAnimationType == ToolTip.AnimationType.SLIDE_VERTICAL) {
            Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.custom_slide_down);
            startAnimation(slideDown);
        }
    }

    public void setPointerCenterX(final int pointerCenterX) {
        int pointerWidth = mTopPointerView.getMeasuredWidth();
        mTopPointerView.setX(pointerCenterX - pointerWidth / 2 - getX());
        pointerWidth = mBottomRightPointerView.getMeasuredWidth();
        mBottomRightPointerView.setX((int)(pointerCenterX - pointerWidth - getX()));
        mBottomLeftPointerView.setX((int)(pointerCenterX - getX()));
    }

    public void setOnToolTipViewDismissedListener(final OnToolTipDismissedListener listener) {
        mToolTipDismissedListener = listener;
    }

    public void setOnToolTipViewDetachedListener(final OnToolTipViewDetachedListener listener) {
        mToolTipViewDetachedListener = listener;
    }

    public void setColor(final int color) {
        mTopPointerView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        if (mTopFrame.getBackground() != null) {
            mTopFrame.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        if (mBottomFrame.getBackground() != null) {
            mBottomFrame.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        mContentHolder.setBackgroundColor(color);
    }

    private void setContentView(final View view) {
        mContentHolder.removeAllViews();
        mContentHolder.addView(view);
    }

    public void remove() {
        if (mToolTip.getAnimationType() == ToolTip.AnimationType.FADE) {
            Animation fadeOutAnimator = AnimationUtils.loadAnimation(getContext(),
                    R.anim.fade_out);

            fadeOutAnimator.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeViewFromParent();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            startAnimation(fadeOutAnimator);

        } else if (mToolTip.getAnimationType() == ToolTip.AnimationType.SLIDE_HORIZONTAL) {
            Animation slideOutRight = AnimationUtils.loadAnimation(getContext(),
                    R.anim.custom_slide_out_right);

            slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeViewFromParent();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            startAnimation(slideOutRight);

        } else if (mToolTip.getAnimationType() == ToolTip.AnimationType.SLIDE_VERTICAL) {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(),
                    R.anim.custom_slide_up);

            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeViewFromParent();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            startAnimation(slideUp);

        } else {
            removeViewFromParent();
        }
    }

    public void clearAnimationAndRemove() {
        if (mToolTip.getAnimationType() != ToolTip.AnimationType.NONE) {
            clearAnimation();
        }
        removeViewFromParent();
    }


    private void removeViewFromParent() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (ViewUtilities.isActivityAlive(ViewUtilities.getActivity(this))) {
                ViewParent parent = getParent();
                if (parent != null) {
                    ((ViewManager) parent).removeView(this);
                }
            }
        });
    }

    private void onDismiss() {
        remove();

        if (mToolTipDismissedListener != null) {
            mToolTipDismissedListener.onToolTipViewDismissed(this);
        }
    }

    /**
     * Convenience method for getting X.
     */
    @SuppressLint("NewApi")
    @Override
    public float getX() {
        float result;
        result = super.getX();

        return result;
    }

    /**
     * Convenience method for setting X.
     */
    @SuppressLint("NewApi")
    @Override
    public void setX(final float x) {
        super.setX(x);

    }

    /**
     * Convenience method for getting Y.
     */
    @SuppressLint("NewApi")
    @Override
    public float getY() {
        float result;
        result = super.getY();
        return result;
    }

    /**
     * Convenience method for setting Y.
     */
    @SuppressLint("NewApi")
    @Override
    public void setY(final float y) {

        super.setY(y);

    }

    public interface OnToolTipDismissedListener {
        void onToolTipViewDismissed(ToolTipView toolTipView);
    }

    //This listener is used when
    public interface OnToolTipViewDetachedListener {
        void onDetached();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }
}
