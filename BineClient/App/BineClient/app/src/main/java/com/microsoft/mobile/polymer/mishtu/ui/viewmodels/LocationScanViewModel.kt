// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationScanViewModel(application: Application): AndroidViewModel(application) {

    var error = MutableLiveData<Error>()
    var hubExist = MutableLiveData<Boolean>()
    var progress = MutableLiveData<Boolean>()

    fun checkRetailersWithHubExist(
        latitude: Double,
        longitude: Double,
        radius: Double) {
        CoroutineScope(Dispatchers.Default).launch {
            val responseData = BineAPI.RMS().getNearbyRetailer(radius, latitude, longitude)//20 KMs //17.429228, 78.340927
            responseData.result?.let {

                if(it.isEmpty()){
                    hubExist.postValue(false)
                }
                else {
                    var exist = false
                    for (retailer in it) {
                        val deviceList = retailer.retailer.deviceAssignments
                        if (deviceList.isNotEmpty() && deviceList[0].isActive) {
                            exist = true
                            break
                        }
                    }
                    hubExist.postValue(exist)
                }
            }
            responseData.error?.let {
                //No retailers OR Error
                if (responseData.code == 404) hubExist.postValue(false)
                else error.postValue(it)
            }
        }
    }
}