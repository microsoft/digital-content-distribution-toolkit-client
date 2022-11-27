package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper

data class BadgeEvent(var eventType: NotificationBadgeHelper.BadgeType, var count: Int, var active: Boolean)
