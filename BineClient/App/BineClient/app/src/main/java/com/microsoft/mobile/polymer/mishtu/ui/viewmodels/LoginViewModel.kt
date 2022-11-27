package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.mobile.auth.AuthManager
import com.microsoft.mobile.auth.dtos.AuthError
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.DeviceUtil
import com.microsoft.mobile.polymer.mishtu.services.FirebaseMessageReceiverService
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.IncentivesRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.Error

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(@ApplicationContext val context: Context, private val incentivesRepository: IncentivesRepository) : ViewModel() {

    private var verifyMobileLiveData = MutableLiveData<Boolean>()
    private var verifyOtpLiveData = MutableLiveData<Boolean?>()
    var accountDeleteProgress = MutableLiveData<Boolean?>()
    var errorResponseLiveData = MutableLiveData<String?>()
    private var token: String? = null
    private var userId: String? = null

    fun getVerifyMobileData(): MutableLiveData<Boolean> {
        return verifyMobileLiveData
    }

    fun getVerifyOtpData(): MutableLiveData<Boolean?> {
        return verifyOtpLiveData
    }

    fun verifyPhone(mobile: String) {
        token = null
        userId = null
        viewModelScope.launch {
            val response = AuthManager.getInstance().validatePhone(mobile, false, DeviceUtil.getDeviceId(context))
            response.accountStatus?.let {
                if (it == "OtpNeeded") {
                    if (BuildConfig.DEBUG) Toast.makeText(context, response.details, Toast.LENGTH_SHORT).show()
                    verifyMobileLiveData.postValue(true)
                }
            } ?:
            response.error?.let {
                AnalyticsLogger.getInstance().logGenericLogs(Event.LOGIN_ERROR,"ValidatePhone", response.error.toString(), response.details)
                if (it == AuthError.NETWORK_ERROR) errorResponseLiveData.postValue(context.getString(
                    R.string.bn_no_internet_subtitle))
                else errorResponseLiveData.postValue(response.details)
            }
        }
    }

    /**
     *
     * error response
     * {"message":"Sorry, the pin is invalid.","errorCode":"InvalidPin","errorCategoryKey":"AuthException","accountStatus":"OtpNeeded","userSentInvalidPin":"111111","maskedPhoneNumber":"919XXXXX1562","pinVerificationAttempt":"4","pinVerficationRemainingAttempts":"1"}
     *
     */
    fun verifyOtp(otpInput: String, mobile: String) {
        viewModelScope.launch {
            if(!this@LoginViewModel.userId.isNullOrEmpty() &&
                !this@LoginViewModel.token.isNullOrEmpty()){
                initBineSdk(this@LoginViewModel.token!!, mobile, this@LoginViewModel.userId!!)
            }
            else {
                val response = AuthManager.getInstance()
                    .validatePin(mobile, otpInput, DeviceUtil.getDeviceId(context))
                response.authToken?.let { token ->
                    this@LoginViewModel.token = token
                    userId = response.userId
                    // Init Bine SDK with the token
                    initBineSdk(token, mobile, response.userId)
                    response.isNewUSer?.let {
                        if (it) {
                            AnalyticsLogger.getInstance().logEvent(Event.APP_INSTALL)
                        }
                        //If not a new user
                        //Removing this as it would block first install after delete request completion
                        //incentivesRepository.setAppInstallSharedPrefs(it)
                    }
                } ?: response.error?.let {
                    AnalyticsLogger.getInstance()
                        .logGenericLogs(Event.LOGIN_ERROR,"ValidateOTP", response.error.toString(), response.details)
                    if (it == AuthError.NETWORK_ERROR) errorResponseLiveData.postValue(
                        context.getString(
                            R.string.bn_no_internet_subtitle
                        )
                    )
                    else errorResponseLiveData.postValue(response.details)
                }
            }
        }
    }

    private fun initBineSdk(authenticationToken: String, mobile: String, userId: String?) {
        viewModelScope.launch {
            val response = BineAPI.getInstance().init(authenticationToken,
                true, //isCreateUser = true -> User/user api will get called
                mobile)

            response.userId?.let {
                processLoginSuccess(mobile, userId, it)
            } ?:
            response.details?.let { details ->
                if (details.contains(BNConstants.APIError.ACCOUNT_DELETE_IN_PROGRESS.value)) {
                    //Account delete in progress
                    accountDeleteProgress.postValue(true)
                }
                else {
                    errorResponseLiveData.postValue(response.details)
                }
            } ?:
            response.error?.let {
                when (it) {
                    Error.NETWORK_ERROR -> errorResponseLiveData.postValue(
                        context.getString(R.string.bn_no_internet_subtitle)
                    )
                    else -> errorResponseLiveData.postValue(response.details)
                }
            }
        }
    }

    private fun processLoginSuccess(mobile: String,
                                    userId: String?/*from KMS*/,
                                    clientUserId: String/*from BlendNet*/) {
        userId?.let {
            SharedPreferenceStore.getInstance()
                .save(SharedPreferenceStore.KEY_USER_ID, userId)
        }
        SharedPreferenceStore.getInstance()
            .save(SharedPreferenceStore.KEY_CLIENT_USER_ID, clientUserId)

        FirebaseMessageReceiverService.registerFCM(mobile)

        verifyOtpLiveData.postValue(true)

        BootTelemetryLogger.getInstance().recordIntermediaryEvent(
            BootTelemetryLogger.BootMarker.LOGIN_COMPLETE)
    }
}