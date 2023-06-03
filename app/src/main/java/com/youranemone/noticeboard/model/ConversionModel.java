package com.youranemone.noticeboard.model;

public class ConversionModel {
    String id;
    String image;
    String name;

    String adsTitle;
    String adsAdress;
    String adsImageId;

    public String getAdsTitle() {
        return adsTitle;
    }

    public void setAdsTitle(String adsTitle) {
        this.adsTitle = adsTitle;
    }

    public String getAdsAdress() {
        return adsAdress;
    }

    public void setAdsAdress(String adsAdress) {
        this.adsAdress = adsAdress;
    }

    public String getAdsImageId() {
        return adsImageId;
    }

    public void setAdsImageId(String adsImageId) {
        this.adsImageId = adsImageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
