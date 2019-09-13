package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class Repository {
    public static final String DB_NAME = "db";
    private static final String TAG = "Repository";
    private Database database;

    public Repository (Context context) {
        database = Database.getInstance(context);
    }

    @SuppressLint("StaticFieldLeak")
    public long insertMessage(final Message message) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                if (message.getType() == 3)
                    Log.d(TAG, "doInBackground: inserting quiz");
                Log.d(TAG, "doInBackground: message id -> " + message.getId());
                return database.messageDAO().insertMessage(message);
            }

            @Override
            protected void onPostExecute(Long id) {
                super.onPostExecute(id);
                message.setId(id.intValue());
                Log.d(TAG, "onPostExecute: id -> " + message.getId());
            }
        }.execute();
        return -1;
    }

    @SuppressLint("StaticFieldLeak")
    public void insertQuiz(final QuizAsJSON quiz) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                database.quizAsJSONDAO().insertQuiz(quiz);
                return null;
            }
        }.execute();
    }

    public List<Message> getMessages() {
        return database.messageDAO().getMessages();
    }

    public List<QuizAsJSON> getQuizzes() {
        return database.quizAsJSONDAO().getAllQuizzes();
    }

    public List<QuizAsJSON> getQuizForId(int id) {
        return database.quizAsJSONDAO().getQuizzesForId(id);
    }

    public List<QuizAsJSON> getQuizzesForUser(String email) {
        return database.quizAsJSONDAO().getQuizzesForUser(email);
    }

    public Message getLastMessageForGroup(String groupId) {
        return database.messageDAO().getLastMessageForGroup(groupId);
    }

    public void removeQuiz(QuizAsJSON quizAsJSON) {
        database.quizAsJSONDAO().removeQuiz(quizAsJSON);
    }

    public  void updateImagePath(String path, int id) {
        database.messageDAO().updateImagePath(path, id);
    }

    public  void updateLocalFileData(String data, int id) {
        database.messageDAO().updateLocalFileData(data, id);
    }
}
