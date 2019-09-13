package com.example.micua.licenseapp;

import java.io.Serializable;

public class LeaderBoardEntry implements Serializable {
    private String id;

    private String referencedUserId;
    // referencedQuizId = alphaNumeric + "_" + userId;
    private String referencedQuizId;
    private String referencedUserFullName;
    private boolean quizHasTimer;
    private String completionTime;
    private long completionTimeInMillis;
    private double completionPercentage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReferencedUserId() {
        return referencedUserId;
    }

    public void setReferencedUserId(String referencedUserId) {
        this.referencedUserId = referencedUserId;
    }

    public String getReferencedQuizId() {
        return referencedQuizId;
    }

    public void setReferencedQuizId(String referencedQuizId) {
        this.referencedQuizId = referencedQuizId;
    }

    public String getReferencedUserFullName() {
        return referencedUserFullName;
    }

    public void setReferencedUserFullName(String referencedUserFullName) {
        this.referencedUserFullName = referencedUserFullName;
    }

    public boolean doesQuizHaveTimer() {
        return quizHasTimer;
    }

    public void setQuizHasTimer(boolean quizHasTimer) {
        this.quizHasTimer = quizHasTimer;
    }

    public String getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public long getCompletionTimeInMillis() {
        return completionTimeInMillis;
    }

    public void setCompletionTimeInMillis(long completionTimeInMillis) {
        this.completionTimeInMillis = completionTimeInMillis;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}
