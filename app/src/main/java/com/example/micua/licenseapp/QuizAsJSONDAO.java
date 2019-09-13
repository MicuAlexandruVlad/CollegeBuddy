package com.example.micua.licenseapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface QuizAsJSONDAO {
    @Insert
    void insertQuiz(QuizAsJSON quiz);

    @Delete
    void removeQuiz(QuizAsJSON quiz);

    @Query("SELECT * FROM QuizAsJSON")
    List<QuizAsJSON> getAllQuizzes();

    @Query("SELECT * FROM QuizAsJSON where published = :published")
    List<QuizAsJSON> getPublishedQuizes(int published);

    @Query("SELECT * FROM QuizAsJSON where published = :published")
    List<QuizAsJSON> getUnpublishedQuizes(int published);

    @Query("SELECT * FROM QuizAsJSON where published like :groupAccessToken")
    List<QuizAsJSON> getQuizesForGroup(String groupAccessToken);

    @Query("SELECT * FROM QuizAsJSON where id = :id")
    List<QuizAsJSON> getQuizzesForId(int id);

    @Query("SELECT * FROM QuizAsJSON where referencedUserEmail = :email")
    List<QuizAsJSON> getQuizzesForUser(String email);
}
