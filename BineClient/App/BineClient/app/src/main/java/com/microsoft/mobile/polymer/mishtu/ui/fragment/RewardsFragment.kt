package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentRewardsBinding
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ViewPagerAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.IncentiveViewModel

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardsFragment : Fragment() {

    lateinit var binding: FragmentRewardsBinding
    private lateinit var offersFragment: OffersFragment

    val viewModel by viewModels<IncentiveViewModel>()

    private val mFragmentTitleList: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRewardsBinding.inflate(inflater)

        offersFragment = OffersFragment()
        setupViewPager(binding.viewPager)

        binding.rewardsCoins.text = viewModel.cachedCoins.toString()

        setupTabLayout()
        observeData()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getTotalCoins()
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.isUserInputEnabled = false

        mFragmentTitleList.add(resources.getString(R.string.rewards_offers))
        adapter.addFragment(offersFragment)
        viewPager.adapter = adapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {
                tab, position -> tab.text = mFragmentTitleList[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    binding.viewPager.currentItem = it
                }
                val text = tab?.customView as TextView?
                text?.setTypeface(null, Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val text = tab?.customView as TextView?
                text?.setTypeface(null, Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        for (i in 0 until binding.tabLayout.tabCount) {
            val tab: TabLayout.Tab? = binding.tabLayout.getTabAt(i)
            val tabTextView = TextView(requireContext())
            tab?.customView = tabTextView
            tabTextView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            tabTextView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            tabTextView.text = tab?.text
            tabTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text_color))

            if (i == 0) {
                tabTextView.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun observeData() {
        viewModel.totalCoins.observe(viewLifecycleOwner, {
            binding.rewardsCoins.text = it.toString()
            offersFragment.setTotalCoins(it)
        })
    }

    fun availableCoinsView(): View {
        return binding.rewardsCoins
    }
}