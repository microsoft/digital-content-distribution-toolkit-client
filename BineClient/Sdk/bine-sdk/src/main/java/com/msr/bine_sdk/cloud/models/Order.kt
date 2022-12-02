// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

data class Order(val id: String,
                 val phoneNumber: String,
                 val userId: String,
                 val userName: String,
                 val retailerPhoneNumber: String,
                 val retailerId: String,
                 val retailerName: String,
                 val orderItems: List<Subscription>,
                 val orderStatus: String,
                 val orderCreatedDate: String) {
    data class Subscription(val amountCollected: Int,
                            val planStartDate: String,
                            val planEndDate: String,
                            val partnerReferenceNumber: String?,
                            val subscription: SubscriptionPack)
}
