package com.msr.bine_sdk.cloud.models

import com.google.gson.annotations.SerializedName

data class DataExportResponse( var createdByUserId: String,
                               var modifiedByByUserId: String,
                               var createdDate: String,
                               var modifiedDate: String,
                               var id: String,
                               var phoneNumber: String,
                               var type: String,
                               var status: String,
                               @SerializedName("result") var exportResult:Result?) {

    data class Result( var dateCompleted: String,
                       var exportedDataUrl: String,
                       var exportedDataValidity: String)
}
