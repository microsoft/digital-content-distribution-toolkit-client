// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.GONE
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.loopnow.fireworklibrary.FwSDK
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentMediaLandingBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.SearchActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.SubscriptionPackListActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ViewPagerAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.NotificationViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class MediaLandingFragment : Fragment() {

    private lateinit var binding: FragmentMediaLandingBinding
    private val mFragmentTitleList: MutableList<String> = ArrayList()

    //private lateinit var allFragment: ContentFragment
    private lateinit var clipsFragment: ContentFragment

    private lateinit var moviesFragment: ContentFragment
    private lateinit var seriesFragment: ContentFragment

    private lateinit var contentProviderForMovieFragment: ContentProviderFragment
    private lateinit var contentProviderForSeriesFragment: ContentProviderFragment
    private var selectedContentProviderOfFilms: String? = null
    private var selectedContentProviderOfSeries: String? = null
    private val notificationViewModel by activityViewModels<NotificationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = androidx.databinding.DataBindingUtil.inflate(
            inflater, R.layout.fragment_media_landing, container, false)

        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE

        //allFragment = ContentFragment.newInstance(ContentFragment.TYPE.ALL)
        clipsFragment =
            ContentFragment.newInstance(ContentFragment.TYPE.CLIPS, "")

        selectedContentProviderOfFilms = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+ContentFragment.TYPE.MOVIES.name)
        selectedContentProviderOfSeries = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+ContentFragment.TYPE.SERIES.name)
        if(!selectedContentProviderOfFilms.isNullOrEmpty()) {
            moviesFragment = ContentFragment.newInstance(ContentFragment.TYPE.MOVIES,
                selectedContentProviderOfFilms!!)
        }else {
            contentProviderForMovieFragment = ContentProviderFragment(ContentFragment.TYPE.MOVIES)
        }
        if(!selectedContentProviderOfSeries.isNullOrEmpty()) {
            seriesFragment = ContentFragment.newInstance(ContentFragment.TYPE.SERIES,
                selectedContentProviderOfSeries!!)
        }else {
            contentProviderForSeriesFragment = ContentProviderFragment(ContentFragment.TYPE.SERIES)
        }
        setupViewPager(binding.viewPager)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = mFragmentTitleList[position]
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 60, 0)
            tab.requestLayout()
        }

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { it ->
                    binding.viewPager.currentItem = it
                    when (it) {
                        0 -> AnalyticsLogger.getInstance().logScreenView(Screen.MEDIA_CLIPS)
                        1 -> {
                            AnalyticsLogger.getInstance().logScreenView(Screen.MEDIA_MOVIES)

                            animateDownArrow()
                            (requireActivity() as? MainActivity)?.showDownloadsFTU()
                            notificationViewModel.clearNotification(NotificationBadgeHelper.BadgeType.NEW_CONTENT)
                            val movieContentProvider = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+ContentFragment.TYPE.MOVIES.name)
                            if(!movieContentProvider.isNullOrEmpty())
                                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER +"_${SubscriptionPackListActivity.SUBSCRIPTION}" , movieContentProvider)
                            tab.removeBadge()
                        }
                        2 -> {
                            val seriesContentProvider = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER+ContentFragment.TYPE.SERIES.name)
                            if(!seriesContentProvider.isNullOrEmpty())
                                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER +"_${SubscriptionPackListActivity.SUBSCRIPTION}" , seriesContentProvider)
                        }
                        else -> {}
                    }
                }
                tab?.let {
                    AppUtils.setStyleForTab(requireContext(), tab, Typeface.BOLD)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    AppUtils.setStyleForTab(requireContext(), tab, Typeface.NORMAL)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        for (tabIndex in 0..binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(tabIndex)?.let {
                if (tabIndex == 0) AppUtils.setStyleForTab(requireContext(), it, Typeface.BOLD)
                else AppUtils.setStyleForTab(requireContext(), it, Typeface.NORMAL)
            }
        }

        binding.searchContainer.setOnClickListener {
            activity?.startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        binding.contentDownButton.setOnClickListener {
            binding.contentDownButton.visibility = View.GONE
            val position = binding.viewPager.currentItem
            if (position == 0) {
                clipsFragment.scrollDown()
            }
            if (position == 1) {
                //moviesFragment.scrollDown()
            }
        }

        //setHasOptionsMenu(true)
        setUpFireworkPlayListeners()

        /*binding.discoverNotificationMenu.setOnClickListener {
            NotificationDialogFragment().newInstance(
                NotificationActionHandler(requireActivity(), childFragmentManager)
            ).show(childFragmentManager, null)
        }*/

        //notificationViewModel.getUnreadNotifications()
        return binding.root
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        activity?.let {
            val adapter = ViewPagerAdapter(it)
            // mFragmentTitleList.add(resources.getString(R.string.bn_all))
            mFragmentTitleList.add(resources.getString(R.string.bn_clips))
            mFragmentTitleList.add(resources.getString(R.string.bn_movies))
            mFragmentTitleList.add(resources.getString(R.string.bn_series))
            viewPager.isUserInputEnabled = false
            binding.viewPager.offscreenPageLimit = 2
            //adapter.addFragment(allFragment)
            adapter.addFragment(clipsFragment)
            //adapter.addFragment(moviesFragment)
            if(!selectedContentProviderOfFilms.isNullOrEmpty()){
                adapter.addFragment(moviesFragment)
            }else{
                adapter.addFragment(contentProviderForMovieFragment)
            }
            if(!selectedContentProviderOfSeries.isNullOrEmpty()){
                adapter.addFragment(seriesFragment)
            }else{
                adapter.addFragment(contentProviderForSeriesFragment)
            }


            //adapter.addFragment(servicesFragment)
            viewPager.adapter = adapter
        }
    }

    private fun animateDownArrow() {
        binding.contentDownButton.visibility = View.VISIBLE
        val animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(),
            R.anim.home_down_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.contentDownButton.visibility = GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.contentDownButton.startAnimation(animation)
    }

    private fun setUpFireworkPlayListeners() {
        FwSDK.addVideoEventListener(object : FwSDK.VideoEventListener {
            override fun event(event: String, jsonObject: JSONObject) {
                Log.d("VideoEventListener", event)
                if (event == "video-complete") {
                    val videoId = jsonObject.get("video_id") as? String
                    videoId?.let {
                        val params = HashMap<String, String>()
                        params["ContentId"] = it
                        params["Title"] = (jsonObject.get("caption") as? String) ?: ""
                        params["ProviderId"] = "Firework"
                        val genre = SharedPreferenceStore.getInstance()
                            .get(SharedPreferenceStore.KEY_FW_CATEGORY)
                        params["Genre"] = if (genre.isNullOrEmpty()) "Default" else genre
                        params["ContentType"] = AnalyticsLogger.ShortForm
                        AnalyticsLogger.getInstance().logEvent(Event.CONTENT_VIEW, params)
                    }
                }
                if (event == "video-share") {
                    AppUtils.shareContent((jsonObject.get("caption") as? String) ?: "",
                        "Firework",
                        (jsonObject.get("video_id") as? String) ?: "",
                        AnalyticsLogger.ShortForm,
                        requireContext())
                }
            }
        })
    }

    internal fun showBadge(count: Int) {
        if (binding.tabLayout.selectedTabPosition == 1) return

        binding.tabLayout.getTabAt(1)?.let {
            it.orCreateBadge.backgroundColor =
                ContextCompat.getColor(requireActivity(), R.color.pack_expired_color)
            it.orCreateBadge.badgeTextColor =
                ContextCompat.getColor(requireActivity(), R.color.white)
            it.orCreateBadge.number = count
        }
    }

    fun getTabLayout(): TabLayout {
        return binding.tabLayout
    }

    fun getActiveOrderCardCTA(): View? {
        return clipsFragment.getActiveOrderCardCTA()
    }

    fun buyPackButton(): Button? {
        return clipsFragment.buyPackButton()
    }

    //To Pausse the autoPlaying trailler
    override fun onPause() {
        if (this::contentProviderForMovieFragment.isInitialized) {
            contentProviderForMovieFragment.onFragmentChanged()
        } else {
            moviesFragment.onFragmentChanged()
        }
        if (this::contentProviderForSeriesFragment.isInitialized) {
            contentProviderForSeriesFragment.onFragmentChanged()
        } else {
            seriesFragment.onFragmentChanged()
        }
        super.onPause()
    }
}