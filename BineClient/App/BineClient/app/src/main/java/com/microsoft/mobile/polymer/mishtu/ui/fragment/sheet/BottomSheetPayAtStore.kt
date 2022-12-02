// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetPayAtStoreBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetPayAtStore(val contentProviderId: String) : BottomSheetDialogFragment()  {
    lateinit var binding: BottomSheetPayAtStoreBinding
    private val viewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override  fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getActiveOrder()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_pay_at_store, container, false)

        binding.dialogClose.setOnClickListener {
            dismiss()
        }
        binding.btnPayAtStore.setOnClickListener {

            AppUtils.showNearbyStore(requireContext(), null, false, contentProviderId, null)
            dismiss()
        }
        binding.bttCancelPack.setOnClickListener {
            cancelOrder()
        }
        observeData()
        return binding.root
    }


    private fun cancelOrder() {
        val order: String? = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_ORDER_ID+"_$contentProviderId")
        order?.let { viewModel.cancelOrder(it, contentProviderId) }
    }

    private fun observeData() {
        viewModel.cancelOrder.observe(this,{
            dismiss()
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
        viewModel.error.observe(this,{
            dismiss()
            Toast.makeText(requireContext(),it.name, Toast.LENGTH_SHORT).show()
        })
        viewModel.currentOrder.observe(this,{
            if(it != null){
                binding.title.text = getString(R.string.pending_pack_payment,it.price)
            }
        })
        viewModel.loading.observe(this,{
            if(it){
                (requireActivity() as BaseActivity).showProgress()
            }
            else{
                (requireActivity() as BaseActivity).hideProgress()
            }
        })
    }

    private fun getActiveOrder(){
        var orderId = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_ORDER_ID+"_$contentProviderId")
        orderId = orderId ?: ""
        viewModel.getActiveorder(orderId)
    }
}