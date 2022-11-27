package com.microsoft.mobile.polymer.mishtu.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui.*
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.ui.activity.*

class TooltipDisplayHelper(
    private val activity: BaseActivity,
    private val containerId: Int,
    private var tooltipDisplayCompletion: TooltipDisplayCompletion?) {

    private lateinit var screen: FTUScreen
    private lateinit var teachingFragment: TeachingFragment
    private var sharedPreferenceStore = SharedPreferenceStore.getInstance()

    enum class FTUScreen {
        Main,
        Rewards,
        Search,
        BuyPack,
        ActiveOrder,
        Downloads,
        None
    }

    fun start(screen: FTUScreen) {
        this.screen = screen

        /*sharedPreferenceStore.save(screen.name, "")
        sharedPreferenceStore.save(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_HOME.name, "")
        sharedPreferenceStore.save(TeachingUIConstants.TeachingFeature.TOP_NAV.name, "")
        sharedPreferenceStore.save(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_REWARDS.name, "")
        sharedPreferenceStore.save(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_PROFILE.name, "")*/

        if (sharedPreferenceStore.get(screen.name).isNullOrEmpty()) {
            teachingFragment = TeachingFragment()
            (activity.findViewById(containerId) as FrameLayout).visibility = View.VISIBLE
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.add(containerId, teachingFragment)
            transaction.commit()
            showNext()
        }
        else tooltipDisplayCompletion?.onComplete()
    }

    private fun showNext() {
        Handler(Looper.getMainLooper()).postDelayed({
            when(screen) {
                FTUScreen.Main -> startMainActivityTips()
                FTUScreen.Search -> startSearchActivityTips()
                FTUScreen.Rewards -> startRewardsActivityTips()
               // FTUScreen.BuyPack -> startBuyPackActivityTips()
                FTUScreen.ActiveOrder -> startActiveOrderTips()
                else -> {}
            }
        }, 500)
    }

    private fun startMainActivityTips() {
        if(!shouldShowTooltip()) {
            return
        }
        (activity as MainActivity).let {
            var toolTipTitle = ""
            var toolTipDescription = ""
            var anchorView: View? = null
            var storeKey = ""

            if (sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.TOP_NAV.name).isNullOrEmpty()) {
                anchorView = it.getTopNavigationTabLayout().getTabAt(0)?.view
                toolTipTitle = activity.getString(R.string.tooltip_top_nav_title)
                toolTipDescription = activity.getString(R.string.tooltip_top_nav_description)
                storeKey = TeachingUIConstants.TeachingFeature.TOP_NAV.name
            }
            else{
                if(sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.TOP_NAV_FILMS.name).isNullOrEmpty()){
                    anchorView = it.getTopNavigationTabLayout().getTabAt(1)?.view
                    toolTipTitle = activity.getString(R.string.tooltip_top_nav_title)
                    toolTipDescription = activity.getString(R.string.tooltip_top_nav_films_description)
                    storeKey = TeachingUIConstants.TeachingFeature.TOP_NAV_FILMS.name
                }
                else {
                    if(sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.TOP_NAV_SERIES.name).isNullOrEmpty()){
                        anchorView = it.getTopNavigationTabLayout().getTabAt(2)?.view
                        toolTipTitle = activity.getString(R.string.tooltip_top_nav_title)
                        toolTipDescription = activity.getString(R.string.tooltip_top_nav_series_description)
                        storeKey = TeachingUIConstants.TeachingFeature.TOP_NAV_SERIES.name
                    }
                    else {
                        if(sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_HOME.name)
                                .isNullOrEmpty()) {
                            toolTipTitle = activity.getString(R.string.bn_home_title)
                            toolTipDescription = activity.getString(R.string.tooltip_home_description)
                            anchorView = it.getTabLayout().getTabAt(0)?.view
                            storeKey = TeachingUIConstants.TeachingFeature.BOTTOM_NAV_HOME.name
                        }
                        else {
                            if(sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_REWARDS.name).isNullOrEmpty()) {
                                toolTipTitle = activity.getString(R.string.bn_rewards)
                                toolTipDescription = activity.getString(R.string.tooltip_rewards_description)
                                anchorView = it.getTabLayout().getTabAt(1)?.view
                                storeKey = TeachingUIConstants.TeachingFeature.BOTTOM_NAV_REWARDS.name
                            }
                            else {
                                if (sharedPreferenceStore.get(TeachingUIConstants.TeachingFeature.BOTTOM_NAV_PROFILE.name).isNullOrEmpty()) {
                                    toolTipTitle = activity.getString(R.string.bn_profile_title)
                                    toolTipDescription = activity.getString(R.string.tooltip_profile_description)
                                    anchorView = it.getProfileTab()?.view
                                    storeKey = screen.name
                                }
                            }
                        }
                    }

                }
            }








            showToolTip(it, anchorView,
                toolTipTitle,
                toolTipDescription,
                storeKey)
        }
        return
    }

    private fun startBuyPackActivityTips() {
        if(!shouldShowTooltip()) {
            return
        }
        (activity as MainActivity).let {
            showToolTip(it, it.buyPackButton(),
                activity.getString(R.string.buy_pack),
                activity.getString(R.string.tooltip_buy_pack_description),
                screen.name)
        }
    }

    private fun startActiveOrderTips() {
        if(!shouldShowTooltip()) {
            return
        }
        (activity as MainActivity).let {
            showToolTip(it, it.getActiveOrderLayout(),
                activity.getString(R.string.tooltip_order_pending_title),
                activity.getString(R.string.tooltip_order_pending_description),
                screen.name)
        }
    }

    private fun startSearchActivityTips() {
        if(!shouldShowTooltip()) {
            return
        }
        (activity as SearchActivity).let {
            showToolTip(it, it.getSearchMicView(),
                activity.getString(R.string.tooltip_search_mic_title),
                activity.getString(R.string.tooltip_search_mic_description),
                screen.name)
        }
    }

    private fun startRewardsActivityTips() {
        if(!shouldShowTooltip()) {
            return
        }
        (activity as MainActivity).let {
            showToolTip(it, it.getAvailableCoinLayout(),
                activity.getString(R.string.tooltip_available_coins_title),
                activity.getString(R.string.tooltip_available_coins_description),
                screen.name)
        }
    }

    private fun shouldShowTooltip(): Boolean {
        if(!sharedPreferenceStore.get(screen.name).isNullOrEmpty()) {
            close()
            tooltipDisplayCompletion?.onComplete()
            return false
        }
        return true
    }

    private fun showToolTip(activity: TeachingBaseActivity,
                            anchorView: View?,
                            toolTipTitle: String,
                            toolTipDescription: String,
                            storeKey: String) {
        anchorView?.let { view->
            if (activity.isActivityVisible()) {
                showToolTip(view, toolTipTitle, toolTipDescription)
                sharedPreferenceStore.save(storeKey, "true")
            }
        }
    }

    private fun showToolTip(anchorView: View, title: String, description: String) {
        ToolTipManager.getInstance()
            .evaluateAndShowToolTip(TeachingUIConstants.TeachingFeature.CHAT_HISTORY,
                ToolTip.Type.Contextual,
                anchorView,
                null /*parentView*/,
                { getTabToolTip(activity, anchorView, title, description) },
                teachingFragment,
                object : IToolTipViewHandler {
                    override fun onShow(view: ToolTipView) {
                    }

                    override fun onDismiss(view: ToolTipView) {
                        showNext()
                    }
                })
    }

    /**
     * Returns the tool tip to show over the tab
     * @param context Context in which this tool tip is to be shown
     * @param anchorView - Anchor view for the tooltip
     * @param title - title for the tool tip
     * @param desc - desc for the tool tip
     * @return required tooltip object
     */
    private fun getTabToolTip(
        context: Context,
        anchorView: View,
        title: String,
        desc: String
    ): ToolTip? {
        val ymargin = context.resources.getDimension(R.dimen.conversation_tooltip_ymargin).toInt()
        return ToolTip(context)
            .withOverlayblocked()
            .withContextualView(title, desc, anchorView)
            .withDeltaShift(context, 0, ymargin)
            .withAnimationType(ToolTip.AnimationType.FADE)
    }

    private fun close() {
        activity.supportFragmentManager.findFragmentById(containerId)?.let { fragment ->
            activity.supportFragmentManager.beginTransaction()
                .remove(fragment).commit()
            (activity.findViewById(containerId) as FrameLayout).visibility = View.GONE
        }
    }

    interface TooltipDisplayCompletion {
        fun onComplete()
    }
}