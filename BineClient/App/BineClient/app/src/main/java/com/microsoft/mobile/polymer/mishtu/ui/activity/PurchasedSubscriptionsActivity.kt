// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewSubscriptionsPurchasedBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen

import com.microsoft.mobile.polymer.mishtu.ui.adapter.PurchasedOrdersListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SubscriptionViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.SearchView
import com.microsoft.mobile.polymer.mishtu.utils.*
import com.msr.bine_sdk.cloud.models.Order
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PurchasedSubscriptionsActivity: BaseActivity(),
    PurchasedOrdersListAdapter.OnOrderItemClickedListener,VoiceSearchUtil.VoiceSearchCallBack {

    private val viewModel by viewModels<SubscriptionViewModel>()
    private var adapter: PurchasedOrdersListAdapter? = null
    private lateinit var binding: ViewSubscriptionsPurchasedBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private lateinit var contract: ActivityResultLauncher<Intent>
    @Inject lateinit var subscriptionManager: SubscriptionManager

    private var selectedPackCP: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
                DataBindingUtil.setContentView(this, R.layout.view_subscriptions_purchased)

        AnalyticsLogger.getInstance().logScreenView(Screen.PROFILE_PREVIOUS_SUBS)


        binding.subscriptionSearchView.setData(
                resources.getString(R.string.bn_subscription_hint),
                true, false, true
        )
        binding.subscriptionSearchView.setOnSearchViewListener(object :
                SearchView.OnSearchViewListener {
            override fun onBackButtonCLicked() {}

            override fun onMicButtonClicked() {
                initVoiceInput()
            }

            override fun onTextChanged(charSequence: String?) {
                if (!charSequence.isNullOrEmpty()) {
                    adapter?.filter?.filter(charSequence)
                }
            }

            override fun onInputEnterKeyCLicked(charSequence: String?) {}

        })
        binding.backButton.setOnClickListener {
            finish()
        }
        observeData()
        viewModel.getUserSubscription()

        binding.subscriptionRenew.setOnClickListener {
            if (selectedPackCP.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.select_pack_to_renew), Toast.LENGTH_SHORT).show()
            } else {
                AppUtils.startOrderFlow(supportFragmentManager, this, selectedPackCP)
            }
        }

        dialog.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }
        contract = VoiceSearchUtil.getNewInstance().getContract(this, this)

    }
    private fun initVoiceInput() {
        VoiceSearchUtil.getNewInstance().voiceInput(this, contract)
    }


    private fun observeData() {
        val map = HashMap<String, Content>()
        contentViewModel.allContentLiveData.observe(this) {
            for (con in it) {
                map[con.contentId] = con
            }
        }

        viewModel.purchasedSubscriptions.observe(this) {
            adapter = PurchasedOrdersListAdapter(this, this, it, it, true)
            adapter?.setContent(map)
            binding.subscriptionRecyclerView.adapter = adapter
            adapter?.notifyDataSetChanged()
        }




        viewModel.error.observe(this) {
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOrderItemClicked(order: Order) {
        viewModel.canBuyPack(order)
        viewModel.canBuyPack.observe(this){
            selectedPackCP = order.orderItems[0].subscription.contentProviderId
            binding.subscriptionRenew.isEnabled = it
        }
    }

    override fun orderTitleClicked(order: Order, subTitle: String?) {
        viewModel.getSubscriptionByIdLiveData(order.orderItems[0].subscription.id).observe(this) { subscriptionPack ->
            if (subscriptionPack != null) {
                val intent = Intent(this, SubscriptionPackDetailActivity::class.java)
                intent.putExtra(SubscriptionPackDetailActivity.EXTRA_SUBSCRIPTION_PACK, BOConverter.bnSubscriptionPackToBO(order.orderItems[0].subscription))
                intent.putExtra(SubscriptionPackDetailActivity.EXTRA_TOOLBAR_TITLE, getString(R.string.bn_previous_subscriptions))
                intent.putExtra(SubscriptionPackDetailActivity.EXTRA_PACK_SUBTITLE, subTitle)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.pack_is_not_active), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSerachQueryDetected(res: String) {
        binding.subscriptionSearchView.setData(
                res,
                true, false, false
        )
        adapter?.filter?.filter(res)
    }
}