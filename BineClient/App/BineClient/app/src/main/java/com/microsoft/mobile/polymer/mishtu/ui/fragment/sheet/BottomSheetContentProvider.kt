// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.marginBottom
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.loopnow.fireworklibrary.setMarginTop
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentContentProviderBinding
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ContentProviderAdapter
import com.microsoft.mobile.polymer.mishtu.ui.fragment.ContentFragment
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetContentProvider(
    val listner: ContentProviderAdapter.onItemClickListner) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentContentProviderBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private var contentProviderAdapter: ContentProviderAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_content_provider, container, false)
        contentProviderAdapter = ContentProviderAdapter(requireContext(), listner, this)
        binding.close.visibility = View.VISIBLE
        val mlp = binding.close.layoutParams as ViewGroup.MarginLayoutParams
        mlp.setMargins(0, 0, 0, requireContext().resources.getDimension(R.dimen.dp_20).toInt())
        binding.close.setOnClickListener {
            dismiss()
        }
        binding.fragmentHeader.visibility = View.GONE
        binding.titleBottomsheet.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = contentProviderAdapter
        binding.title.setMarginTop(requireContext().resources.getDimension(R.dimen.dp_30).toInt())
        contentViewModel.getAllContentProviders()
        observeData()
        return binding.root
    }

    private fun observeData() {
        contentViewModel.allContentProvidersLiveData.observe(viewLifecycleOwner, {
            Log.d("CPF2here",it.toString());
            contentProviderAdapter?.setData(it)
        })
    }



}