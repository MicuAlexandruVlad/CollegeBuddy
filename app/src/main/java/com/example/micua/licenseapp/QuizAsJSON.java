package com.example.micua.licenseapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class QuizAsJSON implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String quizJson;
    private String referencedGroupTokens;
    private String referencedUserEmail;
    // 0 - not published
    // 1 - published
    private int published;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuizJson() {
        return quizJson;
    }

    public void setQuizJson(String quizJson) {
        this.quizJson = quizJson;
    }

    public String getReferencedGroupTokens() {
        return referencedGroupTokens;
    }

    public void setReferencedGroupTokens(String referencedGroupTokens) {
        this.referencedGroupTokens = referencedGroupTokens;
    }

    public String getReferencedUserEmail() {
        return referencedUserEmail;
    }

    public void setReferencedUserEmail(String referencedUserEmail) {
        this.referencedUserEmail = referencedUserEmail;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }
}
