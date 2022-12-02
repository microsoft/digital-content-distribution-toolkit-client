// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope

import com.microsoft.mobile.polymer.mishtu.ui.adapter.SubscriptionListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SubscriptionViewModel
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewSubscriptionPacksBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ContentProviderAdapter
import com.microsoft.mobile.polymer.mishtu.ui.fragment.ContentFragment
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentProvider
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.AutoScrollContentView
import com.microsoft.mobile.polymer.mishtu.ui.views.SearchView
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BinePlayerListenerActivity
import com.microsoft.mobile.polymer.mishtu.utils.VoiceSearchUtil

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

@AndroidEntryPoint
open class SubscriptionPackListActivity : BinePlayerListenerActivity(),
    SubscriptionListAdapter.SubscriptionListAdapterListener, ContentProviderAdapter.onItemClickListner,
    VoiceSearchUtil.VoiceSearchCallBack {

    private val viewModel by viewModels<SubscriptionViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()
    private lateinit var binding: ViewSubscriptionPacksBinding
    private lateinit var adapter: SubscriptionListAdapter
    private lateinit var contract: ActivityResultLauncher<Intent>
    private  var contentProviderId: String? = null
    private var selectedPack: String? = null
    private var searchQuery: String? = null
    companion object {
        const val CONTENTPROVIDER_ID = "contentProviderId"
        const val SUBSCRIPTION = "Subscription"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this, R.layout.view_subscription_packs)

        AnalyticsLogger.getInstance().logScreenView(Screen.VIEW_ALL_SUBS)

        adapter = SubscriptionListAdapter(this, ArrayList(), this)
        binding.subscriptionRecyclerView.adapter = adapter
        contentProviderId = intent.getStringExtra(CONTENTPROVIDER_ID)

        if(contentProviderId.isNullOrEmpty()){
            contentProviderId = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+"_$SUBSCRIPTION").toString()
        }
        if(contentProviderId.isNullOrEmpty()){
           changeContentProvider()
        }
        contentProviderId?.let {
            setContentProviderInfo(it)
        }
        binding.changeContentProvider.setOnClickListener {
            changeContentProvider()
        }
        binding.backButton.setOnClickListener{
            finish()
        }

        contract = VoiceSearchUtil.getNewInstance().getContract(this, this)
        binding.searchContainer.setOnSearchViewListener(onSearchViewListener())
        binding.searchContainer.setData(getString(R.string.bn_search_by_actor_movie),true, false,true)
        if(!contentProviderId.isNullOrEmpty()) {
            viewModel.getSubscriptionPacks(contentProviderId!!)
        }
        observeData()
        observeSearchData()

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

    private fun observeSearchData() {
        viewModel.searchByQueryResult.observe(this) {
            val txtSpannable = SpannableString(searchQuery)
            txtSpannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.changeContentProvider.visibility = View.GONE
            binding.packListTitle.visibility = View.GONE
            binding.titleResults.visibility = View.VISIBLE
            adapter.setSubscriptionList(it)

            if (!it.isNullOrEmpty()) {
                binding.titleResults.text = TextUtils.concat(getString(R.string.showing_result)+" ", txtSpannable)
            } else {
                binding.titleResults.text = TextUtils.concat(getString(R.string.no_result)+" ", txtSpannable)
            }
        }
    }

    private fun onSearchViewListener() = object : SearchView.OnSearchViewListener {
        override fun onBackButtonCLicked() {
        }

        override fun onMicButtonClicked() {
            initVoiceInput()
        }

        override fun onTextChanged(charSequence: String?) {

            binding.searchContainer.setData(charSequence,true, false,true)
            if(!charSequence.isNullOrEmpty()) {
                searchQuery = charSequence
                viewModel.getSubscriptionPackFromQuery(charSequence)
                binding.subscriptionRecyclerView.visibility = View.VISIBLE
            }else{
                searchQuery = ""
                binding.titleResults.visibility = View.GONE
                binding.subscriptionRecyclerView.visibility = View.GONE
            }
        }

        override fun onInputEnterKeyCLicked(charSequence: String?) {
            binding.searchContainer.setData(charSequence,true, false,false)
        }
    }

    private fun setContentProviderInfo(contentProviderId: String) {
        binding.changeContentProvider.visibility = View.VISIBLE
        binding.packListTitle.text = getString(R.string.choose_pack_from,AppUtils.getContentProviderNameFromId(contentProviderId))
        AppUtils.loadImage(this,
            AppUtils.getContentProviderSquareLogoURL(contentProviderId),
            binding.cpImageview)
        binding.selectedContentProvider.text = getString(R.string.you_are_watching,
            AppUtils.getContentProviderNameFromId(contentProviderId))
    }

    private fun changeContentProvider() {
        val bottomSheet = BottomSheetContentProvider(this)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun initVoiceInput() {
        VoiceSearchUtil.getNewInstance().voiceInput(this, contract)
    }

    //should not call if cpid is null
    private fun observeData() {
        val map = HashMap<String, Content>()
        contentViewModel.allContentLiveData.observe(this) {
            for (con in it) {
                map[con.contentId] = con
            }
        }

        viewModel.subscriptionPack.observe(this) {
            if (it.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.error_no_subscriptions),
                        Toast.LENGTH_SHORT
                ).show()
                binding.subscriptionRecyclerView.visibility = View.GONE
                return@observe
            } else {
                binding.subscriptionRecyclerView.visibility = View.VISIBLE
                adapter.setSubscriptionList(it)
                adapter.setContent(map)
            }
        }


        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        orderViewModel.success.observe(this) {
            if (it) {
                AppUtils.showNearbyStore(this, null, false, contentProviderId, selectedPack)
                //dismiss()
            }
        }
        if (!contentProviderId.isNullOrEmpty()) {
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
    }

    override fun onSelect(subscriptionPack: SubscriptionPack) {
        contentProviderId = subscriptionPack.contentProviderId
        selectedPack = subscriptionPack.id
        orderViewModel.createOrder(subscriptionPack)
    }

    override fun startAutoScroll(autoScrollContentView: AutoScrollContentView) {
        lifecycleScope.launch {
            autoScrollContentView.startAutoScroll()
        }
    }

    override fun stopAutoScroll(autoScrollContentView: AutoScrollContentView) {
            autoScrollContentView.stopScroll()
        }

    override fun aboutPack(subscriptionPack: SubscriptionPack, subTitle: String) {
        val intent = Intent(this,SubscriptionPackDetailActivity::class.java)
        intent.putExtra(SubscriptionPackDetailActivity.EXTRA_SUBSCRIPTION_PACK,subscriptionPack)
        intent.putExtra(SubscriptionPackDetailActivity.EXTRA_PACK_SUBTITLE,subTitle)
        startActivity(intent)
    }

    override fun onItemClicked(contentProvider: String) {
        val selectedMovieCPKey = SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+"_${ContentFragment.TYPE.MOVIES.name}"
        val selectedSeriesCPKey = SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+"_${ContentFragment.TYPE.SERIES.name}"
        val cpidMovie = SharedPreferenceStore.getInstance().get(selectedMovieCPKey)
        val cpidSeries = SharedPreferenceStore.getInstance().get(selectedSeriesCPKey)
        if(cpidMovie.isNullOrEmpty() && cpidSeries.isNullOrEmpty()){
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+"_$SUBSCRIPTION",contentProvider)
        }
        setContentProviderInfo(contentProvider)
        viewModel.getSubscriptionPacks(contentProvider)
    }

        override fun onSerachQueryDetected(res: String) {
            binding.searchContainer.setData(res,true, false,false)

        }
    }