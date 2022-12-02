// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetAboutUsBinding
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetAboutUs: BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_about_us, container,false)
        binding.version.text = getString(R.string.bn_version, getAppVersion())

        binding.termOfUse.setOnClickListener {
           AppUtils.openTermsOfUse(requireContext())
        }

        binding.privacyPolicy.setOnClickListener {
            AppUtils.openPrivacyPolicy(requireContext())
        }

        binding.appAvailability.setOnClickListener {
            Toast.makeText(requireContext(), "This section would be added shortly..", Toast.LENGTH_SHORT).show()
        }
        binding.close.setOnClickListener{
            dismiss()
        }

        return binding.root
    }

    private fun getAppVersion(): String{
        var version = ""
        try {
            val pInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
             version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }
}