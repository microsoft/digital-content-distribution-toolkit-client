// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.microsoft.mobile.polymer.mishtu.ui.adapter.RetailerListAdapter
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.activity.NearbyStoresActivity
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentNearbyStoreBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import com.msr.bine_sdk.cloud.models.RetailerDistance
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils


@AndroidEntryPoint
class NearbyStoreFragment(val contentProviderId: String?, val subscriptionId: String?) : Fragment() {

    lateinit var adapter: RetailerListAdapter
    lateinit var binding: FragmentNearbyStoreBinding
    private val orderViewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_nearby_store,
                container,
                false
        )
        binding.btnWatchClips.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.putExtra(MainActivity.extraShiftTab, 1)
            startActivity(intent)
            activity?.finish()
        }
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        AnalyticsLogger.getInstance().logScreenView(Screen.NEARBY_STORES)

        binding.toolBar.title = ""

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        adapter = RetailerListAdapter(requireContext(),
            requireActivity(),
            ArrayList(),
            ArrayList(),
            contentProviderId,
            childFragmentManager)
        binding.nearbyStoreRecyclerview.adapter = adapter

        binding.subscriptionFindStore.setOnClickListener {
            (activity as NearbyStoresActivity?)?.startStoreDetails(adapter.getSelectedRetailer().retailer)
        }
        binding.btnClose.setOnClickListener {
            requireActivity().finish()
        }

        //getCurrentOrder()
        return binding.root
    }
    private fun observeSubscriptionData(subscriptionId: String?) {
        if (subscriptionId != null) {
            orderViewModel.getSubscriptionPackLiveData(subscriptionId)
            orderViewModel.subscription.observe(viewLifecycleOwner){
                if(it != null) {
                    showSubscriptionPackInfo(it.price, it.durationDays)
                }
            }
        }
        else if(contentProviderId != null){
            val activeOrder = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_ORDER_ID+"_$contentProviderId")
            activeOrder?.let {
                orderViewModel.getActiveorder(it)
                orderViewModel.currentOrder.observe(viewLifecycleOwner){order->
                    showSubscriptionPackInfo(order.price, order.duration)
                }
            }
        }
    }

    fun setRetailerListData(retailers: List<RetailerDistance>, showRetailerWithHub: Boolean) {
        binding.nearbyStoresResult.visibility = View.VISIBLE
        binding.nearbyStoresNoResult.visibility = View.GONE
        binding.toolBar.title =
            String.format(getString(R.string.bn_found_stores_near_you), retailers.size)
        if (showRetailerWithHub) {
            hideSubscriptionPackInfo()
        }else{
            observeSubscriptionData(subscriptionId)
        }
        adapter.setRetailerList(retailers)
    }

    fun setRetailerListDataForSelectedContent(
        retailers: List<Pair<RetailerDistance, String>>,
        content: Content,
    ) {
        binding.nearbyStoresResult.visibility = View.VISIBLE
        binding.nearbyStoresNoResult.visibility = View.GONE
        hideSubscriptionPackInfo()
        adapter.setRetailerList(retailers, content)

    }

    private fun hideSubscriptionPackInfo() {
        binding.packLogo.visibility = View.GONE
        binding.nearbyStorePackInfoTv2.visibility = View.GONE
        binding.nearbyDesc.visibility = View.GONE
        binding.nearbyStorePackInfoTv1.text = getString(R.string.select_shop_to_download_for_free)
        val lp = binding.nearbyTitleContainer.layoutParams
        lp.height = resources.getDimension(R.dimen.dp_144).toInt()
    }

    fun setNoRetailersFound(isPackFlow: Boolean) {
        binding.nearbyStoresNoResult.visibility = View.VISIBLE
        binding.nearbyStoresResult.visibility = View.GONE
        binding.toolBar.title = ""
        binding.nearbyNostoresTitle.text = if (isPackFlow) getString(R.string.bn_no_stores_nearby) else getString(R.string.bn_no_stores_nearby_download)
    }

    private fun showSubscriptionPackInfo(price: Int, duration: Int) {
        binding.nearbyStorePackInfoTv1.text =
            getString(R.string.btn_subscription_info, duration, price)
        binding.nearbyStorePackInfoTv2.text = getString(R.string.btn_subscription_desc)
        contentProviderId?.let {
            AppUtils.loadImage(requireContext(), AppUtils.getContentProviderSquareLogoURL(it),
                binding.packLogoImageView)
        }
    }
}