package com.msr.bine_sdk.cloud.models

data class Retailer(val id: String,
                    val phoneNumber: String,
                    val name: String,
                    val address: Address,
                    val deviceAssignments: List<Device>
) {
    data class Address(val address1: String,
                       val address2: String,
                       val address3: String,
                       val city: String,
                       val state: String,
                       val pinCode: String,
                       val mapLocation: HubLocation
    )
    data class Device(val deviceId: String,
                                 val isActive: Boolean)
}
