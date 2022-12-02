// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityTermsConditionBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen

class TermsAndCondition : BaseActivity(){
    private lateinit var binding: ActivityTermsConditionBinding
    enum class PageType{
        TNC,
        Privacy
    }

    companion object {

        const val EXTRA_PAGE_TYPE = "PageType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_terms_condition)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.bn_close_black)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.get(EXTRA_PAGE_TYPE)?.toString()?.let {
            when(it) {
                PageType.Privacy.name -> binding.toolbar.title =getString(R.string.bn_privacy_policy)
                PageType.TNC.name -> binding.toolbar.title =getString(R.string.bn_terms_conditions)
            }

        }
        AnalyticsLogger.getInstance().logScreenView(Screen.TERMS_CONDITIONS)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}
