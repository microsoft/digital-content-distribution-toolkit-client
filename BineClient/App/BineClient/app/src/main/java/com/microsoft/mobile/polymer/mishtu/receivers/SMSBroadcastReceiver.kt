package com.microsoft.mobile.polymer.mishtu.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SMSBroadcastReceiver: BroadcastReceiver() {
    private var otpReceiver: OTPReceiveListener? = null

    fun initOTPListener(receiver: OTPReceiveListener) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

            extras.get(SmsRetriever.EXTRA_SMS_MESSAGE).let {
                Log.e("SMSBroadcastReceiver",  it.toString())
            }

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    val otp: String = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    Log.e("SMSBroadcastReceiver", otp)
                    val p = Pattern.compile( "(\\d{6})" )
                    val m = p.matcher(otp)
                    if ( m.find() ) {
                        otpReceiver?.onOTPReceived(m.group())
                    }
                }

                CommonStatusCodes.TIMEOUT ->
                    otpReceiver?.onOTPTimeOut()
            }
        }
    }

    interface OTPReceiveListener {
        fun onOTPReceived(otp: String)
        fun onOTPTimeOut()
    }
}