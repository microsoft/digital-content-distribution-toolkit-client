// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

data class User(val id : String,
                val phoneNumber : String,
                val userName : String,
                val name : String,
                val channelId : String,
                val type : String,
                val dataExportRequestStatus: String,
                val dataExportedBy: DataExportedBy,
                val referralInfo: ReferralInfo?){

    data class DataExportedBy(val dataExportResult: DataExportResult){
            data class DataExportResult(val exportedDataUrl: String,
                                        val expiresOn: String,
                                        val notificationSent: Boolean)
    }

    data class ReferralInfo(val retailerUserId: String?,
                            val retailerReferralCode: String?,
                            val retailerPartnerCode: String?)
}
