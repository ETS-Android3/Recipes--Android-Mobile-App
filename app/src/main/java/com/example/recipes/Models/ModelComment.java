package com.example.recipes.Models;

public class ModelComment {
    String cId, comment, cTime, uid, uEmail, uProfilePicture, uName;

    public ModelComment(){
    }

    public ModelComment(String cId, String comment, String cTime, String uid, String uEmail, String uProfilePicture, String uName) {
        this.cId = cId;
        this.comment = comment;
        this.cTime = cTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uProfilePicture = uProfilePicture;
        this.uName = uName;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getcTime() {
        return cTime;
    }

    public void setcTime(String cTime) {
        this.cTime = cTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuProfilePicture() {
        return uProfilePicture;
    }

    public void setuProfilePicture(String uProfilePicture) {
        this.uProfilePicture = uProfilePicture;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
