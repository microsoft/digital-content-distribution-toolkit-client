// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.content.Context
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient

import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.LoginViewModel
import com.microsoft.mobile.polymer.mishtu.receivers.SMSBroadcastReceiver
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ActivityOtpValidateBinding
import com.microsoft.mobile.polymer.mishtu.listeners.OnOtpCompletionListener
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

class ValidatePinActivity : BaseActivity(), SMSBroadcastReceiver.OTPReceiveListener {
    private lateinit var binding: ActivityOtpValidateBinding
    private var otpInput = ""
    private var mobile = ""
    private val smsBroadcast = SMSBroadcastReceiver()
    private var otpReceiver: SMSBroadcastReceiver.OTPReceiveListener = this
    lateinit var client: SmsRetrieverClient
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_validate)

        mobile = intent.getStringExtra("mobile").toString()

        if (!TextUtils.isEmpty(mobile)) {
            val mobileSpan = SpannableString(mobile)
            mobileSpan.setSpan(StyleSpan(Typeface.BOLD),
                    0, mobileSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.subTitle.text = TextUtils.concat(resources.getString(R.string.bn_otp_has_been_sent), mobileSpan)
        }

        setUpResendOtpText()
        registerSmsRetriever()

        binding.inputOtp.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String) {
                otpInput = otp
                binding.btnVerifyPin.isEnabled = true && binding.cbAgeConfirm.isChecked
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.inputOtp.windowToken, 0)
            }

            override fun onOtpRemoving(otp: String) {
                otpInput = otp
                binding.btnVerifyPin.isEnabled = false && binding.cbAgeConfirm.isChecked
            }
        })

        binding.cbAgeConfirm.setOnClickListener {
            binding.btnVerifyPin.isEnabled = binding.cbAgeConfirm.isChecked && otpInput.length == 6
        }

        binding.btnVerifyPin.setOnClickListener {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.btnVerifyPin.windowToken, 0)
            if(binding.cbAgeConfirm.isChecked) {
                doVerifyPin(otpInput, mobile)
            }else{
                Toast.makeText(this, resources.getString(R.string.bn_confirm_age_error), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getVerifyMobileData().observe(this, {
            hideProgress()
            it?.let {
                binding.resendOtp.visibility = View.GONE
                showTimer()
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

        viewModel.getVerifyOtpData().observe(this, {
            hideProgress()
            AnalyticsLogger.getInstance().logEvent(Event.LOGIN_APP)
            AnalyticsLogger.getInstance().logLanguageSelected()
            startActivity(AppUtils.getNextIntent(this))
            finish()
        })

        viewModel.accountDeleteProgress.observe(this, {
            hideProgress()
            binding.userDeleteLayout.visibility = View.VISIBLE
            binding.userDeleteButton.setOnClickListener { finish() }
        })

        AnalyticsLogger.getInstance().logScreenView(Screen.LOGIN_OTP)
    }

    private fun showTimer() {
        binding.timer.visibility = View.VISIBLE
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds=millisUntilFinished/1000
                val remainingTime: String = if(seconds<10){
                    "Resend OTP in 00:0$seconds"
                }else{
                    "Resend OTP in 00:$seconds"
                }
                binding.timer.text = remainingTime
            }

            override fun onFinish() {
                binding.timer.visibility=View.GONE
                binding.resendOtp.visibility = View.VISIBLE

            }
        }.start()
    }

    private fun setUpResendOtpText() {
        val resendOTp = SpannableString(resources.getString(R.string.bn_resend_otp))
        val clickSpanPrivacy: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(this@ValidatePinActivity, R.color.bn_terms_condition)
                ds.isUnderlineText = true
            }

            override fun onClick(textView: View) {
                doLogin(mobile)
            }
        }
        resendOTp.setSpan(clickSpanPrivacy, 0, resendOTp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.resendOtp.movementMethod = LinkMovementMethod()
        binding.resendOtp.text =
                TextUtils.concat(resources.getString(R.string.bn_didnt_receive_otp), resendOTp)
    }

    private fun doVerifyPin(otpInput: String,mobile: String) {
        showProgress()
        viewModel.verifyOtp(otpInput,mobile)
    }

    private fun doLogin(mobile: String) {
        showProgress()
        viewModel.verifyPhone(mobile)
    }

    override fun onOTPReceived(otp: String) {
        if(!TextUtils.isEmpty(otp)){
           binding.inputOtp.setText(otp)
        }
    }

    override fun onOTPTimeOut() {

    }

    private fun registerSmsRetriever() {
        smsBroadcast.initOTPListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcast, intentFilter)

        client = SmsRetriever.getClient(this)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {}
        task.addOnFailureListener {}
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(smsBroadcast)
    }
}
