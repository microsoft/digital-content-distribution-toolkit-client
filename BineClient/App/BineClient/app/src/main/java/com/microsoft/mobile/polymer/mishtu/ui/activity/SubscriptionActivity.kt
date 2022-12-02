// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivitySubscriptionBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ActiveSubscriptionAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

class SubscriptionActivity : BaseActivity() {

    private lateinit var binding: ActivitySubscriptionBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private lateinit var adapter: ActiveSubscriptionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription)

        AnalyticsLogger.getInstance().logScreenView(Screen.PROFILE_SUBSCRIPTION)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        binding.subscriptionPacksLayout.setOnClickListener {
            AppUtils.showSubscriptionPacks(this, null)
        }

        binding.subscriptionPreviousLayout.setOnClickListener {
            val intent = Intent(this, PurchasedSubscriptionsActivity::class.java)
            startActivity(intent)
        }
        adapter = ActiveSubscriptionAdapter(this, supportFragmentManager)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.subscriptionRecyclerView.layoutManager = layoutManager
        binding.subscriptionRecyclerView.adapter = adapter

        observeData()
    }

    private fun observeData() {
        contentViewModel.allActiveSubscriptionLiveData().observe(this, { sub ->
            sub?.let {
                adapter.setData(sub)
            }
        })
    }
}