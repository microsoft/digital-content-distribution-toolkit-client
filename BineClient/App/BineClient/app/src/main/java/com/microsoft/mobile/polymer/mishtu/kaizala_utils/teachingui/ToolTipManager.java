package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.mobile.polymer.mishtu.BuildConfig;
import com.microsoft.mobile.polymer.mishtu.R;

/***
 * This class takes care of below things
 * 1. Evaluating policy for features and maintaining state
 * 2. Dismissing and disabling tooltips
 */
public class ToolTipManager {
    private static final String LOG_TAG = "ToolTipManager";

    private static volatile ToolTipManager sInstance;
    //private final LocalStorage mStore;
    private final int MAX_TOOL_TIPS_PER_SESSION = 6;
    private int mNumOfToolTipsShownInCurrentSession = 0;
    private ToolTipContainer mToolTipContainer;
    private Handler mConfigChangeHandler;
    private Handler mAutoDismissHandler;
    private Handler mClearCalloutHandler;
    private volatile boolean mEnabled = true;

    private ToolTipManager() {
        //mStore = LocalStorage.getInstance(context);
    }

    public static ToolTipManager getInstance() {
        if (sInstance == null) {
            synchronized (ToolTipManager.class) {
                if (sInstance == null) {
                    sInstance = new ToolTipManager();
                }
            }
        }
        return sInstance;
    }

    private void initializeHandlers() {
        if (mConfigChangeHandler == null || mAutoDismissHandler == null || mClearCalloutHandler == null) {
            mConfigChangeHandler = new Handler(Looper.getMainLooper());
            mAutoDismissHandler = new Handler(Looper.getMainLooper());
            mClearCalloutHandler = new Handler(Looper.getMainLooper());
        }
    }

