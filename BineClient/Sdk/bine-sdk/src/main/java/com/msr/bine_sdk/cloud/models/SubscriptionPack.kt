// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

data class SubscriptionPack(var id: String,
                            var contentProviderId: String,
                            var type: String,
                            var title: String,
                            var durationDays: Int,
                            var price: Int,
                            var startDate: String,
                            var endDate: String,
                            var isRedeemable: Boolean,
                            var redemptionValue: Int,
                            var subscriptionType: String,
                            var contentIds: List<String>)
