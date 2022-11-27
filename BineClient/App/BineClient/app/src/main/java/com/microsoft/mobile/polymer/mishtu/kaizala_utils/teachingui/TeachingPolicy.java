package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

import androidx.annotation.NonNull;

public class TeachingPolicy {
    private TeachingUIConstants.TeachingFeature mFeatureID;
    private Integer mShowCount;
    private TeachingUIConstants.ToolTipDismissalType mDismissalType;
    private Integer mAutoDismissalTimeInSec;
    private Integer mPriority; // 0 will have higher priority than 1
    private int mUserSessionCount; // shown when the user reaches the screen nth time. 0 value means show everytime.

    /**
     *
     * @param featureID TeachingFeature Enum
     * @param showCount Number of times to show tooltip to the user
     * @param dismissalType Dismissal type for tool tip
     * @param autoDismissalTimeInSec Valid only if dismissal type is TimerBased
     * @param priority 0 will have higher priority than 1
     */
    public TeachingPolicy(TeachingUIConstants.TeachingFeature featureID,
                          int showCount,
                          TeachingUIConstants.ToolTipDismissalType dismissalType,
                          int autoDismissalTimeInSec,
                          int priority) {
        mFeatureID = featureID;
        mShowCount = showCount;
        mDismissalType = dismissalType;
        if( dismissalType.equals(TeachingUIConstants.ToolTipDismissalType.TimerBased)) {
            mAutoDismissalTimeInSec = autoDismissalTimeInSec;
        } else {
            mAutoDismissalTimeInSec = 0;
        }
        mPriority = priority;
        mUserSessionCount = 0;
    }

    /**
     *
     * @param featureID TeachingFeature Enum
     * @param showCount Number of times to show tooltip to the user
     * @param dismissalType Dismissal type for tool tip
     * @param autoDismissalTimeInSec Valid only if dismissal type is TimerBased
     * @param priority 0 will have higher priority than 1
     */
    public TeachingPolicy(TeachingUIConstants.TeachingFeature featureID,
                   int showCount,
                   TeachingUIConstants.ToolTipDismissalType dismissalType,
                   int autoDismissalTimeInSec,
                   int priority,
                   int sessionCount) {
        mFeatureID = featureID;
        mShowCount = showCount;
        mDismissalType = dismissalType;
        if( dismissalType.equals(TeachingUIConstants.ToolTipDismissalType.TimerBased)) {
            mAutoDismissalTimeInSec = autoDismissalTimeInSec;
        } else {
            mAutoDismissalTimeInSec = 0;
        }
        mPriority = priority;
        mUserSessionCount = sessionCount;
    }

    @NonNull
    public TeachingUIConstants.TeachingFeature getFeatureID() {
        return mFeatureID;
    }

    public Integer getShowCount() {
        return mShowCount;
    }

    public TeachingUIConstants.ToolTipDismissalType getDismissalType() {
        return mDismissalType;
    }

    @NonNull
    public Integer getAutoDismissalTimeInSec() {
        return mAutoDismissalTimeInSec;
    }

    public Integer getPriority() {
        return mPriority;
    }

    public Integer getUserSessionCount() {
        return mUserSessionCount;
    }

}
