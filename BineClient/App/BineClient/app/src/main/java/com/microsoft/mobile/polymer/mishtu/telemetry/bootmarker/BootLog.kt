// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker

import androidx.annotation.Keep
import androidx.collection.ArrayMap
import com.google.gson.annotations.Expose
import java.util.*

@Keep
data class BootLog(
    @Expose var createTime: Date,
    @Expose val bootType: String,
    @Expose var intermediateMarkers: ArrayMap<String, Date>,
    @Expose val pageLoads: ArrayMap<String, PageLoad>) {

    @Keep
    data class PageLoad(var pageName: String,
                        @Expose var createTime: Date,
                        @Expose var markers: ArrayMap<String, String>,
                        var markersCount: Int)

}
