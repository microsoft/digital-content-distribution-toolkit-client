package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentScratchCardsBinding
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ScratchCardsAdapter
import com.msr.bine_sdk.cloud.models.IncentiveEvent

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScratchCardsFragments : Fragment(), ScratchCardsAdapter.ItemClickListener {

    lateinit var binding: FragmentScratchCardsBinding
    lateinit var adapter: ScratchCardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_scratch_cards, container, false
        )
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rewardsRecyclerView.layoutManager = mLayoutManager

        adapter = ScratchCardsAdapter(requireContext(), this)
        binding.rewardsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onCardClicked(event: IncentiveEvent, position: Int) {
        showDailyRewardsDialog()
    }

    private fun showDailyRewardsDialog() {
        //RewardsDialog().newInstance().show(childFragmentManager, null)
    }

}