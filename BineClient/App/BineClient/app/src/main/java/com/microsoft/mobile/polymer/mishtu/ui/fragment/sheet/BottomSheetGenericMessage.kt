// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetGenericMessageBinding
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.OnDismissCallback

class BottomSheetGenericMessage(
    private val titleIcon: Int?,
    private val titleMsg: String?,
    private val message: String?,
    private val closeButton: Boolean?,
    private val positiveButtonTitle: String?,
    private val negativeButtonTitle: String?,
    private val positiveButtonClickListener: View.OnClickListener?,
    private val negativeButtonClickListener: View.OnClickListener?,
    private val additionalParams: Map<String,Any>?
) : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetGenericMessageBinding
    private var onDismissCallback: OnDismissCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater,
                R.layout.bottom_sheet_generic_message,
                container,
                false)

        titleIcon?.let {
            binding.imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                titleIcon))
            additionalParams?.let { map ->
                if (map.containsKey("icon_padding")) {
                    val padding = map["icon_padding"] as Int
                    binding.imageView.setPadding(padding, padding, padding, padding)
                }
            }
        }

        message?.let {
            binding.message.text = it
        } ?: run {
            binding.message.visibility = View.GONE
        }

        positiveButtonTitle?.let {
            binding.positiveButton.text = it
        }

        titleMsg?.let {
            binding.titleMsg.text = it
        } ?: run {
            binding.titleMsg.layoutParams.height =  resources.getDimensionPixelSize(R.dimen.dp_2)
        }

        negativeButtonTitle?.let {
            binding.negativeButton.visibility = View.VISIBLE
            binding.negativeButton.text = it

            binding.negativeButton.setOnClickListener { view ->
                negativeButtonClickListener?.onClick(view)
                dismiss()
            }
        } ?: run {
            binding.negativeButton.visibility = View.GONE
        }

        binding.dialogClose.setOnClickListener {
            dismiss()
        }
        binding.positiveButton.setOnClickListener {
            positiveButtonClickListener?.onClick(it)
            dismiss()
        }
        if (closeButton == null || closeButton == false) {
            binding.dialogClose.visibility = View.GONE
        }

        additionalParams?.let { map ->
            if (map.containsKey("bold_text")) {
                val boldText = map["bold_text"] as String
                binding.message.text = AppUtils.getBoldTextSpannable(getString(R.string.location_scan_denied),
                    arrayOf(boldText))
            }
        }
        return binding.root
    }

    fun setOnDismissListener(listener: OnDismissCallback) {
        this.onDismissCallback = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        this.onDismissCallback?.onDismiss()
        super.onDismiss(dialog)
    }
}