package com.example.recipes.Models;

public class ModelPost {
    String uid, uName, uEmail, uProfilePicture, pId, pRecipeName, pIngredients, pInstructions, pImage, pTime, pLikes, pComments;

    public ModelPost() {
    }

    public ModelPost(String uid, String uName, String uEmail, String uProfilePicture, String pId, String pRecipeName, String pIngredients, String pInstructions, String pImage, String pTime, String pLikes, String pComments) {
        this.uid = uid;
        this.uName = uName;
        this.uEmail = uEmail;
        this.uProfilePicture = uProfilePicture;
        this.pId = pId;
        this.pRecipeName = pRecipeName;
        this.pIngredients = pIngredients;
        this.pInstructions = pInstructions;
        this.pImage = pImage;
        this.pTime = pTime;
        this.pLikes = pLikes;
        this.pComments = pComments;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
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

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpRecipeName() {
        return pRecipeName;
    }

    public void setpRecipeName(String pRecipeName) {
        this.pRecipeName = pRecipeName;
    }

    public String getpIngredients() {
        return pIngredients;
    }

    public void setpIngredients(String pIngredients) {
        this.pIngredients = pIngredients;
    }

    public String getpInstructions() {
        return pInstructions;
    }

    public void setpInstructions(String pInstructions) {
        this.pInstructions = pInstructions;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }
}