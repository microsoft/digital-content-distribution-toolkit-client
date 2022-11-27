package com.msr.bine_sdk.cloud.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HubContactModel implements Parcelable {
    public String deviceid;
    public String phone;

    protected HubContactModel(Parcel in) {
        deviceid = in.readString();
        phone = in.readString();
    }

    public static final Creator<HubContactModel> CREATOR = new Creator<HubContactModel>() {
        @Override
        public HubContactModel createFromParcel(Parcel in) {
            return new HubContactModel(in);
        }

        @Override
        public HubContactModel[] newArray(int size) {
            return new HubContactModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(deviceid);
        parcel.writeString(phone);
    }
}
