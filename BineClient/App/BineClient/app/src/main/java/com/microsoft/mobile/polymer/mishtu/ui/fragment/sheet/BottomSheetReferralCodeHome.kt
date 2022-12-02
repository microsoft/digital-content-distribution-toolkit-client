// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetReferralCodeHomeBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.utils.OnDismissCallback

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BottomSheetReferralCodeHome(private var referralCode: String,
                                  private var onDismissCallback: OnDismissCallback?) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetReferralCodeHomeBinding

    private val codePrefix = "RN-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_referral_code_home, container, false)

        binding.inputSearch.setText(referralCode)
        binding.searchMic.setOnClickListener { /*dismiss()*/ }

        binding.close.setOnClickListener { dismiss() }

        binding.btnValidateReferral.setOnClickListener {
            val referralCode = binding.inputSearch.text.toString()
            if (TextUtils.isEmpty(referralCode)) {
                Toast.makeText(context, context?.resources?.getString(R.string.bn_enter_referral_code), Toast.LENGTH_SHORT).show()
            } else {
                verifyReferralCode(referralCode)
            }
            //dismiss()
        }

        binding.btnSkip.setOnClickListener {
            dismiss()
        }

        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }
        return binding.root
    }

    private fun verifyReferralCode(referralCode: String) {
        (requireActivity() as? BaseActivity)?.showProgress()
        CoroutineScope(Dispatchers.Default).launch {
           val response= BineAPI.UMS().assignRetailer("$codePrefix$referralCode")
            (requireActivity() as? BaseActivity)?.hideProgress()
            if(response.result != null){
                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_REFERRAL_CODE, "$codePrefix$referralCode")
                requireActivity().runOnUiThread {
                    Toast.makeText(activity, getString(R.string.referral_code_saved), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }else{
                response.details?.let {
                    val first= it.indexOf("=[")
                    val end= it.indexOf("]")

                    var msg= ""
                    if (first > -1 && end > -1) {
                        msg = it.substring(first + 2, end)
                        if (msg.indexOf("USR_ERR_004") > -1 && msg.indexOf(codePrefix) > -1) {
                            val codeStart = msg.indexOf(codePrefix)
                            val responseReferralCode = msg.substring(codeStart, codeStart + 6)
                            SharedPreferenceStore.getInstance()
                                .save(SharedPreferenceStore.KEY_REFERRAL_CODE, responseReferralCode)
                            requireActivity().runOnUiThread {
                                Toast.makeText(activity, getString(R.string.referral_code_saved_already), Toast.LENGTH_SHORT).show()
                            }
                            dismiss()
                            return@launch
                        }
                        if (msg.indexOf("USR_ERR_005") > -1) {
                            msg = getString(R.string.invalid_referral_code)
                        }
                    }

                    if (msg.isEmpty()) {
                        response.error?.let { error ->
                            msg = when (error) {
                                Error.NETWORK_ERROR-> getString(R.string.bn_no_internet_connection)
                                else -> {
                                    getString(R.string.bn_server_error)
                                }
                            }
                        }
                    }

                    if(!TextUtils.isEmpty(msg)){
                        requireActivity().runOnUiThread {
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissCallback?.onDismiss()
        super.onDismiss(dialog)
    }
}