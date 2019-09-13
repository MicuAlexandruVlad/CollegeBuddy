package com.example.micua.licenseapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {
    private String questionText;
    private String explanation;
    private List<String> correctAnswers, incorrectAnswers;
    private int questionType;
    private int numCorrectAnswers;

    public Question() {
        correctAnswers = new ArrayList<>();
        incorrectAnswers = new ArrayList<>();
        this.explanation = "";
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void addIncorrectAnswer(String answer) {
        incorrectAnswers.add(answer);
    }

    public void addCorrectAnswer(String answer) {
        correctAnswers.add(answer);
    }

    public void removeCorrectAnswersAt(int index) {
        correctAnswers.remove(index);
    }

    public void removeIncorrectAnswersAt(int index) {
        incorrectAnswers.remove(index);
    }

    public int getQuestionType() {
        return questionType;
    }

    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    public int getNumCorrectAnswers() {
        return numCorrectAnswers;
    }

    public void setNumCorrectAnswers(int numCorrectAnswers) {
        this.numCorrectAnswers = numCorrectAnswers;
    }

    public void resetAnswers() {
        correctAnswers.clear();
        incorrectAnswers.clear();
    }
}
