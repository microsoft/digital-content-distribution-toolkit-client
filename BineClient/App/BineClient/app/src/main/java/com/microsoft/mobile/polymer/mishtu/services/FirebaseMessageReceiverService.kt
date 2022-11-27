package com.microsoft.mobile.polymer.mishtu.services

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.microsoft.mobile.auth.AuthManager

import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.utils.NotificationHandler
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Source
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessageReceiverService: FirebaseMessagingService() {

    @Inject lateinit var notificationHandler : NotificationHandler

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, remoteMessage.data.toString())

        //Ignore the push received if user not logged in
        if(SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_USER_ID).isNullOrEmpty())
            return
        notificationHandler.onMessageReceived(this, remoteMessage.data)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, "New Token - $p0")
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FCM_TOKEN, p0)

        registerFCM(null)
    }

    companion object {
        private const val fcm_push_topic = "mishtu_entertainment"
        private const val TAG = "FirebaseMessageService"

        private fun getToken(phone: String?) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.v(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.v(TAG, "Re-Fetch Token - $token")
                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FCM_TOKEN, token)
                registerFCM(phone)
            })
        }

        /**
         * The purpose of phone number is only for resolving alpha endpoint.
         * In pre-prod it has no significance, hence ignoring it has no issue
         */
        fun registerFCM(phone: String?) {
            val accessToken = BineAPI.getInstance().getToken(Source.CLOUD)
            if (accessToken.isNullOrEmpty()) return

            val token = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_FCM_TOKEN)

            if (token.isNullOrEmpty()) getToken(phone)
            else {
                GlobalScope.launch {
                    val response = AuthManager.getInstance().registerFCM(
                        accessToken,
                        token,
                        phone ?: "",
                        arrayListOf(fcm_push_topic)
                    )
                    response.success?.let {
                        Log.v(TAG, "Register - Success")
                        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FCM_TOKEN_FAILED, "")
                        AnalyticsLogger.getInstance().logGenericLogs(
                            Event.API_LOG,
                            "FirebaseFCM",
                            "Success",
                            null)
                    } ?: response.error?.let {
                        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FCM_TOKEN_FAILED, "true")
                        AnalyticsLogger.getInstance().logGenericLogs(
                            Event.API_LOG,
                            "FirebaseFCM",
                            response.error.toString(),
                            response.details)
                    }
                }
            }
        }
    }
}