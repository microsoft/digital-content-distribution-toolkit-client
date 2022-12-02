// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetHelpSupportBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen

import com.microsoft.mobile.polymer.mishtu.ui.adapter.BottomSheetHelpSupportAdapter

class BottomSheetHelpSupport : BottomSheetDialogFragment(),
        BottomSheetHelpSupportAdapter.OnOrderItemClickedListener {

    private lateinit var binding: BottomSheetHelpSupportBinding
    private lateinit var listener: OnBottomSheetOrdersItemClickListener
    private lateinit var adapter: BottomSheetHelpSupportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
                DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_help_support, container, false)

        AnalyticsLogger.getInstance().logScreenView(Screen.HELP_SUPPORT)

        val queries = resources.getStringArray(R.array.help_support).toCollection(ArrayList())
        activity?.let {
            adapter = BottomSheetHelpSupportAdapter(queries, queries)
            binding.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        binding.close.setOnClickListener { dismiss() }
        binding.searchMic.setOnClickListener { dismiss() }

        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int, count: Int
            ) {

                if (s.isNotEmpty() && s.length >= 3) {
                    adapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        return binding.root
    }

    fun setListener(listener: OnBottomSheetOrdersItemClickListener) {
        this.listener = listener
    }


    interface OnBottomSheetOrdersItemClickListener {
        fun onBottomSheetOrderItemClicked(position: Int)
    }

    override fun onOrderItemClicked(position: Int) {
        listener.onBottomSheetOrderItemClicked(position)
    }
}