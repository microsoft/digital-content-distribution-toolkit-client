package com.microsoft.mobile.polymer.mishtu.storage.entities.snappy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Notification implements Serializable {
    @SerializedName("type")
    private String type;
    @SerializedName("title")
    private String title;
    @SerializedName("subTitle")
    private String subTitle;
    @SerializedName("imageURL")
    private String imageURL;
    @SerializedName("isRead")
    private boolean isRead;
    @SerializedName("dataId")
    private String dataId;

    public Notification() {
    }

    public Notification(String type, String title, String subTitle, String imageURL, boolean isRead, String dataId) {
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
        this.imageURL = imageURL;
        this.isRead = isRead;
        this.dataId = dataId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
