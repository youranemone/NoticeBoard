package com.youranemone.noticeboard;

public class UserParams {
    private String imageId;
    private String username;
    private String phone_number;
    private String uid;
    private String email;

    public String geteMail() {
        return email;
    }

    public void seteMail(String eMail) {
        this.email = eMail;
    }

    public String getuID() {
        return uid;
    }

    public void setuID(String uID) {
        this.uid = uID;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
