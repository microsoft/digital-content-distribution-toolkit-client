package com.msr.bine_sdk.cloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HubListResponse {
    @SerializedName("details")
    List<Hub> hubList= new ArrayList<Hub>();

    @SerializedName("contacts")
    List<HubContactModel> contactModelsList=new ArrayList<>();

    @SerializedName("image_url")
    String imageBaseURl;

    public List<HubContactModel> getContactModelsList() {
        return contactModelsList;
    }

    public void setContactModelsList(List<HubContactModel> contactModelsList) {
        this.contactModelsList = contactModelsList;
    }

    public List<Hub> getHubList() {
        return hubList;
    }

    public void setHubList(List<Hub> hubList) {
        this.hubList = hubList;
    }

    public String getImageBaseURl() {
        return imageBaseURl;
    }

    public void setImageBaseURl(String imageBaseURl) {
        this.imageBaseURl = imageBaseURl;
    }
}
