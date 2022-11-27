package com.microsoft.mobile.polymer.mishtu.telemetry

import android.content.Context
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import java.io.File
import java.util.*

class ClientLogging() {



    companion object{
        private var instance = ClientLogging()
        private val filename = "logcat_meraMishtu.txt"
        lateinit var  outputFile: File

        fun getInstance(): ClientLogging = instance

        fun init(context: Context){
            try{
                outputFile = File(context.externalCacheDir,filename)
                if(!outputFile.exists()){
                    outputFile.createNewFile()
                }
            }catch (e: Exception){

            }
        }
        //Clear the log file after 10 day
        fun clearClientLogFileIfTimeExceeded() {
            try {
                val logFileStartDate = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.LOGFILE_START_DATE)
                if (logFileStartDate.isNullOrEmpty()) {
                    SharedPreferenceStore.getInstance().save(SharedPreferenceStore.LOGFILE_START_DATE, Date().time.toString())
                } else if (Date().time - logFileStartDate.toLong() > 10* 24 * 60 * 60 * 1000) {
                    outputFile.writeText("")
                }
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }

    }
    fun writeLog(logTag: String, log: String){
        //val fileOutputStream = FileOutputStream(outputFile,true)


        val text: String = Date().toString() + "  " + logTag + " : " + log
        try {
            outputFile.appendText(text + System.getProperty("line.separator"))
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }








}