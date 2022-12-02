// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.*
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.Error
import com.msr.bine_sdk.cloud.models.User
import com.msr.bine_sdk.download.exo.DRMManager

import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.storage.AppDatabase
import com.microsoft.mobile.polymer.mishtu.telemetry.ClientLogging
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val contentRepository: ContentRepository) : ViewModel() {

    var error = MutableLiveData<Error>()
    var errorString = MutableLiveData<String>()
    var userDetails = MutableLiveData<User>()
    var userUpdateResponse = MutableLiveData<Pair<Boolean, Error?>>()
    var exportDataNewRequest = MutableLiveData<Boolean>()
    var exportDataRequestSubmitted = MutableLiveData<Boolean>()
    var exportDataReadyToDownload = MutableLiveData<Pair<String?, String?>>()
    var exportDataNewRequestResponse = MutableLiveData<Boolean>()
    var loading = MutableLiveData<Boolean>()
    var deleteAccountSuccess = MutableLiveData<Boolean>()

    private val DATAEXPORT_STATUS_NotInitialized: String = "NotInitialized"
    private val DATAEXPORT_STATUS_ExportedDataNotified: String = "ExportedDataNotified"



    @DelicateCoroutinesApi
    fun logout(context: Context) {
        GlobalScope.launch {
            clearApplicationData(context)
            appDatabase.clearAllTables()
            contentRepository.clearWatchList()
            DRMManager.getInstance(context).getDownloadManager().removeAllDownloads()
        }
    }

    private fun clearApplicationData(context: Context) {
        val cacheDirectory: File = context.cacheDir
        cacheDirectory.parent?.let {
            val applicationDirectory = File(it)
            if (applicationDirectory.exists()) {
                applicationDirectory.list()?.let { list->
                    for (fileName in list) {
                        if (fileName != "lib") {
                            deleteDirectoryRecursively(File(applicationDirectory, fileName))
                        }
                    }
                }
            }
        }
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().clear().apply()

        //val fcmToken = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_FCM_TOKEN)
        SharedPreferenceStore.getInstance().reset()
        /*fcmToken?.let {
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FCM_TOKEN, it)
        }*/
        BineAPI.getInstance().resetUser()
        BootTelemetryLogger.getInstance().reset(true)
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.DOWNLOAD_COUNT, "0")
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.DOWNLOAD_COUNT_START_DATE, Date().time.toString())
        clearClientLogs()

    }

    private fun clearClientLogs() {
        try {
            ClientLogging.outputFile.writeText("")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    @Throws(SecurityException::class)
    fun deleteDirectoryRecursively(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()) {
                deleteDirectoryRecursively(child)
            }
        }

        //Not deleting db files here as it hard to recreate the files again.
        // Noticed that Room not able to create the user_db again. Hence
        // new content fetched are not saved correctly.
        // line appDatabase.clearAllTables() would deal on clearing user data
        if (!fileOrDirectory.name.contains("user_db") &&
                !fileOrDirectory.name.contains("exoplayer_internal")) {
            fileOrDirectory.delete()
        }
    }

    fun getUserDetails() {
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.UMS().userDetail()
            response.result?.let {
                userDetails.postValue(it)
            }
            response.error?.let {
                error.postValue(it)
            }
        }
    }

    fun updateUser(name: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.UMS().updateUser(name)
            response.let {
                userUpdateResponse.postValue(Pair(it.result, it.error))
            }
        }
    }


    fun downloadUserDataState(username: String) {
        val date: Date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd/MM/yyyy")
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.UMS().userDetail()
            loading.postValue(false)
            response.result?.let {
                when (it.dataExportRequestStatus) {
                    DATAEXPORT_STATUS_NotInitialized -> {
                        exportDataNewRequest.postValue(true)
                    }
                    DATAEXPORT_STATUS_ExportedDataNotified -> {
                        val exportValididtyDate =
                            formatter.parse(it.dataExportedBy?.dataExportResult?.expiresOn!!.split(".")[0])
                        val exportUrl = it.dataExportedBy?.dataExportResult?.exportedDataUrl
                        if (exportValididtyDate > date) {
                            exportDataReadyToDownload.postValue(Pair(exportUrl,
                                targetFormat.format(exportValididtyDate)))
                        } else {
                            exportDataNewRequest.postValue(true)
                        }
                    }
                    else -> {
                        exportDataRequestSubmitted.postValue(true)
                    }
                }
            }
            response.error?.let {
                if (response.code != 404)
                    error.postValue(it)
            }
        }
    }

    fun createDataExportRequest(username: String) {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.UMS().requestDataExport(username)
            loading.postValue(false)
            response.let {
                if (response.success)
                    exportDataNewRequestResponse.postValue(true)
            }
            response.error?.let {
                exportDataNewRequestResponse.postValue(false)
                error.postValue(it)
            }
        }
    }

    fun deleteAccount(username: String) {
        loading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            val response = BineAPI.UMS().deleteUser(username)
            loading.postValue(false)
            if(response.success) {
                deleteAccountSuccess.postValue(true)
                return@launch
            }
            response.error?.let {
                when(it) {
                    Error.NETWORK_ERROR -> error.postValue(it)
                    else -> {
                        response.details?.let { details->
                            errorString.postValue(details)
                        } ?:
                        error.postValue(it)
                    }
                }
            }
        }
    }
}