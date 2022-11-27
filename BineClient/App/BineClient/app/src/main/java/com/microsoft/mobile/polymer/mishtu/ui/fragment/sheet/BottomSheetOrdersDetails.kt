package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetOrderDetailsBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

import com.msr.bine_sdk.cloud.models.Order
import java.text.SimpleDateFormat
import java.util.*


class BottomSheetOrdersDetails : BottomSheetDialogFragment(){

    private lateinit var binding: BottomSheetOrderDetailsBinding
    private var currentOrder: Order? = null
    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_order_details, container, false)

        AnalyticsLogger.getInstance().logScreenView(Screen.ORDER_DETAILS)

        binding.close.setOnClickListener { dismiss() }

        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
            }
        }

        currentOrder?.let {
            binding.orderId.text = it.id
            binding.title.text = it.orderItems[0].subscription.title
            val date = TimestampUtils.getDateFromUTCString(it.orderCreatedDate)
            val dateString = SimpleDateFormat(BNConstants.DATE_FORMAT_dd_MM_YY, Locale.getDefault()).format(date)
            binding.orderDate.text = dateString
            binding.price.text = String.format(requireContext().getString(R.string.bn_pack_rs), it.orderItems[0].subscription.price)

            binding.orderListenToIt.setOnClickListener { _ ->
                textToSpeech?.speak(String.format(requireContext().getString(R.string.bn_order_listen), dateString,
                        it.orderItems[0].subscription.title, it.orderItems[0].subscription.price),
                        TextToSpeech.QUEUE_FLUSH, null, null)
            }

            it.orderItems[0].partnerReferenceNumber?.let { partner ->
                binding.retailerName.text = partner
            }
        }

        return binding.root
    }

    fun setOrder(order: Order) {
        this.currentOrder = order
    }
}