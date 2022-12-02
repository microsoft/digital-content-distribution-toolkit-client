// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class DownloadRequest(var id: String,
                           var downloadUri: Uri,
                           var type: Downloader.TYPE,
                           var extras: String): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
            Downloader.TYPE.CUSTOM,
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(downloadUri, flags)
        parcel.writeString(extras)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadRequest> {
        override fun createFromParcel(parcel: Parcel): DownloadRequest {
            return DownloadRequest(parcel)
        }

        override fun newArray(size: Int): Array<DownloadRequest?> {
            return arrayOfNulls(size)
        }
    }
}