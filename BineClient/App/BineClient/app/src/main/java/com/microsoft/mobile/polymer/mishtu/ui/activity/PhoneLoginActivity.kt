// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityLoginBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.LoginViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ProfileViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.OtpView
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.VoiceSearchUtil
import java.util.*


class PhoneLoginActivity : BaseActivity(), VoiceSearchUtil.VoiceSearchCallBack {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding:ActivityLoginBinding
    private val profileViewModel by viewModels<ProfileViewModel>()
    private lateinit var contract: ActivityResultLauncher<Intent>

    companion object {
        const val WAS_FORCED_LOGOUT = "ForcedLogout"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val wasForcedLogin = intent.getBooleanExtra(WAS_FORCED_LOGOUT, false)
        if (wasForcedLogin) profileViewModel.logout(this)
        setupUI()
    }

    private fun setupUI() {
        setTermsConditionLabel()

        binding.btnLogin.setOnClickListener {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.inputMobile.windowToken, 0)
            if (binding.cbTermsAndConditions.isChecked) {
                val mobile = binding.inputMobile.text.toString().trim()
                if (TextUtils.isEmpty(mobile) || !OtpView.isValidPhoneNumber(mobile)) {
                    Toast.makeText(this, resources.getString(R.string.lbl_error_valid_mobile), Toast.LENGTH_SHORT).show()
                } else {
                    doLogin("+91$mobile")
                }
            } else {
                Toast.makeText(this@PhoneLoginActivity, resources.getString(R.string.bn_terms_conditions_error), Toast.LENGTH_SHORT).show()
            }
        }

        binding.cbTermsAndConditions.setOnClickListener {
            binding.btnLogin.isEnabled = binding.cbTermsAndConditions.isChecked
        }

        viewModel.getVerifyMobileData().observe(this, {
            hideProgress()
            it?.let {
                if (it) {
                    startActivity(Intent(this@PhoneLoginActivity, ValidatePinActivity::class.java)
                            .putExtra("mobile", "+91${binding.inputMobile.text.toString().trim()}"))
                    finish()
                }
            }
        })

        viewModel.errorResponseLiveData.observe(this, {
            hideProgress()
            it?.let{
                if(!TextUtils.isEmpty(it)){
                    Toast.makeText(this, it,Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.inputMobile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int) {
                if (s.isNotEmpty() && s.length == 10) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.inputMobile.windowToken, 0)
                }
            }
        })
        contract = VoiceSearchUtil.getNewInstance().getContract(this , this)

        binding.btnMic.setOnClickListener {
            VoiceSearchUtil.getNewInstance().voiceInput(this ,contract)
        }

        AnalyticsLogger.getInstance().logScreenView(Screen.LOGIN_PHONE)
    }


    private fun doLogin(mobile: String) {
        showProgress()
        viewModel.verifyPhone(mobile)
    }

    private fun setTermsConditionLabel() {
        val tnc = SpannableString(resources.getString(R.string.bn_terms_conditions))
        val clickSpanTNC: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(this@PhoneLoginActivity, R.color.bn_terms_condition)
                ds.isUnderlineText = true
            }

            override fun onClick(textView: View) {
                AppUtils.openTermsOfUse(this@PhoneLoginActivity)
            }
        }
        tnc.setSpan(clickSpanTNC, 0, tnc.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacy = SpannableString(resources.getString(R.string.bn_privacy_policy))
        val clickSpanPrivacy: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(this@PhoneLoginActivity, R.color.bn_terms_condition)
                ds.isUnderlineText = true
            }

            override fun onClick(textView: View) {
               AppUtils.openPrivacyPolicy(this@PhoneLoginActivity)
            }
        }
        privacy.setSpan(clickSpanPrivacy, 0, privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textTermsAndConditions.movementMethod = LinkMovementMethod()
        binding.textTermsAndConditions.text = TextUtils.expandTemplate(resources.getString(R.string.bn_accept_the), tnc, privacy)
    }

    override fun onSerachQueryDetected(res: String) {
        Log.d("VoiceInput", res)
        var num = res.replace(" ", "")
        num = num.replace("[^0-9]".toRegex(), "")
        if (!num.isEmpty() && num.isDigitsOnly()) {
            binding.inputMobile.setText(num)
        } else {
            Toast.makeText(this, getString(R.string.lbl_error_valid_mobile), Toast.LENGTH_SHORT).show()
        }
    }
}
