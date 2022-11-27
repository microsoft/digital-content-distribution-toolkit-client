package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

/**
 * This interface is used to return the required tooltip to the caller when tooltip is ready to be shown to user.
 */

public interface IToolTipProvider {
    /**
     * Tooltip must be constructed and returned in this method.
     * @return Tooltip that needs to be shown
     */
    ToolTip getTooltip();
}
