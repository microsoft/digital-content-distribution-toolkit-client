package com.microsoft.mobile.polymer.mishtu.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.IFCMHandler
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants.NotificationType
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.microsoft.mobile.polymer.mishtu.AppLifecycleObserver
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class NotificationHandler internal constructor(
    private val contentRepository: ContentRepository,
    private val orderRepository: OrderRepository,
    private val notificationBadgeHelper: NotificationBadgeHelper,
) : IFCMHandler {

    companion object {
        const val NOTIFICATION_ID = "NotificationId"
        private const val MISHTU_NOTIFICATION_ORDER_ID = 2
        private const val MISHTU_NOTIFICATION_NEW_ARRIVAL_ID = 1
        private const val MISHTU_NOTIFICATION_OFFERS_ID = 3
        private const val MISHTU_NOTIFICATION_EXPORT_DATA_COMPLETE_ID = 4

        private const val PUSH_CHANNEL_ID = "com.microsoft.mobile.polymer.mishtu.push_channel"
    }

    override fun onMessageReceived(
        context: Context,
        notificationData: Map<String, String>,
    ) {

        val type = notificationData["type"]
        type?.let {
            when (it) {
                NotificationType.OrderComplete.value -> {
                    val orderId =
                        Gson().fromJson(notificationData["additional_props"], HashMap::class.java)
                            .get("OrderId") as String
                    if (AppLifecycleObserver.IS_APP_IN_FOREGROUND) {
                        postToEventBusForOrderCompleteEvent(orderId)
                    } else {
                        showOrderCompleteNotification(context, notificationData)
                    }
                    return
                }
                NotificationType.NewContentArrived.value -> {
                    showNewContentNotification(context, notificationData)
                    return
                }
                NotificationType.NewOffer.value -> {
                    showNewOfferNotification(context, notificationData)
                    return
                }
                NotificationType.UserDataExportComplete.value -> {
                    showUserDataExportCompleteNotification(context, notificationData)
                    return
                }
            }
            //telemetry
            AnalyticsLogger.getInstance().logNotification(it)
        }
    }

    private fun showUserDataExportCompleteNotification(
        context: Context,
        notificationData: Map<String, String>,
    ) {

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val openAppIntent = MainActivity.getMishtuNotificationIntent(
            context, NotificationType.UserDataExportComplete.name,
            notificationData["id"]
        )
        openAppIntent.putExtra(NOTIFICATION_ID, MISHTU_NOTIFICATION_EXPORT_DATA_COMPLETE_ID)
        stackBuilder.addNextIntent(openAppIntent)
        val pendingIntent: PendingIntent? = stackBuilder.getPendingIntent(
            MISHTU_NOTIFICATION_ORDER_ID,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.bn_app_logo)
            .setContentTitle(notificationData["title"])
            .setContentText(notificationData["body"])
            .addAction(R.drawable.fw_ads_choice,
                context.getString(R.string.download_data),
                pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (AppUtils.isOreoOrAbove()) {
            createChannelIfNotExist(context)
        }
        notificationManager.notify(MISHTU_NOTIFICATION_EXPORT_DATA_COMPLETE_ID,
            notificationBuilder.build())
        notificationBadgeHelper.markNotification(NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY)
    }

    override fun onDeleteMessages() {
    }

    private fun showNewContentNotification(
        context: Context,
        notificationData: Map<String, String>,
    ) {
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val openAppIntent = MainActivity.getMishtuNotificationIntent(
            context, NotificationType.NewContentArrived.name,
            notificationData["id"]
        )
        openAppIntent.putExtra(NOTIFICATION_ID, MISHTU_NOTIFICATION_NEW_ARRIVAL_ID)
        stackBuilder.addNextIntent(openAppIntent)
        val pendingIntent: PendingIntent? = stackBuilder.getPendingIntent(
            MISHTU_NOTIFICATION_NEW_ARRIVAL_ID,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val imageUri: Uri? = Uri.parse(notificationData["image_url"])
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.bn_app_logo)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(imageUri?.let { getBitmapFromUrl(it) }))
            .setContentTitle(notificationData["title"])
            .setContentText(notificationData["body"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.exo_icon_play, context.getString(R.string.button_watch_now),
                pendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (AppUtils.isOreoOrAbove()) {
            createChannelIfNotExist(context)
        }
        notificationManager.notify(MISHTU_NOTIFICATION_NEW_ARRIVAL_ID, notificationBuilder.build())

        contentRepository.fetchContentAsync()

        notificationBadgeHelper.markNotification(NotificationBadgeHelper.BadgeType.NEW_CONTENT)
    }

    private fun showOrderCompleteNotification(
        context: Context,
        notificationData: Map<String, String>,
    ) {
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val openAppIntent = MainActivity.getMishtuNotificationIntent(
            context, NotificationType.OrderComplete.name,
            notificationData["id"]
        )
        openAppIntent.putExtra(NOTIFICATION_ID, MISHTU_NOTIFICATION_ORDER_ID)
        stackBuilder.addNextIntent(openAppIntent)
        val pendingIntent: PendingIntent? = stackBuilder.getPendingIntent(
            MISHTU_NOTIFICATION_ORDER_ID,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.bn_app_logo)
            .setContentTitle(notificationData["title"])
            .setContentText(notificationData["body"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (AppUtils.isOreoOrAbove()) {
            createChannelIfNotExist(context)
        }
        notificationManager.notify(MISHTU_NOTIFICATION_ORDER_ID, notificationBuilder.build())
        val orderId = Gson().fromJson(notificationData["additional_props"], HashMap::class.java)
            .get("OrderId") as String
        var cpId : String? = null
        GlobalScope.launch {
            val order = orderRepository.getActiveOrderByOrderId(orderId)
            cpId = order?.contentProviderId
            if (cpId != null) {
                order?.subscriptionId?.let { SharedPreferenceStore.getInstance().save(SharedPreferenceStore.NOTIFICATION_ORDER_COMPLETE_SUBSCRIPTIONID, it) }
                orderRepository.fetchActiveSubscriptions(cpId)
            }
        }

        /*notificationBadgeHelper.saveNotification(
            Notification(
                NotificationBadgeHelper.NOTIFICATION_TYPE_ORDER_CONFIRMED,
                notificationData["title"] ?: "",
                notificationData["body"] ?: "",
                "", false, notificationData["id"]
            ), NotificationBadgeHelper.NOTIFICATION_TYPE_ORDER_CONFIRMED
        )*/
    }

    private fun postToEventBusForOrderCompleteEvent(orderId: String) {
        var cpId: String? = null
        GlobalScope.launch {
            val order = orderRepository.getActiveOrderByOrderId(orderId)
            cpId = order?.contentProviderId
            if (cpId != null) {
                orderRepository.fetchActiveSubscriptions(cpId!!)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (AppLifecycleObserver.IS_APP_IN_FOREGROUND) {
                        order?.subscriptionId?.let {
                            EventBus.getDefault().post(OrderCompleteEvent(it,
                                    BOConverter.EventType.CONSUMER_INCOME_ORDER_COMPLETED))
                        }
                    }
                }, 500)
            }
        }
    }

    private fun showNewOfferNotification(context: Context, notificationData: Map<String, String>) {
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val openAppIntent =
            MainActivity.getMishtuNotificationIntent(context, NotificationType.NewOffer.name, "")

        stackBuilder.addNextIntent(openAppIntent)
        val pendingIntent: PendingIntent? = stackBuilder.getPendingIntent(
            MISHTU_NOTIFICATION_OFFERS_ID,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.bn_app_logo)
            .setContentTitle(notificationData["title"])
            .setContentText(notificationData["body"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (AppUtils.isOreoOrAbove()) {
            createChannelIfNotExist(context)
        }
        notificationManager.notify(MISHTU_NOTIFICATION_OFFERS_ID, notificationBuilder.build())
    }

    /*
     *To get a Bitmap image from the URL received
     * */
    private fun getBitmapFromUrl(imageUrl: Uri): Bitmap? {
        return try {
            val url = URL(imageUrl.toString())
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannelIfNotExist(context: Context) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel: NotificationChannel? =
            notificationManager.getNotificationChannel(PUSH_CHANNEL_ID)

        if (notificationChannel == null) {
            val cloudPushChannel = NotificationChannel(
                PUSH_CHANNEL_ID,
                context.getString(R.string.content_messages_channel_title),
                NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(cloudPushChannel)
        }
    }
}