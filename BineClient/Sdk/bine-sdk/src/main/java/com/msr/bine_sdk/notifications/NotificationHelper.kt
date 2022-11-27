package com.msr.bine_sdk.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.download.exo.ExoDownloadService
import com.msr.bine_sdk.notifications.receivers.NotificationReceiver
import com.msr.bine_sdk.secure.BineSharedPreference

abstract class NotificationHelper(context: Context) {
    private val context: Context = context.applicationContext

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

        //WIFI Channel
        val notificationChannel = NotificationChannel(Constants.ID_NETWORK_CHANNEL,
                Constants.NETWORK_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(notificationChannel)

        //Create all channels
        val downloadProgressChannel = NotificationChannel(Constants.ID_PROGRESS_CHANNEL,
                Constants.PROGRESS_CHANNEL, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(downloadProgressChannel)

        //Create all channels
        val cloudPushChannel = NotificationChannel(Constants.ID_PUSH_CHANNEL,
                Constants.PUSH_CHANNEL, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(cloudPushChannel)
    }

    fun show(channel: String?, title: String?, message: String?) {
        val builder = NotificationCompat.Builder(context, channel!!)
            .setContentText(message)
            .setSmallIcon(getAppIcon())
            .setAutoCancel(true)
        val notification = builder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_WIFI, notification)
    }

    fun showHubConnectNotification(smallIcon: Int) {
        val notification = getHubReachedNotification(smallIcon)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_WIFI, notification)
    }
    fun showProgress(current: Int,
                     image: String?,
                     movieName: String?, folderID: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_DOWNLOAD_PROGRESS, getProgress(current, image, movieName, folderID))
    }

    fun showDownloadComplete(image: String?, movieName: String?, folderID: String?, videoURL: String?, smallIcon: Int,mpdPath:String, size: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_DOWNLOAD_PROGRESS, getDownloadCompleteNotification(image, movieName, folderID, videoURL, mpdPath, "", size, NOTIFICATION_DOWNLOAD_PROGRESS))
    }
    fun showDownloadFailed(movieName: String?, icon: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_DOWNLOAD_PROGRESS, getContentFailedNotifications(movieName, icon))
    }

    fun startProgress(channel: String?, max: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channel!!)
                .setContentTitle("Video Download")
                .setContentText("Download in progress")
                .setSmallIcon(getAppIcon())
                .setAutoCancel(false)
        builder.setProgress(max, 0, false)
        notificationManager.notify(NOTIFICATION_DOWNLOAD_PROGRESS, builder.build())
    }

    @SuppressLint("StringFormatInvalid")
    fun getProgress(current: Int,
                    thumbnail: String?,
                    movieName: String?, folderID: String?): Notification {
        val notificationLayout = RemoteViews(context.packageName, com.msr.bine_sdk.R.layout.notification_download_progress)
        notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.idContentName, movieName)
        notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.progress_text, String.format(context.getString(com.msr.bine_sdk.R.string.text_download_status), current, "%"))

        notificationLayout.setInt(com.msr.bine_sdk.R.id.download_progress, "setProgress", current)
        val closeIntent = Intent(context, ExoDownloadService::class.java);
        closeIntent.putExtra(Constants.FOLDER_ID, folderID);
        closeIntent.action = Constants.CANCEL_DOWNLOAD;

        val pcloseIntent = PendingIntent.getService(context, 111,
                closeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val customNotification = NotificationCompat.Builder(context, Constants.ID_PROGRESS_CHANNEL)
                .setSmallIcon(getAppIcon())
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(getHomePendingIntent(true))
                .setColor(ContextCompat.getColor(context, com.msr.bine_sdk.R.color.colorPrimary))

        customNotification.addAction(NotificationCompat.Action(com.msr.bine_sdk.R.drawable.ic_close,
            HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(
                context, com.msr.bine_sdk.R.color.colorBtnPermission) + "\">" +
                    context.getString(com.msr.bine_sdk.R.string.cancel) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
            pcloseIntent))
        customNotification.setChannelId(Constants.ID_PROGRESS_CHANNEL)

        if (thumbnail != null) {
            loadImageWithURL(Uri.parse(getMetaResourceBaseUrl() + thumbnail),
                notificationLayout, customNotification.build(),
                ExoDownloadService.DOWNLOAD_STATUS_NOTIFICATION_ID)
        }
        return customNotification.build()
    }

    fun getDownloadCompleteNotification(thumbnail: String?, movieName: String?, folderID: String?, videoURL: String?,mpdPath: String, time: String, size: Long, notificationId: Int): Notification {
        val notificationLayout = RemoteViews(context.packageName, com.msr.bine_sdk.R.layout.notification_download_completed)
        //val threadCount = BineSharedPreference(context).get(BineSharedPreference.KEY_PARALLEL_COUNT)
        notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.idContentName, movieName)
        //notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.idContentName, "$movieName \nDownload time: $time \nSize: $size \nThread Count: $threadCount")

        val openPlayerIntent = Intent(context, NotificationReceiver::class.java)
        openPlayerIntent.putExtra("id", DOWNLOAD_STATUS_NOTIFICATION_ID)
        openPlayerIntent.putExtra(Constants.TITLE, movieName)
        if(!TextUtils.isEmpty(mpdPath)){
            openPlayerIntent.putExtra(Constants.VIDEO_URL, mpdPath)
        }else{
            openPlayerIntent.putExtra(Constants.VIDEO_URL, videoURL)
        }
        openPlayerIntent.putExtra(Constants.FOLDER_ID, folderID)
        /*val pcloseIntent = PendingIntent.getBroadcast(context, DOWNLOAD_STATUS_NOTIFICATION_ID,
                openPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        val builder = NotificationCompat.Builder(context, Constants.ID_PROGRESS_CHANNEL)
        builder.setSmallIcon(getAppIcon())
        builder.setAutoCancel(true)
        builder.setCustomContentView(notificationLayout)
        builder.setCustomBigContentView(notificationLayout)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setChannelId(Constants.ID_PROGRESS_CHANNEL)
        /*builder.addAction(NotificationCompat.Action(com.msr.bine_sdk.R.drawable.ic_close,
                HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(
                        context, com.msr.bine_sdk.R.color.colorBtnPermission) + "\">" +
                        context.getString(com.msr.bine_sdk.R.string.button_title_play) + "</font>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY), pcloseIntent))*/
        builder.color = ContextCompat.getColor(context, com.msr.bine_sdk.R.color.colorPrimary)
        builder.setContentIntent(getHomePendingIntent(true))

        if (thumbnail != null) {
            loadImageWithURL(Uri.parse(getMetaResourceBaseUrl() + thumbnail),
                notificationLayout, builder.build(), notificationId)
        }
        return builder.build()
    }

    fun getContentFailedNotifications(movieName: String?, icon: Int): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, Constants.ID_PROGRESS_CHANNEL)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(com.msr.bine_sdk.R.string.download_failed))
                .setContentText(movieName)
                .setAutoCancel(true)
                .setContentIntent(getHomePendingIntent(true))
        notificationBuilder.setChannelId(Constants.ID_PROGRESS_CHANNEL)
        return notificationBuilder.build()
    }

    fun getContentDeleteNotifications(movieName: String?, smallIcon: Int): Notification {
        val notificationLayout = RemoteViews(context.packageName, com.msr.bine_sdk.R.layout.notification_content_delete)
        notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.idContentName, movieName)
        val builder = NotificationCompat.Builder(context, Constants.ID_PROGRESS_CHANNEL)
        builder.setSmallIcon(smallIcon)
        builder.setAutoCancel(true)
        builder.setCustomContentView(notificationLayout)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setChannelId(Constants.ID_PROGRESS_CHANNEL)
        return builder.build()
    }

    fun getHubReachedNotification(icon: Int): Notification {
        val notificationLayout = RemoteViews(context.packageName, com.msr.bine_sdk.R.layout.notification_connect_hub)
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("id", NOTIFICATION_WIFI)
        val clickPendingIntent = PendingIntent.getBroadcast(context,
                NOTIFICATION_WIFI, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationLayout.setOnClickPendingIntent(com.msr.bine_sdk.R.id.idBtnConnect, clickPendingIntent)
        val builder = NotificationCompat.Builder(context, Constants.ID_NETWORK_CHANNEL)
        builder.setSmallIcon(icon)
        builder.setCustomContentView(notificationLayout)
        builder.setCustomBigContentView(notificationLayout)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setChannelId(Constants.ID_NETWORK_CHANNEL)
        return builder.build()
    }

    fun showPromoCodeNotification(couponCode: String, smallIcon: Int) {
        val notificationLayout = RemoteViews(context.packageName, com.msr.bine_sdk.R.layout.notiification_promocode)
        val spannableString = SpannableString(couponCode + context.getString(com.msr.bine_sdk.R.string.str_promocode_desc_notification))
        spannableString.setSpan(RelativeSizeSpan(1.2f), 0, couponCode.length, 0) // set size
        notificationLayout.setTextViewText(com.msr.bine_sdk.R.id.idConntentDesc, spannableString)
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("id", NOTIFICATION_VOUCHER)
        intent.putExtra(Constants.COUPON_VALUE, couponCode)
        val pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_VOUCHER,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationLayout.setOnClickPendingIntent(com.msr.bine_sdk.R.id.idBtnConnect, pendingIntent)
        val builder = NotificationCompat.Builder(context, Constants.ID_NETWORK_CHANNEL)
        builder.setSmallIcon(smallIcon)
        builder.setCustomContentView(notificationLayout)
        builder.setCustomBigContentView(notificationLayout)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setChannelId(Constants.ID_NETWORK_CHANNEL)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_VOUCHER, builder.build())
    }

    /*
   *To get a Bitmap image from the URL received
   * */
    private fun loadImageWithURL(imageUrl: Uri, remoteView: RemoteViews,notification: Notification, notificationId: Int) {
        val target = NotificationTarget(
            context,
            com.msr.bine_sdk.R.id.idContentImage,
            remoteView,
            notification,
            notificationId
        )

        Glide.with(context.applicationContext)
            .asBitmap()
            .load(imageUrl)
            .into(target)
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
    }

    companion object {
        const val NOTIFICATION_WIFI = 101
        const val NOTIFICATION_DOWNLOAD_PROGRESS = 101
        const val NOTIFICATION_DOWNLOAD_FAILD = 102
        const val NOTIFICATION_CLOUD = 111
        const val NOTIFICATION_VOUCHER = 121
        const val DOWNLOAD_STATUS_NOTIFICATION_ID = 1
        //private var sharedInstance: NotificationHelper? = null

        /*@JvmStatic
        fun getInstance(context: Context): NotificationHelper {
            if (sharedInstance == null) {
                sharedInstance = NotificationHelper(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sharedInstance!!.createNotificationChannels()
                }
            }
            return sharedInstance as NotificationHelper
        }*/
    }

    open fun getHomePendingIntent(showDownloads: Boolean): PendingIntent? {
        val intent = getHomeLaunchIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            121,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    abstract fun getMetaResourceBaseUrl(): String
    abstract fun getAppIcon(): Int
    abstract fun getHomeLaunchIntent(): Intent
}