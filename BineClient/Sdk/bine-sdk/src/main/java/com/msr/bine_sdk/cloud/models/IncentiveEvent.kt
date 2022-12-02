// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

data class IncentiveEvent(var eventAggregateResponses: List<Event>,
                          var totalValue: Int) {

    data class Event(
        var id: String,
        var aggregratedValue: Int,
        var eventType: String,
        var eventSubType: String,
        var ruleType: String)
}
