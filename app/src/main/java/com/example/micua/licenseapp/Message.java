package com.example.micua.licenseapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Message implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String message;
    private String sentBy;
    private String referencedGroupId;
    private String referencedUserEmail;
    private String timestamp;
    private String imagePath;
    private String quizData;
    private String imageData;
    private String localFileData;
    private String fileExtension;
    private String alphaNumeric;
    private int quizId;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getReferencedGroupId() {
        return referencedGroupId;
    }

    public void setReferencedGroupId(String referencedGroupId) {
        this.referencedGroupId = referencedGroupId;
    }

    public String getReferencedUserEmail() {
        return referencedUserEmail;
    }

    public void setReferencedUserEmail(String referencedUserEmail) {
        this.referencedUserEmail = referencedUserEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getQuizData() {
        return quizData;
    }

    public void setQuizData(String quizData) {
        this.quizData = quizData;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getLocalFileData() {
        return localFileData;
    }

    public void setLocalFileData(String localFileData) {
        this.localFileData = localFileData;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getAlphaNumeric() {
        return alphaNumeric;
    }

    public void setAlphaNumeric(String alphaNumeric) {
        this.alphaNumeric = alphaNumeric;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
