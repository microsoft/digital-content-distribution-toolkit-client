// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.telemetry

import java.util.HashMap

data class TelemetryMessage(var DeviceId: String,
                            val MessageType: String = "Telemetry",
                            var MessageSubType: String,
                            var TimeStamp: String,
                            var MessageBody: HashMap<String, String>) {
}