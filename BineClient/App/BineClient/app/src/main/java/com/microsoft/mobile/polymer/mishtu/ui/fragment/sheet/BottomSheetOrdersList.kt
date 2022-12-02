// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetOrdersBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.PurchasedOrdersListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel

import com.msr.bine_sdk.cloud.models.Order
import com.msr.bine_sdk.models.Error
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetOrdersList : BottomSheetDialogFragment(),
        PurchasedOrdersListAdapter.OnOrderItemClickedListener {

    private lateinit var binding: BottomSheetOrdersBinding
    private lateinit var listener: OnBottomSheetOrdersItemClickListener
    private lateinit var adapter: PurchasedOrdersListAdapter
    private val viewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding =
                DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_orders, container, false)

        AnalyticsLogger.getInstance().logScreenView(Screen.ORDER_HISTORY)

        binding.close.setOnClickListener { dismiss() }

       /* binding.orderListSearchView.findViewById<EditText>(R.id.inputSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable) {}
        })*/

        observeData()
        viewModel.getAllOrders()
        return binding.root
    }

    fun setListener(listener: OnBottomSheetOrdersItemClickListener) {
        this.listener = listener
    }

    private fun observeData() {
        viewModel.loading.observe(this, {
            if (it) (requireActivity() as BaseActivity).showProgress()
            else (requireActivity() as BaseActivity).hideProgress()
        })

        viewModel.orders.observe(this, {
            if (it.isNotEmpty()) {
                adapter = PurchasedOrdersListAdapter(requireContext(),
                    this, it, it, false)
                binding.recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
            else {
                binding.orderListNoOrders.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
        })

        viewModel.error.observe(this, {
            binding.orderListNoOrders.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            when (it){
                Error.NETWORK_ERROR -> binding.orderListNoOrders.text = getString(R.string.bn_no_internet_subtitle)
                /*else -> binding.orderListNoOrders.text = getString(R.string.bn_server_error)*/
                Error.INVALID_PHONE -> {}
                Error.INVALID_OTP -> {}
                Error.EXCEED_ATTEMPT -> {}
                Error.UNKNOWN_ERROR -> {}
                Error.USER_REFERRED -> {}
                Error.INVALID_REFERRAL -> {}
                Error.API_ERROR -> {}
                Error.INIT_FAILURE -> {}
            }
        })
    }

    interface OnBottomSheetOrdersItemClickListener {
        fun onBottomSheetOrderItemClicked(order: Order)
    }

    override fun onOrderItemClicked(order: Order) {
        listener.onBottomSheetOrderItemClicked(order)
    }

    override fun orderTitleClicked(order: Order, subTitle: String?) {
        listener.onBottomSheetOrderItemClicked(order)
    }
}