    public void showToolTipOnConfigChange(final TeachingFragment activity) {

        initializeHandlers();
        if (mToolTipContainer != null && mToolTipContainer.getToolTip().getType() == ToolTip.Type.Contextual) {

            if (mToolTipContainer.getToolTip().getType().equals(ToolTip.Type.Inline)) {
                return;
            }
            mToolTipContainer.getToolTipView().clearAnimationAndRemove();
            mConfigChangeHandler.removeCallbacksAndMessages(null);

            mConfigChangeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mToolTipContainer != null) {
                        ToolTip toolTip = mToolTipContainer.getToolTip();
                        ToolTipView toolTipView = activity.getTeachingOverlayView().showToolTipForView(toolTip);
                        mToolTipContainer.setToolTipView(toolTipView);
                        toolTipView.setOnToolTipViewDismissedListener(createToolTipViewDismissedListener(mToolTipContainer.getPolicy()));
                    }
                }
            }, 500);


        }
    }

    private boolean isInputValid(TeachingPolicy teachingPolicy,
                                 ToolTip.Type tooltipType,
                                 View anchorView,
                                 View parentLayout,
                                 TeachingFragment activity,
                                 IToolTipViewHandler toolTipViewHandler) {
        if (teachingPolicy == null) {

            return false;
        }

        switch (tooltipType) {
            case Contextual:
                if (activity == null || activity.getTeachingOverlayView() == null || anchorView == null) {

                    return false;
                }
                break;
            case Inline:
                if (activity == null || activity.getTeachingOverlayView() == null || anchorView == null || parentLayout == null) {

                    return false;
                }
                break;
            case FullPage:
                if(toolTipViewHandler == null) {

                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * Evaluates the policy, validates input and shows the tooltip if it has to be shown
     * @param feature            Feature name
     * @param tooltipType        Type of the tooltip
     * @param anchorView         Anchor view of tooltip
     * @param parentView         Parent view for inline tooltips
     * @param toolTipProvider    Tooltip provider instance which gives tooltip when required
     * @param activity           Parent activity
     * @param toolTipViewHandler Handler object with onShow and onDismiss
     */
    public void evaluateAndShowToolTip(final TeachingUIConstants.TeachingFeature feature,
                                       final ToolTip.Type tooltipType,
                                       final View anchorView,
                                       final View parentView,
                                       final IToolTipProvider toolTipProvider,
                                       final TeachingFragment activity,
                                       final IToolTipViewHandler toolTipViewHandler) {

        final TeachingPolicy policy = TeachingPolicyFactory.getTeachingPolicyForFeature( feature);
        if (areTeachingTooltipsEnabled()
                && !isToolTipOnScreen()
                && isInputValid(policy, tooltipType, anchorView, parentView, activity, toolTipViewHandler)
                && canShowToolTipBasedOnTeachingPolicy(policy)
                && canShowToolTipBasedOnGlobalPolicy()) {

            mNumOfToolTipsShownInCurrentSession++;
            incrementDisplayedCountForTeachingPolicy(policy);
            showToolTip(feature, tooltipType, anchorView, parentView, toolTipProvider, activity, toolTipViewHandler);
        }
    }

    /**
     * Show the tooltip if it is not showing
     * @param feature               Feature name
     * @param tooltipType           Type of tooltip
     * @param anchorView            Anchor view of tooltip
     * @param parentView            Parent view for inline tooltips
     * @param toolTipProvider    Tooltip provider instance which gives tooltip when required
     * @param activity              Parent activity
     * @param toolTipViewHandler    Handler object with getTooltip, onShow and onDismiss
     */
    public void showToolTip(final TeachingUIConstants.TeachingFeature feature,
                            final ToolTip.Type tooltipType,
                            final View anchorView,
                            final View parentView,
                            final IToolTipProvider toolTipProvider,
                            final TeachingFragment activity,
                            final IToolTipViewHandler toolTipViewHandler) {

        initializeHandlers();
        final TeachingPolicy policy = TeachingPolicyFactory.getTeachingPolicyForFeature(feature);
        if (areTeachingTooltipsEnabled()
                && isInputValid(policy, tooltipType, anchorView, parentView, activity, toolTipViewHandler)
                && !isToolTipOnScreen()) {

            ToolTipRelativeLayout teachingOverlayView = activity.getTeachingOverlayView();
            final ToolTipView toolTipView;
            ToolTip toolTip = toolTipProvider.getTooltip();
            ToolTip.Type toolTipType = toolTip.getType();
            if(toolTip.shouldShowgreyOverlay()) {
                activity.getConversationHolderOverlay().setVisibility(View.VISIBLE);
                activity.getConversationHolderOverlay().DrawRectangleOverlay(toolTip);
            } else {
                activity.getConversationHolderOverlay().setVisibility(View.GONE);
            }
            switch (toolTipType) {
                case Contextual:
                    toolTipView = teachingOverlayView.showToolTipForView(toolTip);
                    break;
                case Inline:
                    toolTipView = new ToolTipView(activity.requireContext());
                    toolTipView.setToolTip(toolTip);
                    ((ViewGroup) toolTip.getParentLayout()).addView(toolTipView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    break;
                case FullPage:
                    //As FullPage tooltip is a new activity, call only onShow();
                    toolTipViewHandler.onShow(null);
                    return;
                default:

                    return;
            }

            mToolTipContainer = new ToolTipContainer(policy, toolTip, toolTipView, toolTipViewHandler);
            if (toolTipViewHandler != null) {
                toolTipViewHandler.onShow(toolTipView);
            }

            //Adding state listener on anchorView to dismiss the tooltip if anchor gets detached from screen
            toolTip.getAnchorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                    clearToolTipsOnScreen();

                }
            });

            toolTipView.setOnToolTipViewDismissedListener(createToolTipViewDismissedListener(policy));

            toolTipView.setOnToolTipViewDetachedListener(new ToolTipView.OnToolTipViewDetachedListener() {
                @Override
                public void onDetached() {
                    mToolTipContainer = null;
                }
            });

            //Handling Auto-dismissal tool tip cases
            handleAutoDismissal(policy);
        }
    }

    /**
     * Disable the tooltip policy and dismisses if being shown on the screen
     *
     * @param feature Feature enum
     */
    public void dismissAndDisableToolTip(final Context context, final TeachingUIConstants.TeachingFeature feature, boolean forceDisableNextTooltip) {
        TeachingPolicy policy = TeachingPolicyFactory.getTeachingPolicyForFeature(feature);
        if (mToolTipContainer != null
                && policy != null
                && mToolTipContainer.getPolicy().getFeatureID() == policy.getFeatureID()) {
            setUserDismissalCountForTeachingPolicy(policy);
            clearToolTipsOnScreen();
        } else if(forceDisableNextTooltip) {
            setUserDismissalCountForTeachingPolicy(policy);
        }
    }


    public void clearToolTipsAndHandlers() {
        clearToolTipsOnScreen();
        clearHandlers();
    }

    /**
     * Clears config change and auto dismissal handlers
     */
    private void clearHandlers() {
        if(mConfigChangeHandler != null)
            mConfigChangeHandler.removeCallbacksAndMessages(null);
        if(mAutoDismissHandler != null)
            mAutoDismissHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Clears if there are any tooltips on screen returns false if there are no tooltips
     */
    public boolean clearToolTipsOnScreen() {
        if (mToolTipContainer != null) {
            mClearCalloutHandler.post( new Runnable() {
                @Override
                public void run() {
                    if(mToolTipContainer != null) {
                        ToolTipView toolTipView = mToolTipContainer.getToolTipView();

                        if (toolTipView != null) {
                            /*if(mToolTipContainer.getToolTip().shouldShowgreyOverlay()) {
                                ((View)(mToolTipContainer.getToolTipView().getParent())).findViewById(R.id.rectangleOverlay).setVisibility(View.GONE);
                            }*/
                            toolTipView.remove();

                            IToolTipViewHandler toolTipViewHandler = mToolTipContainer.getToolTipViewHandler();
                            if (toolTipViewHandler != null) {
                                toolTipViewHandler.onDismiss(toolTipView);
                            }
                        }
                        mToolTipContainer = null;
                    }
                }
            });
            return true;
        }
        return false;
    }

    /**
     * GLobally enables or disables the tool tips
     * Used in case of test runs to optionally disable it.
     * @param enable
     */
    public void setEnabled(boolean enable) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            mEnabled = enable;
            if (!mEnabled) {
                clearToolTipsAndHandlers();
            }
        });
    }

    public boolean areTeachingTooltipsEnabled() {
        return mEnabled;
    }

    private boolean isToolTipOnScreen() {

        return mToolTipContainer != null;
    }

    /**
     * Returns true if the tooltip is already shown or its force dismissed.
     * This is already taken care by evaluateAndShowTooltip but this is added specifically to reduce the computation of finding the anchorview.
     * This is used only in Tooltip - CHAT_TAB_OVERFLOW_MENU
     * @param feature
     * @return
     */
    public boolean isTooltipShown(TeachingUIConstants.TeachingFeature feature) {
        /*try {
            return mStore.getDisplayedCountForToolTip(feature.name()) > 0 || mStore.getUserDismissalCountToolTip(feature.name()) == 1;
        } catch (LocalStorageException e) {
            LogFile.LogGenericDataToFile(LogLevel.ERROR, LOG_TAG, "exception on isTooltipShown "+ e);
        }*/
        return false;
    }

    public boolean isTooltipDismissedByUser(TeachingUIConstants.TeachingFeature feature) {
        /*try {
            return mStore.getUserDismissalCountToolTip(feature.name()) > 0;
        } catch (LocalStorageException e) {
            LogFile.LogGenericDataToFile(LogLevel.ERROR, LOG_TAG, "exception on isTooltipDismissedByUser "+ e);
        }*/
        return false;
    }

    public boolean canShowToolTipBasedOnTeachingPolicy(TeachingPolicy policy) {
        /*try {
            if (isTooltipDismissedByUser(policy.getFeatureID())) {

                return false;
            } else if (policy.getUserSessionCount() != 0 && incrementAndReturnSessionCountForTeachingPolicy(policy) != policy.getUserSessionCount()) {

                return false;
            } else
                return mStore.getDisplayedCountForToolTip(policy.getFeatureID().name()) < policy.getShowCount();
        } catch (LocalStorageException e) {
            e.printStackTrace();
        }*/
        return true;
    }

    private void incrementDisplayedCountForTeachingPolicy(TeachingPolicy policy) {
        /*try {
            int currentCount = mStore.getDisplayedCountForToolTip(policy.getFeatureID().name());
            mStore.setDisplayedCountForToolTip(policy.getFeatureID().name(), currentCount + 1);
        } catch (LocalStorageException e) {
            e.printStackTrace();
        }*/
    }

    /*  This method is called every time the tooltip is being evaluated for being displayed thus
      * incrementing the count of the number of times user came to this view.
      */
    private int incrementAndReturnSessionCountForTeachingPolicy(TeachingPolicy policy) {
        int currentCount = 0;//mStore.getSessionCountForToolTip(policy.getFeatureID().name());
        // Avoiding db write for when the session count is way above the max user session count
        if (currentCount <= TeachingUIConstants.MAX_TOOL_TIPS_SESSION_COUNT) {
            //mStore.setSessionCountForToolTip(policy.getFeatureID().name(), currentCount + 1);
            return currentCount + 1;
        }
        // Avoiding checking or incrementing session counts in subsequent calls.
        setUserDismissalCountForTeachingPolicy(policy);
        return currentCount;
    }

    /*  This method gives the number of times the view with the tooltip was opened.
     *  We do not store the session counts more than MAX_TOOL_TIPS_SESSION_COUNT+1 so
     *  after that those number of sessions api returns MAX_TOOL_TIPS_SESSION_COUNT+1.
     *  This will help in tracking after how many views did the user dismiss (opened the desired view)
     *  the tooltip.
      */
    public int getSessionCountForTeachingPolicy(TeachingUIConstants.TeachingFeature feature) {
        /*int currentCount = mStore.getSessionCountForToolTip(feature.name());
        return currentCount;*/
        return 0;
    }

    public void setUserDismissalCountForTeachingPolicy(TeachingPolicy policy) {
        /*try {
            //Passing integer instead of boolean to support multiple dismissals by user in future
            mStore.setUserDismissalCountToolTip(policy.getFeatureID().name(), 1);
        } catch (LocalStorageException e) {
            e.printStackTrace();
        }*/
    }

    public boolean canShowToolTipBasedOnGlobalPolicy() {
        return true; //mNumOfToolTipsShownInCurrentSession < MAX_TOOL_TIPS_PER_SESSION;
    }

    public void clearToolTipDBData() {

        //mStore.clearAllDataForTeachingUI();
        mNumOfToolTipsShownInCurrentSession = 0;
    }

    /**
     * create OnToolTipDismissedListener
     * @param policy teaching policy
     * @return a new OnToolTipDismissedListener
     */
    private ToolTipView.OnToolTipDismissedListener createToolTipViewDismissedListener(final TeachingPolicy policy) {
        return new ToolTipView.OnToolTipDismissedListener() {
            @Override
            public void onToolTipViewDismissed(ToolTipView toolTipView) {

                setUserDismissalCountForTeachingPolicy(policy);
                clearToolTipsOnScreen();

            }
        };
    }

    /**
     * handle auto dismissal based on the policy
     * @param policy teaching policy
     */
    private void handleAutoDismissal(final TeachingPolicy policy) {
        if (policy.getAutoDismissalTimeInSec() > 0) {
            mAutoDismissHandler.removeCallbacksAndMessages(null);
            mAutoDismissHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mToolTipContainer == null)
                        return;

                    //If orientation is changed when auto-dismiss is being done, then showToolTipOnConfigChange() may show the tooltip again
                    mConfigChangeHandler.removeCallbacksAndMessages(null);

                    if (policy.getFeatureID().equals(mToolTipContainer.getPolicy().getFeatureID())) {
                        clearToolTipsOnScreen();
                    } else {

                    }
                }
            }, policy.getAutoDismissalTimeInSec() * 1000);
        }
    }

    private class ToolTipContainer {
        TeachingPolicy mPolicy;
        ToolTip mToolTip;
        ToolTipView mToolTipView;
        IToolTipViewHandler mToolTipViewHandler;

        public ToolTipContainer(TeachingPolicy policy, ToolTip toolTip, ToolTipView toolTipView, IToolTipViewHandler iToolTipViewHandler) {
            this.mPolicy = policy;
            this.mToolTip = toolTip;
            this.mToolTipView = toolTipView;
            this.mToolTipViewHandler = iToolTipViewHandler;
        }

        public TeachingPolicy getPolicy() {
            return mPolicy;
        }

        public ToolTip getToolTip() {
            return mToolTip;
        }

        public ToolTipView getToolTipView() {
            return mToolTipView;
        }

        public IToolTipViewHandler getToolTipViewHandler() {
            return mToolTipViewHandler;
        }

        public void setToolTipView(ToolTipView toolTipView) {
            mToolTipView = toolTipView;
        }
    }
}
