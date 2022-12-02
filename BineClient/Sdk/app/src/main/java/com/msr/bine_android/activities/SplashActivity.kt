// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.Gson
import com.msr.bine_android.R
import com.msr.bine_android.base.BaseActivity
import com.msr.bine_android.utils.BineNotificationHelper
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.hub.model.DownloadStatus
import com.msr.bine_sdk.player.BinePlayerViewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private lateinit var logo: ImageView

    private val TAG = "SplashActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        logo = findViewById(R.id.imageView);
        /*val dataRepository = DataRepository.getInstance(AppDatabase.getDatabase(this).folderDao(),
            AppDatabase.getDatabase(this).contentDao(),
            AppDatabase.getDatabase(this).cartDao(),
            AppDatabase.getDatabase(this).folderEntityDao(),
            SharedPreferenceStore(this))

        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeInAnim)
        fadeInAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (dataRepository.isFirstInstall()) {
                    dataRepository.firstInstall(false)
                    Telemetry.getInstance().sendAppInstall()
                    //dataRepository.firstLogin(false)
                }
                val intent = AppUtils.getNextIntent(applicationContext, dataRepository)
                startActivity(intent)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })*/
        testAPIs()
    }
    
    private fun testAPIs() {
        GlobalScope.launch {
            /*val subscriptionResponse = BineAPI.CMS().getSubscriptions("9346c779-6b9a-4802-9c9f-9be3accb5f63")
            subscriptionResponse.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${it[0].contentProviderId}")
            } ?:
            subscriptionResponse.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            }*/
            val response = BineAPI.getInstance().init("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cm46bWljcm9zb2Z0OmNyZWRlbnRpYWxzIjoie1wicGhvbmVOdW1iZXJcIjpcIis5MTEzMDAwMDAwMTVcIixcImNJZFwiOlwiMTgxMWEyODQtNWFiMy00YjRjLWIwOTYtMzUwYzAyMWZhM2JlXCIsXCJ0ZXN0U2VuZGVyXCI6XCJ0cnVlXCIsXCJhcHBOYW1lXCI6XCJjb20ubWljcm9zb2Z0Lm1vYmlsZS5wb2x5bWVyLm1pc2h0dVwiLFwicGVybWlzc2lvbnNcIjpcIjEuMVwiLFwiYXBwbGljYXRpb25UeXBlXCI6MyxcInRva2VuVmFsaWRGcm9tXCI6MTY0MTI3NjgyNzY2MyxcInRlc3RVc2VyUHJvcGVydGllc1wiOlwie1xcXCJ0dVxcXCI6dHJ1ZX1cIn0iLCJ1aWQiOiJNb2JpbGVBcHBzU2VydmljZTo3NjgyMzBhOS1kYTQwLTRjNWQtOWFlMC1mYzg1MWZlYmU4ZjUiLCJ2ZXIiOiIyIiwibmJmIjoxNjQxMjc2ODI3LCJleHAiOjE2NDM4Njg4MjcsImlhdCI6MTY0MTI3NjgyNywiaXNzIjoidXJuOm1pY3Jvc29mdDp3aW5kb3dzLWF6dXJlOnp1bW8iLCJhdWQiOiJ1cm46bWljcm9zb2Z0OndpbmRvd3MtYXp1cmU6enVtbyJ9.qW7IsGKVJOGLaZp1LuGE0uOAJ-m9QdlOj8PM9dsNua0",
                    false,
                    "+911300000015")

            //startDownload()

            /*Downloader(this@SplashActivity, BineNotificationHelper(this@SplashActivity)).clearDownloads(
                arrayListOf("dacc7746-9846-49c4-8797-7dd59f585ddc"))*/
            findViewById<Button>(R.id.button_download).setOnClickListener {
                val intent = Intent(this@SplashActivity, BinePlayerViewActivity::class.java)
                intent.putExtra(BinePlayerViewActivity.keyExtraVideoURL,
                    "http://192.168.0.115:5000/static/mstore/storage/7005_4de89299-b07f-431f-94c7-9d8dff4b301a_211230183000_221231182959/4de89299-b07f-431f-94c7-9d8dff4b301a.mpd"
                )
                intent.putExtra(BinePlayerViewActivity.keyExtraDashURL,
                    "https://streaming-endpoint-stage-amsnearmestage-jpea.streaming.media.azure.net/d81c18ef-bf36-43c6-ac9d-3d8aaceced9a/Premikudu_NandKishore_Telugu.ism/manifest(format=mpd-time-csf,encryption=cenc)"
                )
                intent.putExtra(Constants.TITLE, "Exclusive MO")
                intent.putExtra(Constants.FOLDER_ID, "dacc7746-9846-49c4-8797-7dd59f585ddc")
                startActivity(intent)
            }

            findViewById<Button>(R.id.button_play_online).setOnClickListener {
                val intent = Intent(this@SplashActivity, BinePlayerViewActivity::class.java)
                intent.putExtra(BinePlayerViewActivity.keyExtraVideoURL,
                    "https://streaming-endpoint-stage-amsnearmestage-jpea.streaming.media.azure.net/4663c464-4b9c-4cfa-800c-ad698d35bebc/Mannan.ism/manifest(format=mpd-time-csf,encryption=cenc)"
                )
                intent.putExtra(Constants.TITLE, "Exclusive MO")
                intent.putExtra(Constants.FOLDER_ID, "32ef28f1-0b46-43c3-949b-e8725c310e11")
                startActivity(intent)
            }

            /*val intent = Intent(this@SplashActivity, BinePlayerViewActivity::class.java)
            intent.putExtra(Constants.VIDEO_URL, "https://streaming-endpoint-stage-amsblendnetstage-inct.streaming.media.azure.net/2cd0f116-1772-4f12-aa94-100599e2b40f/Premikudu.ism/manifest(format=mpd-time-csf,encryption=cenc)")
            intent.putExtra(Constants.TITLE, "Premikudu")
            intent.putExtra(Constants.FOLDER_ID, "b605aca8-e2f6-4d19-b8cb-4ebe75030656")
            intent.putExtra(Constants.PLAY_BACK_POSITION, 0)
            intent.putExtra(Constants.IS_STREAMING, true)
            startActivityForResult(intent,1001)*/

            /*val incentives = BineAPI.Incentives().getIncentivePlan()
            incentives.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            }*/

            /*val orderResponse = BineAPI.OMS().createOrder("82dcb50b-d7de-4a5b-84a1-0e506a2b288d", "4d7d1435-08ae-4ae4-b6c1-8dc04ed3b54b");

            orderResponse.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            orderResponse.error?.let {
                Log.d(TAG + "order", "WorkRequest Fetched data - ${orderResponse.details}")
            }*/


            /*val events = BineAPI.Incentives().recordEvent(IncentivesManager.IncentiveEventType.APP_LAUNCH, Date(), null)
            events.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            }*/

            /*val contentProviderResponse = BineAPI.CMS().getContentProviders()

            contentProviderResponse.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            }*/

            /*var contentId = ""
            val response = BineAPI.CMS().getContent("0f1c4593-c132-4037-9f5d-cee8552346b2")
            response.result?.let {
                contentId = it[0].contentId
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            response.error?.let {
                CloudContentWorker.PREVIOUS_REQUEST_FAILED = true
            }
            val orderResponse = BineAPI.OMS().createOrder("0f1c4593-c132-4037-9f5d-cee8552346b2", "880eeade-1c12-4255-aa72-03598099a26a");

            orderResponse.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            orderResponse.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${orderResponse.details}")
            }

            val orders = BineAPI.OMS().getOrders(arrayListOf("Cancelled"))
            orders.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            orders.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${orderResponse.details}")
            }

            val order = BineAPI.OMS().getOrder("4fe7e474-3d47-4a06-bad9-01c46968aaf6")

            order.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            order.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${order.details}")
            }

            val subscription = BineAPI.OMS().getActiveSubscriptions()
            subscription.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            subscription.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${subscription.details}")
            }

            val token = BineAPI.OMS().getAssetToken(contentId)
            token.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            token.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${token.details}")
            }*/

            /*val retailers = BineAPI.RMS().getNearbyRetailer(200.0, 17.490402, 78.343561)
            retailers.result?.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            } ?:
            retailers.error?.let {
                Log.d(TAG, "WorkRequest Fetched data - ${retailers.details}")
            }*/

            /*val res = BineAPI.UMS().assignRetailer("RN-944175")
            res.result.let {
                Log.d(TAG, "WorkRequest Fetched data - $it")
            }*/


           /* val intent = Intent(this@SplashActivity, BinePlayerViewActivity::class.java)
            intent.putExtra(Constants.VIDEO_URL,
                "https://amsblendnetdev-inct.streaming.media.azure.net/da7647d5-008c-4376-ba67-c7dba4062490/Ignite-short.ism/manifest(format=mpd-time-csf,encryption=cenc)"
            )
            intent.putExtra(Constants.TITLE, "folder.bineMetadata.title")
            intent.putExtra(Constants.FOLDER_ID, "2e3e1bac-547d-43cf-81bb-943f94564d61")
            intent.putExtra(Constants.IS_STREAMING, true)
            startActivity(intent)
            finish()*/
        }
    }

