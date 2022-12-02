// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.loopnow.fireworklibrary.FeedType
import com.loopnow.fireworklibrary.views.OnItemClickedListener
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityFireworkCategoryBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants


class FireworkCategoryActivity : BaseActivity() {

    lateinit var binding: ActivityFireworkCategoryBinding
    private lateinit var category:String
    lateinit var categoryId:String
   /* private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>*/

    companion object {
        const val EXTRA_CATEGORY_TITLE = "CategoryTitle"
        const val EXTRA_CATEGORY_ID = "CategoryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{
            binding = DataBindingUtil.setContentView(this, R.layout.activity_firework_category)

            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.toolBar.setNavigationIcon(R.drawable.bn_close_black)
            binding.toolBar.setNavigationOnClickListener {
                finish()
            }

            intent.getStringExtra(EXTRA_CATEGORY_TITLE)?.let {
                category = it
            }
            intent.getStringExtra(EXTRA_CATEGORY_ID)?.let {
                categoryId = it
            }
            if (category.isEmpty() || categoryId.isEmpty()) {
                val list = BNConstants.FwCategories[0].split(":")
                category = list[0]
                categoryId = list[2]
            }

            AnalyticsLogger.getInstance().logScreenView(Screen.FW_CATEGORY, category)

            supportActionBar?.title = category
            binding.mediaFireworkFeedview.setChannel(categoryId)

            /*val bottomSheet = findViewById<FrameLayout>(R.isupportActionBard.media_firework_categoryview)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            Handler().postDelayed({
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }, 5000)*/

            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FW_CATEGORY, category)
            binding.mediaFireworkFeedview.addOnItemClickedListener(object : OnItemClickedListener {
                override fun onItemClicked(index: Int, title: String, id: String, videoDuration: Long) {
                    AnalyticsLogger.getInstance().logShortFormContentView()
                    SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER, "1")
                }
            })

        } catch (e: Exception) {
            Log.d("****","******************************CRASHED**************${e.localizedMessage}")
        }
    }

    override fun onResume() {
        super.onResume()
        SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER)?.let {
            if (it == "1") {
                AnalyticsLogger.getInstance().logShortFormContentPause()
                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER, "0")
            }
        }
    }
}