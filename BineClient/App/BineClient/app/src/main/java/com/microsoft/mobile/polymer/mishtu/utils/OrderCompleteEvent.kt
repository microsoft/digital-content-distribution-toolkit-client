package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription

data class OrderCompleteEvent(val subscriptionId: String?, var eventType: BOConverter.EventType)