/*
    private fun startDownload() {
        val content = Content("dacc7746-9846-49c4-8797-7dd59f585ddc",
            "1f6d5fd0-31f2-4195-87b3-afa48a0126d6",
            "1f6d5fd0-31f2-4195-87b3-afa48a0126d6",
            "Exclusive MO",
            "", "", "", "", "", "", "",
            128F, "0", "",
            "https://streaming-endpoint-stage-amsnearmestage-jpea.streaming.media.azure.net/d81c18ef-bf36-43c6-ac9d-3d8aaceced9a/Premikudu_NandKishore_Telugu.ism/manifest(format=mpd-time-csf,encryption=cenc)", true,
            isHeaderContent = false,
            isExclusiveContent = false,
            people = listOf(Content.Artist("", "")),
            hierarchy = "",
            attachments = listOf(Content.Attachment("mo.jpg","Thumbnail"), Content.Attachment("mo1.jpg", "Thumbnail")),
            createdDate = "",
            ageAppropriateness = "",
            contentAdvisory = "",
            audioTarFileSize = 1024,
            videoTarFileSize = 1024,
            null
        )

        val videoUrl = "http://192.168.0.115:5000/static/mstore/storage/7005_4de89299-b07f-431f-94c7-9d8dff4b301a_211230183000_221231182959/4de89299-b07f-431f-94c7-9d8dff4b301a.mpd"
        val uriValue = Uri.parse(videoUrl)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Starting - $videoUrl", Toast.LENGTH_LONG).show()
        }
        val request = DownloadRequest(content.contentId,
            uriValue,
            Downloader.TYPE.EXO,
            Gson().toJson(content))

        Downloader(this, BineNotificationHelper(this)).start(request, object: DownloadNotifier{
            override fun onDownloadSuccess(
                folder: Content,
                time: Long
            ) {
            }

            override fun onDownloadQueued(folderID: String?, type: Downloader.TYPE?) {
            }

            override fun onLicenseDownloaded(folderID: String?) {
            }

            override fun onDownloadGenericEvent(
                folderID: String?,
                event: String?,
                message: String?
            ) {
            }

            override fun onDownloadProgress(
                folderID: String?,
                status: DownloadStatus?,
                progress: Int
            ) {
            }

            override fun onDownloadDeleted(folderID: String?) {
            }

            override fun onDownloadFailure(folderID: String?, exception: String?) {
            }

        })
    }
*/
}
