package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;


public class TeachingPolicyFactory {

    public static TeachingPolicy getTeachingPolicyForFeature(TeachingUIConstants.TeachingFeature feature) {
        switch (feature) {
            //Main Activity policies
            case TOP_NAV:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0);
            case BOTTOM_NAV_HOME:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0);
            case BOTTOM_NAV_REWARDS:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0);
            case BOTTOM_NAV_PROFILE:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0);

            //Chat Activity policies
            case PUSH_TO_TALK:
                return new TeachingPolicy(feature, Integer.MAX_VALUE, TeachingUIConstants.ToolTipDismissalType.TimerBased, 2, 0, 0);
            case ATTACHMENT:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 1);

            //O365 policies
            case LINKED_ACCOUNTS:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 2);

            //Skype Sign in
            case ADD_SKYPE_ACCOUNT:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 1);

            //ServiceBasedActivity policies
            case NO_NETWORK:
                return new TeachingPolicy(feature, Integer.MAX_VALUE, TeachingUIConstants.ToolTipDismissalType.TimerBased, 4, 0, 0);
            //Chat history feature availability
            case CHAT_HISTORY:
                int sessionCount = 3;
                /**
                 * This is a work around done for showing Chat history feature tooltip on the first session after app upgrade.
                 * In case of normal / fresh install, we want the Chat history tooltip to be shown on the 3 instance, but in case
                 * its an app upgrade scenario, we want to show Chat history as first tool tip. As we know that DISCOVER_TAB FRE is
                 * shown in the same activity, and the feature has been present for long, so in case of upgrade the session count
                 * should be more that Chat_history. After first run the session counts for both policies should be equal, if not,
                 * that indicates its an upgrade scenario.
                 *
                 * THIS IS A HACK AND THERE IS A RELEVANT PBI CREATED TO DEFINE AN INFRA AND NOTIFY USER OF ''WHATS NEW'' FEATURE AVAILABLE IN THE APP.
                 */
                int chatHistoryTooltipSessionCount = ToolTipManager.getInstance().getSessionCountForTeachingPolicy(TeachingUIConstants.TeachingFeature.CHAT_HISTORY);
                if (chatHistoryTooltipSessionCount != ToolTipManager.getInstance().getSessionCountForTeachingPolicy(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_HOME) &&
                        chatHistoryTooltipSessionCount == 0) {
                    sessionCount = 1;
                }
                return new TeachingPolicy(feature, 1/* showCount */, TeachingUIConstants.ToolTipDismissalType.Manual,
                        0/*autoDismissalTimeInSec*/, 0/*priority*/, sessionCount/*sessionCount*/);
            case TEXT_IN_PUBLIC_GROUP:
                return new TeachingPolicy(feature, 1/* showCount */, TeachingUIConstants.ToolTipDismissalType.Manual,
                        0/*autoDismissalTimeInSec*/, 0/*priority*/, 1/*sessionCount*/);
            case CHAT_TAB_OVERFLOW_MENU:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 10); //As session count is not working as expected in the current code, and we are getting issue in internal flavour apk when we show tooltips in same session,Marking it as 10  ( will revert this change post validation in internal build)
            case ME_TAB_OPTIONS_MOVED:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 1);
            case INVITE_TO_GROUP:
                return new TeachingPolicy(feature, Integer.MAX_VALUE, TeachingUIConstants.ToolTipDismissalType.TimerBased, 5, 0, 0);
            case CHAT_CANVAS_OVERFLOW_MENU:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 1);
            case ADD_HASHTAG:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 0);
            case ADD_PUBLIC_LINK:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 0);
            case PEOPLE_TAB_OD_SEARCH:
                // Todo: Set count to 1 before check-in
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 0);
            case STORAGE_MANAGER_BEHAVIOUR:
                return  new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 0);
            case TRANSLATION:
                return new TeachingPolicy(feature, 1/* showCount */, TeachingUIConstants.ToolTipDismissalType.Manual,
                        0/*autoDismissalTimeInSec*/, 0/*priority*/, 0/*sessionCount*/);
            case CHAT_COACH:
                return new TeachingPolicy(feature, 1, TeachingUIConstants.ToolTipDismissalType.Manual, 0, 0, 0);
        }
        return null;
    }
}
