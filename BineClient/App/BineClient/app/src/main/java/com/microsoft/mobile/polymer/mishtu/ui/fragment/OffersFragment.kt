package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R

import com.microsoft.mobile.polymer.mishtu.databinding.FragmentOffersBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.OfferDetailsActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.OffersListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.IncentiveViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SubscriptionViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.AutoScrollContentView
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OffersFragment : Fragment(), OffersListAdapter.OfferClickListener {

    lateinit var binding: FragmentOffersBinding
    lateinit var adapter: OffersListAdapter
    private val subscriptionViewModel by viewModels<SubscriptionViewModel>()
    private val contentViewModel by activityViewModels<ContentViewModel>()
    private val incentiveViewModel by viewModels<IncentiveViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOffersBinding.inflate(inflater)

        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.offersRecyclerView.layoutManager = mLayoutManager

        adapter = OffersListAdapter(requireContext(), this)
        binding.offersRecyclerView.adapter = adapter

        subscriptionViewModel.getAllSubscriptionPacks()
        //contentViewModel.getAllContentProviders()
        observeData()

        return binding.root
    }

    fun setTotalCoins(coins: Int) {
            adapter.setTotalCoins(coins)
    }

    private fun observeData() {
        subscriptionViewModel.allSubscriptionPack.observe(viewLifecycleOwner) {
            Log.d("Subscriptions", "" + it.size)
            if (it.isNotEmpty()) {
                val array = it.filter { s -> s.isReadable }
                adapter.setSubscriptions(array.sortedBy { s -> s.redemptionValue })
            }
        }


        contentViewModel.allPaidContentLiveData.observe(viewLifecycleOwner, {
            adapter.setContent(it)
        })



        incentiveViewModel.loading.observe(viewLifecycleOwner, {
            if(it) (requireActivity() as? BaseActivity)?.showProgress()
            else (requireActivity() as? BaseActivity)?.hideProgress()
        })

        incentiveViewModel.success.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), getString(R.string.offer_redeemed_message), Toast.LENGTH_LONG).show()
            (requireActivity() as? MainActivity)?.startService(/*BNConstants.Service.ENTERTAINMENT*/)
        })

        incentiveViewModel.error.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    fun canRedeam(subscriptionPack: SubscriptionPack): Boolean{
        val cpIdtoSubsMap = (requireActivity() as BaseActivity).cpIdToActiveSubscriptionMap
        val activeSubscription = cpIdtoSubsMap.get(subscriptionPack.contentProviderId)
        val canRedeem = (activeSubscription == null  || activeSubscription.isExpired()) &&
                incentiveViewModel.expenseEventExist()
        return canRedeem
    }

    override fun onRedeemClicked(subscriptionPack: SubscriptionPack) {
        val canRedeem = canRedeam(subscriptionPack)
        if (canRedeem) {
            AppUtils.showConfirmRedeemOfferDialog(requireContext(), subscriptionPack) { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (subscriptionViewModel.hasActiveOrder(subscriptionPack.contentProviderId)) {
                            AppUtils.startActiveOrderDialog(childFragmentManager, subscriptionPack.contentProviderId)
                        }
                        else {
                            incentiveViewModel.redeemOffer(subscriptionPack)
                        }
                        dialog.dismiss()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }
        }
        else {
            if (!incentiveViewModel.expenseEventExist())
                Toast.makeText(requireContext(), getString(R.string.error_no_incentive_plan), Toast.LENGTH_LONG).show()
            else Toast.makeText(requireContext(), getString(R.string.subscription_exist_error), Toast.LENGTH_LONG).show()
        }
    }

    override fun showOfferClicked(subscriptionPack: SubscriptionPack) {
        val intent = Intent(context, OfferDetailsActivity::class.java)
        intent.putExtra(OfferDetailsActivity.EXTRA_KEY_SUBSCRIPTION, subscriptionPack)
        intent.putExtra(OfferDetailsActivity.EXTRA_KET_CAN_REDEEM, canRedeam(subscriptionPack))
        resultLauncher.launch(intent)
    }

    override fun startAutoScroll(autoScrollContentView: AutoScrollContentView) {
        lifecycleScope.launch {
            autoScrollContentView.startAutoScroll()
        }
    }

    override fun stopAutoScroll(autoScrollContentView: AutoScrollContentView) {
        autoScrollContentView.stopScroll()
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            (requireActivity() as? MainActivity)?.startService(/*BNConstants.Service.ENTERTAINMENT*/)
        }
    }
}