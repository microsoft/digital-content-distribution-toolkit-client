// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityOfferDetailsBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.IncentiveViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import kotlinx.coroutines.launch
import java.util.*

class OfferDetailsActivity : BaseActivity() {

    lateinit var binding: ActivityOfferDetailsBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private val incentiveViewModel by viewModels<IncentiveViewModel>()
    lateinit var subscriptionPack: SubscriptionPack

    private var canRedeem = false

    companion object {
        const val EXTRA_KEY_SUBSCRIPTION = "SubscriptionBO"
        const val EXTRA_KET_CAN_REDEEM = "CanRedeem"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_offer_details)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.title = getString(R.string.offers)

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        subscriptionPack = intent.getSerializableExtra(EXTRA_KEY_SUBSCRIPTION) as SubscriptionPack
        binding.offerDetailsPackDuration.text = String.format(getString(R.string.n_days_pack),
            subscriptionPack.durationDays)
        AppUtils.loadImage(this, AppUtils.getContentProviderSquareLogoURL(subscriptionPack.contentProviderId), binding.offerDetailsOttLogo)
        binding.offerDetailsCoinsText.text = String.format(getString(R.string.pay_with_coins), subscriptionPack.redemptionValue)
        canRedeem =  intent.getBooleanExtra(EXTRA_KET_CAN_REDEEM, true)

        if (canRedeem)
            binding.offerDetailsRedeemButton.background = ContextCompat.getDrawable(this, R.drawable.bn_bg_btn_round)
        else binding.offerDetailsRedeemButton.background = ContextCompat.getDrawable(this, R.drawable.bn_bg_btn_round_disabled)

        binding.offerDetailsTerms.text =  getString(R.string.pack_terms_of_service, subscriptionPack.durationDays.toString())
        binding.offerDetailsRedeemButton.setOnClickListener {
            if (canRedeem) {
                AppUtils.showConfirmRedeemOfferDialog(this, subscriptionPack) { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            dialog.dismiss()
                            if (contentViewModel.hasActiveOrder(subscriptionPack.contentProviderId)) {
                                AppUtils.startActiveOrderDialog(supportFragmentManager, subscriptionPack.contentProviderId)
                            }
                            else {
                                incentiveViewModel.redeemOffer(subscriptionPack)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, getString(R.string.subscription_exist_error), Toast.LENGTH_LONG).show()
            }
        }
        observeData()
    }

    private fun observeData() {
        contentViewModel.allPaidContentLiveData.observe(this) {contents->
            binding.offerDetailsContent.setData(if(subscriptionPack.subscriptionType == SubscriptionPack.PackType.SVOD.value)
            {
                contents.filter { con-> con.contentProviderId == subscriptionPack.contentProviderId }.distinctBy { it.name }
            }
            else{
                contents.filter { con-> subscriptionPack.contentIdList!!.contains(con.contentId) }.distinctBy { it.name }
            })
            lifecycleScope.launch {
                binding.offerDetailsContent.startAutoScroll()
            }
        }

        incentiveViewModel.loading.observe(this) {
            if (it) showProgress()
            else hideProgress()
        }

        incentiveViewModel.success.observe(this, {
            Toast.makeText(this, getString(R.string.offer_redeemed_message), Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_OK, Intent())
            finish()
        })

        incentiveViewModel.error.observe(this, {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
    }
}