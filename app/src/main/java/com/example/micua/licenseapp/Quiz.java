package com.example.micua.licenseapp;

import java.io.Serializable;
import java.util.List;

public class Quiz implements Serializable {
    private String name;
    private String description;
    private String createdBy;
    private String createdByName;
    private String timerValue;
    private String createdOn;
    private String idToReference;
    private List<Question> questionsList;
    private List<String> categories;
    private List<String> explanation;
    private boolean timed;
    private boolean limitedTimer;
    private int numQuestions;

    // 0 - low
    // 1 - medium
    // 2 - high
    private int difficulty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getTimerValue() {
        return timerValue;
    }

    public void setTimerValue(String timerValue) {
        this.timerValue = timerValue;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getIdToReference() {
        return idToReference;
    }

    public void setIdToReference(String idToReference) {
        this.idToReference = idToReference;
    }

    public List<Question> getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(List<Question> questionsList) {
        this.questionsList = questionsList;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
    }

    public boolean isTimed() {
        return timed;
    }

    public void setTimed(boolean timed) {
        this.timed = timed;
    }

    public boolean isLimitedTimer() {
        return limitedTimer;
    }

    public void setLimitedTimer(boolean limitedTimer) {
        this.limitedTimer = limitedTimer;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions = numQuestions;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
