package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetLanguagesBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen

import com.microsoft.mobile.polymer.mishtu.ui.adapter.BottomSheetLanguagesListAdapter
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.VoiceSearchUtil

class BottomSheetLanguageList : BottomSheetDialogFragment(),
    BottomSheetLanguagesListAdapter.OnLanguageItemClickListener,
    VoiceSearchUtil.VoiceSearchCallBack {
    private lateinit var binding: BottomSheetLanguagesBinding
    private lateinit var listener: OnBottomSheetLanguageItemClickListener
    private lateinit var adapter: BottomSheetLanguagesListAdapter
    private lateinit var contract: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_languages, container, false)

        AnalyticsLogger.getInstance().logScreenView(Screen.PROFILE_LANGUAGE)

        val languageList=ArrayList<String>()
        languageList.addAll(BNConstants.Languages.asList().subList(0, 3))

        activity?.let {
            adapter = BottomSheetLanguagesListAdapter(it, this, BNConstants.LanguagesCode[0], languageList,languageList)
            binding.recyclerView.adapter = adapter

            adapter.setSelectedLang(AppUtils.getLanguageToShow())

        }
        binding.close.setOnClickListener { dismiss() }
        binding.searchMic.setOnClickListener {
            VoiceSearchUtil.getNewInstance().voiceInput(requireContext() ,contract)
        }

        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }
        contract = VoiceSearchUtil.getNewInstance().getContract(this , this)

        return binding.root
    }

    fun setListener(listener: OnBottomSheetLanguageItemClickListener) {
        this.listener = listener
    }

    override fun onLanguageClicked(code: String) {
        listener.onBottomSheetLanguageClicked(code)
        dismiss()
    }

    interface OnBottomSheetLanguageItemClickListener {
        fun onBottomSheetLanguageClicked(code: String)
    }

    override fun onSerachQueryDetected(res: String) {
        binding.inputSearch.setText(res)
    }
}