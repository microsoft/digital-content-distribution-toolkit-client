// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetCancelOrderBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Order
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetCancelOrderDialog(val order: Order?, val contentProviderId: String) : BottomSheetDialogFragment() {

    lateinit var binding : BottomSheetCancelOrderBinding
    private val viewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_cancel_order, container, false)
        Log.d("order", this.order.toString())
        binding.title.text = getString(R.string.cancel_order_confirm, this?.order?.price)

        binding.dialogClose.setOnClickListener { dismiss() }

        binding.btnYes.setOnClickListener {
            if(this?.order?.id != null)
            viewModel.cancelOrder(this.order.id, contentProviderId)
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
        observeData()
        return binding.root

    }

    private fun observeData() {
        viewModel.cancelOrder.observe(this,{
            dismiss()
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
        viewModel.error.observe(this,{
            dismiss()
            Toast.makeText(requireContext(),it.name,Toast.LENGTH_SHORT).show()
        })

    }
}