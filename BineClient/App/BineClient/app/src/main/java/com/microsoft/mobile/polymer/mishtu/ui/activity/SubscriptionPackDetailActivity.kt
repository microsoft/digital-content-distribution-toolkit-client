package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.SubscriptionPackDetailBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui.ToolTip
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui.ToolTipManager
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.adapter.SubscriptionMovieAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SubscriptionViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.BinePlayerListenerActivity
import com.microsoft.mobile.polymer.mishtu.utils.GridSpacingItemDecoration


class SubscriptionPackDetailActivity: BinePlayerListenerActivity(), SubscriptionMovieAdapter.SubscriptionMovieAdapterListener {
    private lateinit var binding: SubscriptionPackDetailBinding
    private lateinit var subscriptionPack: SubscriptionPack
    private lateinit var subscriptionMovieAdapter: SubscriptionMovieAdapter
    private var toolBarTitle :String? = null
    private var packSubTitle :String? = null
    private val orderViewModel by viewModels<OrderViewModel>()

    companion object{
        const val EXTRA_SUBSCRIPTION_PACK = "subscriptionPack"
        const val EXTRA_TOOLBAR_TITLE = "toolBarTitle"
        const val EXTRA_PACK_SUBTITLE = "packSubTitle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.subscription_pack_detail)
        AnalyticsLogger.getInstance().logScreenView(Screen.SUBSCRIPTION_DETAIL)
        binding.backButton.setOnClickListener {
            finish()
        }
        subscriptionPack = intent.getSerializableExtra(EXTRA_SUBSCRIPTION_PACK) as SubscriptionPack
        toolBarTitle = intent.getSerializableExtra(EXTRA_TOOLBAR_TITLE) as String?
        packSubTitle = intent.getSerializableExtra(EXTRA_PACK_SUBTITLE) as String?
        if(!toolBarTitle.isNullOrEmpty()){
            binding.toolBar.title = toolBarTitle
        }
        AppUtils.loadImage(this, AppUtils.getContentProviderSquareLogoURL(subscriptionPack.contentProviderId),binding.ottLogo)
        binding.packTitle.text = subscriptionPack.title
        binding.movieCount.text = packSubTitle

        binding.buttonSelectPack.text = getString(R.string.buy_rs_pack, subscriptionPack.price)
        binding.packDescription.text = getString(R.string.pack_terms_of_service,subscriptionPack.durationDays.toString())

        subscriptionMovieAdapter = SubscriptionMovieAdapter(this, ArrayList(),this)

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 4)
        val includeEdge = false
        binding.contentRecyclerView.addItemDecoration(GridSpacingItemDecoration(4,
                AppUtils.dpToPx(10.0f, resources),
                includeEdge))
        binding.contentRecyclerView.layoutManager = mLayoutManager
        binding.contentRecyclerView.adapter = subscriptionMovieAdapter

        binding.buttonSelectPack.setOnClickListener {
            orderViewModel.createOrder(subscriptionPack)
        }
        observeData()
    }

    override fun teachingScreenName(): TooltipDisplayHelper.FTUScreen {
        return TooltipDisplayHelper.FTUScreen.None
    }

    override fun teachingContainer(): Int {
        return 0
    }

    override fun completionCallback(): TooltipDisplayHelper.TooltipDisplayCompletion? {
        return null
   }

    override fun shouldShowOnResume(): Boolean {
        return false
    }

    private fun observeData() {
        contentViewModel.getAllPaidContentByProviderId(subscriptionPack.contentProviderId)
        contentViewModel.allPaidContentLiveData.observe(this) { conList ->
        if (subscriptionPack.subscriptionType == SubscriptionPack.PackType.TVOD.value) {
            subscriptionPack.contentIdList?.let { conIds ->
                subscriptionMovieAdapter.setDataList(conList.filter { con-> conIds.contains(con.contentId) }.distinctBy { con -> con.name })
            }
        } else {
                conList?.let {
                    subscriptionMovieAdapter.setDataList(it.distinctBy { con -> con.name })
                }
            }
        }

        orderViewModel.success.observe(this) {
            if (it) {
                AppUtils.showNearbyStore(this, null, false, subscriptionPack.contentProviderId, subscriptionPack.id)
                //dismiss()
            }
        }
            orderViewModel.error.observe(this) {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
            orderViewModel.errorCode.observe(this) {
                if (it != null && it == 400)
                    Toast.makeText(this, this.getString(R.string.create_duplicate_order_error), Toast.LENGTH_SHORT).show()
            }
            orderViewModel.loading.observe(this) {
                if (it) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }



    }

    override fun onItemClicked(content: Content) {
        AppUtils.showContentDetails(supportFragmentManager,BOConverter.getContentDownloadFromContent(content),null)
    }
}