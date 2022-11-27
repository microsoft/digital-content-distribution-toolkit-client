package com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker

import android.util.Log
import androidx.collection.arrayMapOf
import com.google.gson.GsonBuilder
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import java.text.SimpleDateFormat
import java.util.*

/**
 * Logger that records various events after boot
 * On listed markers complete log -> Fire Telemetry with details
 * https://stackoverflow.com/questions/10302803/which-method-gets-called-the-moment-the-activity-is-fully-laid-out-and-ready-for
 */
class BootTelemetryLogger {
    private var bootLog: BootLog? = null

    enum class BootType(val value: String) {
        COLD("ColdBoot"),
        WARM("WarmBoot")
    }

    enum class BootPage(val value: String) {
        FIREWORKS("Fireworks"),
        FILMS("Films")
    }

    enum class BootMarker(val value:String)  {
        LOGIN_COMPLETE("Login"),
        REQUEST_LOCATION_COMPLETE("ReqLoc"),
        CATALOG_GET("CatalogFetch"),

        PAGE_CREATE("OnCreate"),

        //Screen name would be appended to the keys
        //BootPage.FIREWORKS
        FIREWORK_CATEGORY_LOAD("CatLoad"),
        FIREWORK_SDK_INIT("SDKInit"),

        //BootPage.FILMS
        FILMS_PRE_LOAD("PreFetchLoad"),
        FILMS_DB_FETCH("DBFetch"),
        FILMS_LOAD_COMPLETE("LoadComplete")
    }

    companion object {
        const val TOTAL_PAGE_COUNT = 2
        const val TAG = "BootTelemetryLogger"

        private var instance = BootTelemetryLogger()

        @Synchronized
        fun getInstance(): BootTelemetryLogger = instance
    }

    /**
     * Start Cold/Warm boot
     */
    fun start(bootType: BootType) {
        if (bootLog == null) {
            Log.d(TAG, "start - ${bootType.value}")
            bootLog = BootLog(Date(), bootType.value, arrayMapOf(), arrayMapOf())
        }
    }

    /**
     * Record intermediary events of interest
     */
    fun recordIntermediaryEvent(bootEvent: BootMarker) {
        bootLog?.let {
            Log.d(TAG, "recordIntermediaryEvent - ${bootEvent.value}")
            it.intermediateMarkers[bootEvent.value] = Date()
        }
    }

    /**
     * Record Page events
     * Begins with PAGE_CREATE followed by markers of interest
     */
    fun recordPageEvent(page: BootPage, bootMarker: BootMarker) {
        bootLog?.let {
            // Page is just getting created
            if (bootMarker == BootMarker.PAGE_CREATE) {
                val pageLoad = BootLog.PageLoad(page.value,
                    Date(),
                    arrayMapOf(),
                    getCompletionMarker(it.bootType, page))
                it.pageLoads[page.value] = pageLoad

                Log.d(TAG, "recordPageEvent* - ${page.value} - ${bootMarker.value}")
            }
            else {
                //Regular scenario when page was created and markers are getting recorded
                it.pageLoads[page.value]?.let { existingPage ->
                    val createTime = existingPage.createTime
                    val elapsedTime = ((Date().time - createTime.time).toFloat()/1000)
                    existingPage.markers[bootMarker.value] = "${elapsedTime}s"
                    it.pageLoads[page.value] = existingPage

                    Log.d(TAG, "recordPageEvent** - ${page.value} - ${bootMarker.value}")
                } ?:
                //Rare scenario when without page create, marker are called record --> Need to validate
                run {
                    val pageLoad = BootLog.PageLoad(page.value,
                        Date(),
                        arrayMapOf(),
                        getCompletionMarker(it.bootType, page))
                    val createTime = Date()
                    val elapsedTime = ((Date().time - createTime.time).toFloat()/1000)
                    pageLoad.markers[bootMarker.value] = "${elapsedTime}s"
                    it.pageLoads[page.value] = pageLoad

                    Log.d(TAG, "recordPageEvent*** - ${page.value} - ${bootMarker.value}")
                }
            }

            //Check for marker complete & post telemetry
            pageLoadCompleted()
        }
    }

    /**
     * Reset boot events
     * Occurs
     * 1. after user goes to background
     * 2. When markers of interest is done logging
     * 3. User logs out to capture fresh markers
     */
    fun reset(isLogout: Boolean) {
        if (isLogout) {
            bootLog?.intermediateMarkers?.clear()
            bootLog?.pageLoads?.clear()
        }
        else bootLog = null
    }

    private fun getCompletionMarker(bootType: String, page: BootPage): Int {
        return when(page) {
            BootPage.FIREWORKS -> if (bootType == BootType.COLD.name) 2 else 1
            BootPage.FILMS -> 3
        }
    }

    private fun pageLoadCompleted() {
        bootLog?.let {
            //Log.d(TAG, "pageLoadCompleted ${Gson().toJson(it)}")
            var totalCompletes = 0
            for (pageLoad in it.pageLoads.values) {
                if (pageLoad.markers.size >= pageLoad.markersCount) {
                    totalCompletes++
                    Log.d(TAG, "pageLoadCompleted - ${pageLoad.pageName}")
                }
            }

            if (totalCompletes == TOTAL_PAGE_COUNT) {
                //Record telemetry
                recordBootTelemetry()
            }
        }
    }

    private fun recordBootTelemetry() {
        bootLog?.let {
            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val params = HashMap<String, String>()
            params["BootType"] = it.bootType
            params["CreateTime"] = timeFormatter.format(it.createTime)
            params["IntermediaryMarkers"] = it.intermediateMarkers.toString()
            for(pageLoad in it.pageLoads.values) {
                for (marker in pageLoad.markers) {
                    params[pageLoad.pageName+marker.key] = marker.value
                }
            }
            AnalyticsLogger.getInstance().logEvent(Event.BOOT_MARKERS, params)

            Log.d(TAG, "recordBootTelemetry")
            reset(false)
        }
    }
}