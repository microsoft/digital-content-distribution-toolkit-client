package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

/**
 * Interface for callback to clients during events of showing and dismissing tooltips
 * Client can customize experience by modifying any related layouts in onShow() and onDismiss()
 */
public interface IToolTipViewHandler {

    /**
     * For inline tooltips, client has to attach ToolTipView to a relative layout in onShow
     *
     * @param view View object of tooltip
     */
    void onShow(ToolTipView view);

    /**
     * For inline tooltips, client may want to hide the relative layout containing the tooltip in onDismiss
     *
     * @param view
     */
    void onDismiss(ToolTipView view);
}
