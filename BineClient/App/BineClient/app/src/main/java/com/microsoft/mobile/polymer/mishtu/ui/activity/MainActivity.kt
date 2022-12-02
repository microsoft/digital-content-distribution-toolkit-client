// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails

import com.google.android.material.tabs.TabLayout
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityMainBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore.Companion.KEY_REFERRAL_DIALOG_SHOWN
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetReferralCodeHome
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.IncentiveViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.MainModule
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Typeface
import android.view.*

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat

import com.loopnow.fireworklibrary.FwSDK
import com.loopnow.fireworklibrary.SdkStatus
import com.microsoft.mobile.polymer.mishtu.BuildConfig

import com.microsoft.mobile.polymer.mishtu.ui.fragment.*
import com.microsoft.mobile.polymer.mishtu.utils.*
import android.view.animation.Animation.AnimationListener
import android.widget.Button
import android.widget.LinearLayout
import com.loopnow.fireworklibrary.VideoPlayerProperties
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.DeviceUtil
import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.NotificationViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@DelicateCoroutinesApi
@AndroidEntryPoint
class MainActivity : BinePlayerListenerActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firstFragment: MediaLandingFragment
    private lateinit var rewardsFragment: RewardsFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var downloadFragment: DownloadFragment
    private lateinit var adapter: ViewPagerAdapter

    lateinit var mReferrerClient: InstallReferrerClient

    private val viewModel by viewModels<MainModule>()
    private val incentiveViewModel by viewModels<IncentiveViewModel>()
    private var progressShown = false
    private var referralRequestInProgress = false

    private var switchToTabOnLoad = -1
    private var showHubsNearby = false
    private var orderCompleteEventAmount = -1
    private var orderCompleteEventContentProviderId: String? = null
    private val notificationViewModel by viewModels<NotificationViewModel>()

    private var tabCount = 3
    private var homeTabIndex = 0
    private var bonusTabIndex = 1
    var downloadTabIndex = 2
    var profileTabIndex = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        startHubScanIfTime()
        initializeFireworkSDK()
        setupViewPager()
        setupTabLayout()

        if (intent != null && intent.action != null && intent.action.equals(
                NOTIFICATION_TAPPED_BY_USER)
        ) {
            intent.getStringExtra(NOTIFICATION_TYPE)?.let {
                NotificationActionHandler(this, supportFragmentManager).handleNotification(
                    it,
                    intent.getIntExtra(NotificationHandler.NOTIFICATION_ID, -1),
                    intent.getStringExtra(NOTIFICATION_DATA_ID)
                )
            }
        }
        showHubsNearby = intent?.getBooleanExtra(extraShowHubsNearby, false) ?: false

        observeData()
        viewModel.refreshToken()

        notificationViewModel. getUnreadNotifications(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        switchToTabOnLoad = intent?.getIntExtra(extraShiftTab, -1) ?: -1
        orderCompleteEventAmount = intent?.getIntExtra(extraOrderCompleteEventAmount, -1) ?: -1
        orderCompleteEventContentProviderId = intent?.getStringExtra(extraOrderCompleteEventContentProvider)
    }

    override fun teachingScreenName(): TooltipDisplayHelper.FTUScreen {
        return TooltipDisplayHelper.FTUScreen.Main
    }

    override fun teachingContainer(): Int {
        return R.id.teachingContainer
    }

    override fun completionCallback(): TooltipDisplayHelper.TooltipDisplayCompletion {
        return object : TooltipDisplayHelper.TooltipDisplayCompletion {
            override fun onComplete() {
            }
        }
    }

    override fun shouldShowOnResume(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!progressShown) incentiveViewModel.triggerIncentiveEvents()
        }, 500)

        if (switchToTabOnLoad != -1) switchTab()

        if (orderCompleteEventAmount != -1) {
            showOrderCompleteRewards()
        }

        if (showHubsNearby) {
            showHubsNearby = false
            AppUtils.showNearbyStore(this, null, true, null, null)
        }
    }

    private fun switchTab() = when (switchToTabOnLoad) {
        1 -> {
            if (binding.tabs.selectedTabPosition != 0) binding.tabs.selectTab(binding.tabs.getTabAt(
                0))
            firstFragment.getTabLayout().selectTab(firstFragment.getTabLayout().getTabAt(1))
            switchToTabOnLoad = -1
        }
        downloadTabIndex -> {
            switchToTabOnLoad = -1
            binding.tabs.selectTab(binding.tabs.getTabAt(
                downloadTabIndex))
        }
        else -> {}
    }

    private fun setupTabLayout() {
        setTabCount()

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    binding.viewPager.currentItem = it
                    var badgeType: NotificationBadgeHelper.BadgeType? = null
                    when (it) {
                        homeTabIndex -> {
                            AnalyticsLogger.getInstance().logScreenView(Screen.HOME)
                            tab.removeBadge()
                        }
                        bonusTabIndex -> {
                            AnalyticsLogger.getInstance().logScreenView(Screen.REWARDS)
                            start(TooltipDisplayHelper.FTUScreen.Rewards)
                            badgeType = NotificationBadgeHelper.BadgeType.NEW_OFFER
                        }
                        profileTabIndex -> {
                            AnalyticsLogger.getInstance().logScreenView(Screen.PROFILE)
                            badgeType = NotificationBadgeHelper.BadgeType.PROFILE
                        }
                        downloadTabIndex -> {
                            AnalyticsLogger.getInstance().logScreenView(Screen.DOWNLOAD)
                            badgeType = NotificationBadgeHelper.BadgeType.NEW_DOWNLOADS
                        }
                    }
                    badgeType?.let { badge ->
                        notificationViewModel.clearNotification(badge)
                    }
                }
                tab?.let {
                    AppUtils.setStyleForTab(this@MainActivity, tab, Typeface.BOLD)
                }
                setTabIndicatorColor()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    AppUtils.setStyleForTab(this@MainActivity, tab, Typeface.NORMAL)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    if (it == homeTabIndex) startDiscoverPage()
                }
            }
        })

        for (tabIndex in 0..binding.tabs.tabCount) {
            binding.tabs.getTabAt(tabIndex)?.let {
                if (tabIndex == 0) AppUtils.setStyleForTab(this@MainActivity, it, Typeface.BOLD)
                else AppUtils.setStyleForTab(this@MainActivity, it, Typeface.NORMAL)
            }
        }
        setProfileTabIcon()
        setTabIndicatorColor()
    }

    fun setTabIndicatorColor() {
        when (binding.tabs.selectedTabPosition) {
            homeTabIndex -> binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this,
                R.color.colorAccent))
            bonusTabIndex -> binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this,
                R.color.color_rewards))
            profileTabIndex -> binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this,
                R.color.color_profile))
            downloadTabIndex -> binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(
                this,
                R.color.color_downloads))
        }
    }

    private fun observeData() {
        viewModel.errorLiveData.observe(this, {
            Toast.makeText(this, getString(R.string.data_fetch_error), Toast.LENGTH_LONG).show()
        })
        viewModel.progressLiveData.observe(this, {
            if (it) {
                progressShown = true
                showProgress()
            } else {
                hideProgress()
                if (progressShown) {
                    progressShown = false
                    incentiveViewModel.triggerIncentiveEvents()
                }
            }
        })

        viewModel.logoutLiveData.observe(this, {
            //401
            val params = java.util.HashMap<String, String>()
            params["LogoutEvent"] = "Token Expired KMS"
            AnalyticsLogger.getInstance().logEvent(Event.LOGOUT_APP, params)

            startActivity(Intent(this, PhoneLoginActivity::class.java)
                .putExtra(PhoneLoginActivity.WAS_FORCED_LOGOUT, true))
            finish()
        })

        incentiveViewModel.rewardsTrigged.observe(this, {
            if (!it) {
                fetchReferralCode()
            }
        })

        deviceViewModel.deviceConnectionActive.observe(this, {
            it.getContentIfNotHandled()?.let { active ->
                if (active) showConnectionActiveDialog()
            }
        })

        deviceViewModel.wifiFrequencyBand.observe(this, {
            Toast.makeText(this, "Connected to - $it", Toast.LENGTH_SHORT).show()
        })
        contentViewModel.getAllContentProviders()
        contentViewModel.allContentProvidersLiveData.observe(this, {
            for (cp in it) {
                cpIdToContentProviderMap[cp.id] = cp
                if (AppUtils.getContentProviderNameFromId(cp.id).isNullOrEmpty()) {
                    SharedPreferenceStore.getInstance()
                        .save(SharedPreferenceStore.PREFIX_CONTENT_PROVIDER + cp.id, cp.name)
                }
            }
        })
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 2
        firstFragment = MediaLandingFragment()
        profileFragment = ProfileFragment()
        downloadFragment = DownloadFragment()
        rewardsFragment = RewardsFragment()
        binding.viewPager.adapter = adapter
    }

    private fun initializeFireworkSDK() {
        FwSDK.initialize(this.applicationContext,
            BuildConfig.FIREWORK_CLIENT_ID,
            AppUtils.getHash(DeviceUtil.getDeviceId(this)),
            object : FwSDK.SdkStatusListener {
                override fun currentStatus(status: SdkStatus, extra: String) {
                    if (status == SdkStatus.Initialized || status == SdkStatus.InitializationFailed) {
                        val params = HashMap<String, String>()
                        params["status"] = if (extra.isEmpty()) "Initialized" else extra
                        AnalyticsLogger.getInstance().logEvent(Event.FIREWORK_INIT_STATUS, params)

                        BootTelemetryLogger.getInstance().recordPageEvent(
                            BootTelemetryLogger.BootPage.FIREWORKS,
                            BootTelemetryLogger.BootMarker.FIREWORK_SDK_INIT)
                    }
                }
            })
        VideoPlayerProperties.fullScreenPlayer = true
    }

    internal inner class ViewPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return tabCount
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                homeTabIndex -> {
                    return firstFragment
                }
                bonusTabIndex -> {
                    return rewardsFragment
                }
                downloadTabIndex -> {
                    return if (tabCount == 3) profileFragment else downloadFragment
                }
                profileTabIndex -> {
                    return profileFragment
                }
            }
            return Fragment()
        }
    }

    fun startDiscoverPage() {
        //firstFragment.startDiscoverPage()
    }

    private fun fetchReferralCode() {
        if (!SharedPreferenceStore.getInstance().get(KEY_REFERRAL_DIALOG_SHOWN)
                .isNullOrEmpty() || referralRequestInProgress
        ) {
            start(TooltipDisplayHelper.FTUScreen.Main)
            return
        }
        mReferrerClient = InstallReferrerClient.newBuilder(this).build()
        referralRequestInProgress = true
        mReferrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                referralRequestInProgress = false
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> try {
                        val response: ReferrerDetails = mReferrerClient.installReferrer
                        val referrer: String = response.installReferrer
                        mReferrerClient.endConnection()
                        if (referrer.isNotEmpty() && referrer != "utm_source=(not%20set)&utm_medium=(not%20set)"
                            && referrer != "utm_source=google-play&utm_medium=organic"
                        ) {
                            showReferralCode(referrer)
                        } else {
                            showReferralCode("")
                        }
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                        showReferralCode("")
                        Toast.makeText(this@MainActivity, "error " + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        showReferralCode("")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        showReferralCode("")
                    }
                    else -> {
                        showReferralCode("")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    private fun showReferralCode(referral: String) {
        SharedPreferenceStore.getInstance().save(KEY_REFERRAL_DIALOG_SHOWN, "true")
        if (referral.isEmpty()) {
            start(TooltipDisplayHelper.FTUScreen.Main)
            return
        }

        val bottomSheetReferralCodeHome =
            BottomSheetReferralCodeHome(referral, object : OnDismissCallback {
                override fun onDismiss() {
                    start(TooltipDisplayHelper.FTUScreen.Main)
                }
            })
        bottomSheetReferralCodeHome.show(supportFragmentManager, "referralCode")
    }

    companion object {
        const val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
        const val NOTIFICATION_DATA_ID = "NOTIFICATION_DATA_ID"
        const val NOTIFICATION_TAPPED_BY_USER = "NOTIFICATION_TAPPED_BY_USER"
        const val extraShiftTab = "ShiftToTab"
        const val extraShowHubsNearby = "HubsNearby"
        const val extraOrderCompleteEventAmount = "OrderCompleteEventAmount"
        const val extraOrderCompleteEventContentProvider = "OrderCompleteEventContentProvider"

        fun getMishtuNotificationIntent(context: Context?, type: String?, dataId: String?): Intent {
            val openAppIntent = Intent(context, MainActivity::class.java)
            openAppIntent.putExtra(NOTIFICATION_TYPE, type)
            openAppIntent.putExtra(NOTIFICATION_DATA_ID, dataId)
            openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            openAppIntent.action = NOTIFICATION_TAPPED_BY_USER
            return openAppIntent
        }
    }

    fun startService(/*service: BNConstants.Service*/) {
        binding.tabs.selectTab(binding.tabs.getTabAt(homeTabIndex), true)
        //firstFragment.selectedService(service)
    }

    fun startFilmsTab(/*service: BNConstants.Service*/) {
        binding.tabs.selectTab(binding.tabs.getTabAt(homeTabIndex), true)
        firstFragment.getTabLayout().selectTab(firstFragment.getTabLayout().getTabAt(1))
    }

    fun selectTab(index: Int) {
        binding.tabs.selectTab(binding.tabs.getTabAt(index), true)
    }

    fun exportData() {
        notificationViewModel.clearNotification(NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY)
        selectTab(profileTabIndex)
        Handler(Looper.getMainLooper()).postDelayed({
            profileFragment.exportData()
        }, 1000)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRewardReceived(event: RewardsEvent) {
        val rewardFragment = RewardsDialog.newInstance(
            event.eventType,
            event.coins,
            event.contentProviderId, object : RewardsDialog.DialogCloseListener {
                override fun onCloseClick() {
                    alphaAnimateCoin()
                }
            }
        )
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.rewardsContainer, rewardFragment)
        transaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBadgeEventReceived(event: BadgeEvent) {
        val tabIndex = when (event.eventType) {
            NotificationBadgeHelper.BadgeType.NEW_CONTENT -> homeTabIndex
            NotificationBadgeHelper.BadgeType.NEW_OFFER -> bonusTabIndex
            NotificationBadgeHelper.BadgeType.NEW_DOWNLOADS -> downloadTabIndex
            NotificationBadgeHelper.BadgeType.PROFILE -> profileTabIndex
            NotificationBadgeHelper.BadgeType.PACK_EXPIRED -> profileTabIndex
            NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY -> profileTabIndex
        }
        if (event.active) {
            if (tabIndex == homeTabIndex) {
                firstFragment.showBadge(event.count)
            }

            if (tabIndex == binding.tabs.selectedTabPosition) return

            binding.tabs.getTabAt(tabIndex)?.let {
                it.orCreateBadge.backgroundColor =
                    ContextCompat.getColor(this, R.color.pack_expired_color)
                it.orCreateBadge.badgeTextColor =
                    ContextCompat.getColor(this, R.color.white)
                it.orCreateBadge.number = event.count
            }
        } else {
            binding.tabs.getTabAt(tabIndex)?.removeBadge()
        }
    }

    internal fun showDownloadsFTU() {
        if (getLastStatus() != HubScanStatus.HUB_EXIST) return
        if (SharedPreferenceStore.getInstance().get(TooltipDisplayHelper.FTUScreen.Downloads.name)
                .isNullOrEmpty()
        ) {
            SharedPreferenceStore.getInstance()
                .save(TooltipDisplayHelper.FTUScreen.Downloads.name, "true")
            binding.mainFtuParent.visibility = View.VISIBLE

            binding.downloadFtuIcon.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                this,
                R.anim.scale_in_out_download))

            binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(
                this,
                getString(R.string.find_a_nearest_shop_and_save_data),
                getString(R.string.up_to_100)
            )

            binding.mainFtuClose.setOnClickListener {
                binding.downloadFtuIcon.clearAnimation()
                binding.mainFtuParent.visibility = View.GONE
            }

            binding.downloadFtuSelect.setOnClickListener {
                binding.downloadFtuIcon.clearAnimation()
                binding.mainFtuParent.visibility = View.GONE
                selectTab(downloadTabIndex)
            }
        }
    }

    private fun alphaAnimateCoin() {
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.fillAfter = true
        animation.duration = 250
        animation.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.coinImageview.visibility = View.VISIBLE
                scaleAnimateCoin()
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {}
        })
        binding.coinImageview.startAnimation(animation)
    }

    private fun scaleAnimateCoin() {
        val animSet = AnimationSet(true)
        val scale = ScaleAnimation(
            1f,
            0.22f,
            1f,
            0.22f,
            ScaleAnimation.RELATIVE_TO_SELF,
            if (tabCount == 4) -0.25f else .5f,
            ScaleAnimation.RELATIVE_TO_PARENT,
            .5f
        )
        scale.duration = 800
        animSet.addAnimation(scale)
        animSet.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.coinImageview.clearAnimation()
                binding.coinImageview.visibility = ViewGroup.INVISIBLE

                supportFragmentManager.findFragmentById(R.id.rewardsContainer)?.let {
                    supportFragmentManager.beginTransaction()
                        .remove(it).commit()
                    fetchReferralCode()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        binding.coinImageview.startAnimation(animSet)
    }

    private fun showConnectionActiveDialog() {
        val layout: View = layoutInflater.inflate(R.layout.dialog_connection_active, null)
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(layout)
        dialog.setCanceledOnTouchOutside(true)
        val window: Window? = dialog.window
        window?.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        val lp: WindowManager.LayoutParams? = window?.attributes
        lp?.dimAmount = 0.7f
        lp?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        dialog.window?.attributes = lp
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 5000)
    }

    fun getTabLayout(): TabLayout {
        return binding.tabs
    }

    fun getProfileTab(): TabLayout.Tab? {
        return binding.tabs.getTabAt(profileTabIndex)
    }

    fun getTopNavigationTabLayout(): TabLayout {
        return firstFragment.getTabLayout()
    }

    fun getAvailableCoinLayout(): View {
        return rewardsFragment.availableCoinsView()
    }

    fun getActiveOrderLayout(): View? {
        return firstFragment.getActiveOrderCardCTA()
    }

    fun buyPackButton(): Button? {
        return firstFragment.buyPackButton()
    }

    private fun setTabCount() {
        val hubExist = getLastStatus()
        tabCount = if (hubExist == HubScanStatus.HUB_EXIST || previouslyVisitedHubArea()) 4 else 3

        profileTabIndex = if (tabCount == 3) {
            if (binding.tabs.tabCount == 4) binding.tabs.removeTabAt(2)
            2
        } else {
            if (binding.tabs.tabCount == 3) {
                val tab = binding.tabs.newTab()
                tab.text = getString(R.string.download)
                tab.icon = ContextCompat.getDrawable(this, R.drawable.bg_tab_downloads)
                binding.tabs.addTab(tab, 2)
                AppUtils.setStyleForTab(this@MainActivity, tab, Typeface.NORMAL)
            }
            3
        }
    }

    internal fun setProfileTabIcon() {
        val index = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_AVATAR_INDEX)
        if (!index.isNullOrEmpty()) {
            val avatar = BNConstants.Avatars[index.toInt()]
            val id: Int = resources.getIdentifier(avatar, "drawable",
                packageName)
            binding.tabs.getTabAt(profileTabIndex)?.icon = ContextCompat.getDrawable(this, id)
        }
    }

    private fun showOrderCompleteRewards() {
        val isOrderCompleteAfterRedeemOffer =
            SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_REDEEM_OFFER_DONE)
        if (!isOrderCompleteAfterRedeemOffer.isNullOrEmpty()) {
            //Reset flags
            SharedPreferenceStore.getInstance()
                .save(SharedPreferenceStore.KEY_REDEEM_OFFER_DONE, "")
            orderCompleteEventAmount = -1
            return
        }

        val orderCompleteIncentiveList =
            incentiveViewModel.planContainsEvent(BOConverter.EventType.CONSUMER_INCOME_ORDER_COMPLETED, orderCompleteEventContentProviderId)

        if (orderCompleteIncentiveList.isNotEmpty()) {
            var totalCoins = 0;
            var eventSubType = ""
            for (incentive in orderCompleteIncentiveList) {
                if(incentive != null) {
                    if(!incentive.eventSubType.isNullOrEmpty()) {
                        eventSubType = incentive.eventSubType
                    }
                    val coins = when (incentive.formula.formulaType) {

                        BOConverter.FormulaType.PLUS.value -> orderCompleteEventAmount + incentive.formula.firstOperand
                        BOConverter.FormulaType.PERCENTAGE.value -> orderCompleteEventAmount * (incentive.formula.firstOperand.toFloat() / 100.0)
                        BOConverter.FormulaType.MULTIPLY.value -> orderCompleteEventAmount  * incentive.formula.firstOperand.toFloat()
                        else -> {
                            incentive.formula.firstOperand
                        }
                    }
                    totalCoins += coins.toInt()
                }

            }

            EventBus.getDefault().post(RewardsEvent(totalCoins,
                    BOConverter.EventType.CONSUMER_INCOME_ORDER_COMPLETED, eventSubType))
            orderCompleteEventAmount = -1

        }
    }

}