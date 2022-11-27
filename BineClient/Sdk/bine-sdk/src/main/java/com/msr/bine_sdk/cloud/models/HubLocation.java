package com.msr.bine_sdk.cloud.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HubLocation implements Parcelable {
    @SerializedName("latitude")
    @Expose
    public double latitude;
    @SerializedName("longitude")
    @Expose
    public double longitude;

    public HubLocation() {
    }

    protected HubLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HubLocation> CREATOR = new Creator<HubLocation>() {
        @Override
        public HubLocation createFromParcel(Parcel in) {
            return new HubLocation(in);
        }

        @Override
        public HubLocation[] newArray(int size) {
            return new HubLocation[size];
        }
    };
}
