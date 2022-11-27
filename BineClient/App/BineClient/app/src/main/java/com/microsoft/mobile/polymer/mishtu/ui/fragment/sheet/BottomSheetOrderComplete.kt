package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetOrderCompleteBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.OrderViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetOrderComplete(val subscriptionId: String?) :
    BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetOrderCompleteBinding
    val contentViewModel by viewModels<ContentViewModel>()
    val orderViewModel by viewModels<OrderViewModel>()
    var activeSubscription: ActiveSubscription? = null
    lateinit var subId: String

    private var watchFilmsClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.bottom_sheet_order_complete,
            container,
            false)
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.watchFilmsAndSerialsButton.setOnClickListener {
            watchFilmsClicked = true
            dismiss()
        }
        //gettin the cpid from shared pref which we store while showing the notification,
        if (!subscriptionId.isNullOrEmpty()) {
            subId = subscriptionId
        } else {
            subId = SharedPreferenceStore.getInstance()
                .get(SharedPreferenceStore.NOTIFICATION_ORDER_COMPLETE_SUBSCRIPTIONID).toString()
            SharedPreferenceStore.getInstance()
                .save(SharedPreferenceStore.NOTIFICATION_ORDER_COMPLETE_SUBSCRIPTIONID, "")
        }
        if (!subId.isNullOrEmpty()) {
            observeData(subId)
        }
        return binding.root
    }

    private fun observeData(subscriptionId: String) {
        contentViewModel.getActiveSubscriptionForSubscriptionId(subscriptionId)
            .observe(viewLifecycleOwner, {
                it?.let {
                    setBottomSheetText(it)
                    activeSubscription = it
                }
            })


    }

    private fun setBottomSheetText(activeSub: ActiveSubscription) {
        AppUtils.loadImage(requireContext(),
                AppUtils.getContentProviderSquareLogoURL(activeSub.providerId),
                binding.packLogo)
        val string = getString(R.string.order_complete_pack_details,
            activeSub.subscription.price,
            activeSub.subscription.durationDays)
        val boldText1 =
            getString(R.string.order_complete_pack_price, activeSub.subscription.price)
        val boldText2 = getString(R.string.order_complete_pack_duration,
            activeSub.subscription.durationDays)

        binding.packDetail.text =
            AppUtils.getBoldTextSpannable(string, arrayOf(boldText1, boldText2))
    }

    override fun onDismiss(dialog: DialogInterface) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        if (watchFilmsClicked) {
            intent.putExtra(MainActivity.extraShiftTab, 1)
        }
        intent.putExtra(MainActivity.extraOrderCompleteEventAmount,
            activeSubscription?.subscription?.price ?: 0)
        intent.putExtra(MainActivity.extraOrderCompleteEventContentProvider,
            activeSubscription?.subscription?.contentProviderId)
        startActivity(intent)

        super.onDismiss(dialog)
    }
}