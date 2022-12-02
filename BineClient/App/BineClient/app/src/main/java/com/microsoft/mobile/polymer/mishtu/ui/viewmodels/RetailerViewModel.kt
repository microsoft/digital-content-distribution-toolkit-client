// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.common.base.Strings
import androidx.lifecycle.MutableLiveData
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.DeviceContentDetails
import com.msr.bine_sdk.cloud.models.RetailerDistance
//import com.msr.bine_sdk.cloud.model.RetailerDistance.Retailer
import com.msr.bine_sdk.models.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RetailerViewModel(application: Application): AndroidViewModel(application) {

    var retailers = MutableLiveData<List<RetailerDistance>>()
    var retailerWithSelectedContent = MutableLiveData<List<Pair<RetailerDistance, String>>>()
    var error = MutableLiveData<Error>()


    private fun getRetailerWithHub(retList: List<RetailerDistance>, contentId: String?) {
        val tempRetailerList = ArrayList<RetailerDistance>()
        val deviceIdToRetailerMap = HashMap<String, RetailerDistance>()

        for (ret in retList) {
            val deviceList = ret.retailer.deviceAssignments
            if (!deviceList.isNullOrEmpty() && deviceList[0].isActive) {
                tempRetailerList.add(ret)
                deviceIdToRetailerMap[ret.retailer.deviceAssignments[0].deviceId] = ret
            }
        }
            if(Strings.isNullOrEmpty(contentId)){
               retailers.postValue(tempRetailerList)
            }else{
                getContentCountAtDevice(contentId!!, true, deviceIdToRetailerMap)
            }
   }


    fun getRetailers(
        latitude: Double,
        longitude: Double,
        showRetailerWithHub: Boolean,
        contentId: String?,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val responseData = BineAPI.RMS().getNearbyRetailer(5000.0, latitude, longitude)
            //17.429228, 78.340927
            //Log.d("ResponseData nearby = ", responseData.toString())
            //val responseData = getHardCodedRetailer()
           // Log.d("ResponseData nearby = ", responseData.toString())

            // val list = arrayListOf<Retailer>()

            /*var retailer =  Retailer()
            //retailer.name = "Internet Cafe"
            retailer.phoneNumber = responseData?.result[0]?.retailer?.phoneNumber
            retailer.userName = responseData?.result[0]?.retailer.userName
            // retailer.address.mapLocation =
            //retailer.hubLocation.latitude = 15.4650360
            //retailer.hubLocation.longitude = 73.8167950
            list.add(retailer)*/
            responseData.result?.let {
                if(it.isEmpty()){
                    retailers.postValue(Collections.emptyList())
                    retailerWithSelectedContent.postValue(Collections.emptyList())
                }
                else if (showRetailerWithHub) {
                    getRetailerWithHub(it, contentId)
                } else {
                retailers.postValue(it)
            }
            }
            responseData.error?.let {
                if (responseData.code == 404) retailers.postValue(Collections.emptyList())
                else error.postValue(it)
            }
            //retailers.postValue(responseData.result)
        }
    }



    private fun getContentCountAtDevice(
        contentId: String,
        includeActiveContentCount: Boolean,
        deviceIdToRetailerMap: Map<String, RetailerDistance>,
    ) {
         CoroutineScope(Dispatchers.Default).launch {

            val responseData = BineAPI.Devices().getContentAvailability(contentId,
                deviceIdToRetailerMap.keys.toTypedArray(),
                includeActiveContentCount)
            responseData.result?.let {
                retailerWithSelectedContent(it, deviceIdToRetailerMap)
            }
            responseData.error?.let {
                if (responseData.code == 404) retailerWithSelectedContent.postValue(Collections.emptyList())
                else error.postValue(it)
            }
        }
}

    private fun retailerWithSelectedContent(
        contentCountResponse: Array<DeviceContentDetails>,
        deviceIdToRetailerMapLiveData: Map<String, RetailerDistance>,
    ) {

        val retailerWithSelectedCon: ArrayList<Pair<RetailerDistance, String>> = ArrayList()

        for (contentCount in contentCountResponse) {
            val deviceId = contentCount.deviceId
            val retailerDistance = deviceIdToRetailerMapLiveData[deviceId]
            if(retailerDistance!= null) {
                retailerWithSelectedCon.add(Pair(retailerDistance, contentCount.activeContentCount))
            }
        }
        retailerWithSelectedCon.sortBy { it.first.distanceMeters  }
        retailerWithSelectedContent.postValue(retailerWithSelectedCon)
    }
}

