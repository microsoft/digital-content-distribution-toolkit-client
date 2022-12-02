// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.base

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.msr.bine_android.BuildConfig

open class BaseFragment: Fragment() {
//    private var progressDialog: FullscreenProgressDialog? = null
    private var mContext: Context? = null
    var mActivity: BaseActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is BaseActivity) {
            mActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG && activity == null) {
            error("Assertion failed")
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
//        progressDialog = FullscreenProgressDialog(mContext)
        setHasOptionsMenu(false)
    }

    fun showProgressDialog() {
       /* if (progressDialog != null) {
            progressDialog!!.show()
        }*/
    }

    fun dismissProgressDialog() {
        /*if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }*/
    }
}