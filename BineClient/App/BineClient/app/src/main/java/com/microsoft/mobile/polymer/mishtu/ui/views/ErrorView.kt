// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewErrorBinding

class ErrorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                          defStyle: Int = 0): FrameLayout(context, attrs, defStyle) {

    companion object {
        const val INTERNET_CONNECTION = 0
        const val SERVER_ERROR = 1
    }

    private var type = INTERNET_CONNECTION
    private var listener: OnErrorViewRetryClickedListener? = null

    lateinit var binding: ViewErrorBinding

    init {
        initView()
    }

    private fun initView(){
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewErrorBinding.inflate(inflater, this, true)
        binding.btnRetry.setOnClickListener {
            if (type == INTERNET_CONNECTION) {
                listener?.onRefreshClicked()
            } else {
                listener?.onRetryClicked()
            }
        }
    }

    fun setErrorType(type: Int) {
        this.type = type
        if (type == INTERNET_CONNECTION) {
            binding.btnRetry.text = context.resources.getString(R.string.bn_refresh)
            binding.errorText.text = context.resources.getString(R.string.bn_no_internet_connection)
        } else if (type == SERVER_ERROR) {
            binding.errorText.text = context.resources.getString(R.string.bn_server_error)
            binding.btnRetry.text = context.resources.getString(R.string.bn_retry)
        }
    }

    fun setListener(listener: OnErrorViewRetryClickedListener?) {
        this.listener = listener
    }

    interface OnErrorViewRetryClickedListener {
        fun onRefreshClicked()
        fun onRetryClicked()
    }
}