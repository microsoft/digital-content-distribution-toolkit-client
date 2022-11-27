package com.msr.bine_sdk.cloud.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Hub implements Parcelable {
    public HubLocation hubLocation = new HubLocation();
    @SerializedName("uid")
    @Expose
    public Integer uid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("shop_name")
    @Expose
    public String shopName;
    @SerializedName("shop_address")
    @Expose
    public String shopAddress;
    @SerializedName("deviceid")
    @Expose
    public String id;
    @SerializedName("properties")
    @Expose
    public Map<String, String> properties;
    @SerializedName("uuid")
    @Expose
    public String uuid;
    @SerializedName("hub_picture")
    @Expose
    public String hubPicture;
    @SerializedName("location")
    @Expose
    public String location;
    @SerializedName("rating")
    @Expose
    public double rating;
    @SerializedName("created_on")
    @Expose
    public String createdOn;
    @SerializedName("ip")
    @Expose
    public String ip;
    @SerializedName("tags")
    @Expose
    public String tags;
    @SerializedName("payment_options")
    @Expose
    public String paymentOptions;
    @SerializedName("wifi_5G_SSID")
    @Expose
    public String wifi5GSSID;
    @SerializedName("wifi_5G_pass")
    @Expose
    public String wifi5GPass;
    @SerializedName("wifi_2G_SSID")
    @Expose
    public String wifi2GSSID;
    @SerializedName("wifi_2G_pass")
    @Expose
    public String wifi2GPass;

    public String mobile;

    public boolean isFav = false;
    public int distance;

    public Hub() {
    }

    protected Hub(Parcel in) {
        if (in.readByte() == 0) {
            uid = null;
        } else {
            uid = in.readInt();
        }
        name = in.readString();
        shopName = in.readString();
        shopAddress = in.readString();
        id = in.readString();
        uuid = in.readString();
        hubPicture = in.readString();
        location = in.readString();
        rating = in.readDouble();
        createdOn = in.readString();
        ip = in.readString();
        tags = in.readString();
        paymentOptions = in.readString();
        wifi5GSSID = in.readString();
        wifi5GPass = in.readString();
        wifi2GSSID = in.readString();
        wifi2GPass = in.readString();
        mobile = in.readString();
        isFav = in.readByte() != 0;
    }

    public static final Creator<Hub> CREATOR = new Creator<Hub>() {
        @Override
        public Hub createFromParcel(Parcel in) {
            return new Hub(in);
        }

        @Override
        public Hub[] newArray(int size) {
            return new Hub[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (uid == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(uid);
        }
        dest.writeString(name);
        dest.writeString(shopName);
        dest.writeString(shopAddress);
        dest.writeString(id);
        dest.writeString(uuid);
        dest.writeString(hubPicture);
        dest.writeString(location);
        dest.writeDouble(rating);
        dest.writeString(createdOn);
        dest.writeString(ip);
        dest.writeString(tags);
        dest.writeString(paymentOptions);
        dest.writeString(wifi5GSSID);
        dest.writeString(wifi5GPass);
        dest.writeString(wifi2GSSID);
        dest.writeString(wifi2GPass);
        dest.writeString(mobile);
        dest.writeByte((byte) (isFav ? 1 : 0));
    }

    public HubLocation getHubLocation() {
        if(!TextUtils.isEmpty(location)){
            Gson gson = new Gson();
            return gson.fromJson(location, HubLocation.class);
        }
        return hubLocation;
    }
}
