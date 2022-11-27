package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;


public class TeachingUIConstants {

    public enum TeachingFeature {
        TOP_NAV,
        TOP_NAV_FILMS,
        TOP_NAV_SERIES,
        BOTTOM_NAV_HOME,
        BOTTOM_NAV_REWARDS,
        BOTTOM_NAV_PROFILE,
        SEARCH_MIC,
        LINKED_ACCOUNTS,
        NO_NETWORK,
        PUSH_TO_TALK,
        ATTACHMENT,
        ADD_SKYPE_ACCOUNT,
        CHAT_HISTORY,
        TEXT_IN_PUBLIC_GROUP,
        CHAT_TAB_OVERFLOW_MENU,
        ME_TAB_OPTIONS_MOVED,
        INVITE_TO_GROUP,
        CHAT_CANVAS_OVERFLOW_MENU,
        ADD_HASHTAG,
        ADD_PUBLIC_LINK,
        PEOPLE_TAB_OD_SEARCH,
        STORAGE_MANAGER_BEHAVIOUR,
        TRANSLATION,
        CHAT_COACH
    }


    public enum AlignmentMode {
        Left,
        Right,
        Top,
        Bottom
    }

    public enum ToolTipDismissalType {
        Manual,
        TimerBased,
        LightDismiss
    }

    public static final String FRE_LOG_TAG = "FEATURE_FRE";
    public static final int MAX_TOOL_TIPS_SESSION_COUNT = 30; // We track the session counts till this number for telemetry

}
