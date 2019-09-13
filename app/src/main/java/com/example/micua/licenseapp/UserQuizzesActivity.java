package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class UserQuizzesActivity extends AppCompatActivity {
    public static final String TAG = "UserQuizzesActivity";

    private RecyclerView quizzesRV;

    private Intent parentIntent;
    private User currentUser;
    private Repository repository;
    private UserQuizzesAdapter quizzesAdapter;
    private List<Quiz> quizList;
    private List<QuizAsJSON> quizAsJSONList;
    private List<Group> groups;
    private Socket socket;
    private DBLinks dbLinks;
    private Constants constants;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_quizzes);

        Objects.requireNonNull(getSupportActionBar()).hide();

        quizzesRV = findViewById(R.id.rv_quiz_list);

        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        repository = new Repository(this);
        dbLinks = new DBLinks();
        constants = new Constants();
        quizList = new ArrayList<>();
        groups = new ArrayList<>();
        quizAsJSONList = new ArrayList<>();
        quizzesAdapter = new UserQuizzesAdapter(quizList, this, currentUser, quizAsJSONList);

        groups = (List<Group>) parentIntent.getSerializableExtra("group_list");
        quizzesAdapter.setGroups(groups);

        try {
            socket = IO.socket(dbLinks.getBaseLink());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getQuizzes();
            }
        }).start();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        quizzesRV.setLayoutManager(layoutManager);
        quizzesRV.setAdapter(quizzesAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int i = intent.getIntExtra("quiz_index", -1);
                //quizAsJSONList.remove(i);
                Log.d(TAG, "onReceive: quizAsJsonList size -> " + quizAsJSONList.size());
                Log.d(TAG, "onReceive: quizList size -> " + quizList.size());
            }
        }, new IntentFilter("on-quiz-removed"));

        /*LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Group group = (Group) intent.getSerializableExtra("group");
                String quizJSON = intent.getStringExtra("quiz_json");
                Log.d(TAG, "onReceive: group to share quiz -> " + group.getGroupName());
                Log.d(TAG, "onReceive: shared quiz data -> " + quizJSON);

                emitQuiz(group, quizJSON);

            }
        }, new IntentFilter("share-quiz"));*/
    }

    // TODO: still need to emit the quiz...i guess...or maybe it is send by the dialog

    private void emitQuiz(Group group, String quizJSON) {
        socket.connect();
        //socket.emit("chat", "user-connected");

        Message message = new Message();
        message.setMessage("");
        message.setReferencedGroupId(group.getId());
        message.setSentBy(currentUser.getFirstName() + " " + currentUser.getLastName());
        message.setReferencedUserEmail(currentUser.getEmail());
        message.setTimestamp(getCurrentTime());
        message.setQuizData(quizJSON);
        message.setType(constants.MESSAGE_QUIZ);
        message.setImagePath("");

        Gson gson = new Gson();
        socket.emit("chat", gson.toJson(message));
        Log.d(TAG, "emitQuiz: message json -> " + gson.toJson(message));
        repository.insertMessage(message);

        Intent intent = new Intent("new-quiz-message");
        intent.putExtra("message", message);
        intent.putExtra("group", (Serializable) group);
        LocalBroadcastManager.getInstance(UserQuizzesActivity.this).sendBroadcast(intent);
    }

    private String getCurrentTime() {
        return Calendar.getInstance().getTime().toString();
    }

    @SuppressLint("StaticFieldLeak")
    private void getQuizzes() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                quizAsJSONList = repository.getQuizzesForUser(currentUser.getEmail());
                quizzesAdapter.setQuizAsJSONList(quizAsJSONList);
                Log.d(TAG, "doInBackground: quizAsJsonList size -> " + quizAsJSONList.size());
                Log.d(TAG, "doInBackground: quizzes -> " + quizAsJSONList.size());
                createQuizList(quizAsJSONList);
                return null;
            }
        }.execute();
    }

    private void createQuizList(List<QuizAsJSON> quizAsJSONList) {
        Gson gson = new Gson();
        for (int i = 0; i < quizAsJSONList.size(); i++) {
            QuizAsJSON quizAsJSON = quizAsJSONList.get(i);
            String json = quizAsJSON.getQuizJson();
            Log.d(TAG, "createQuizList: quizAsJsonId -> " + quizAsJSON.getId());
            quizList.add(gson.fromJson(json, Quiz.class));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    quizzesAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 192 && resultCode == RESULT_OK) {
            if (data != null) {
                Log.d(TAG, "onActivityResult: got back");
                Quiz quiz;
                quiz = (Quiz) data.getSerializableExtra("edited_quiz");
                QuizAsJSON quizAsJSON;
                quizAsJSON = (QuizAsJSON) data.getSerializableExtra("edited_quiz_as_json");
                final int editedQuizIndex = data.getIntExtra("quiz_index", -1);
                Log.d(TAG, "onActivityResult: quiz index value -> " + editedQuizIndex);
                if (editedQuizIndex >= 0) {
                    Log.d(TAG, "onActivityResult: inside if");
                    final QuizAsJSON quizAsJSON1 = quizAsJSONList.get(editedQuizIndex);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            repository.removeQuiz(quizAsJSON1);
                        }
                    }).start();
                    Log.d(TAG, "onReceive: before remove quizAsJsonList size -> " + quizAsJSONList.size());
                    Log.d(TAG, "onReceive: before remove quizList size -> " + quizList.size());
                    quizAsJSONList.remove(editedQuizIndex);
                    quizList.remove(editedQuizIndex);
                    quizzesAdapter.notifyDataSetChanged();
                    repository.insertQuiz(quizAsJSON);
                    Log.d(TAG, "onReceive: before add quizAsJsonList size -> " + quizAsJSONList.size());
                    Log.d(TAG, "onReceive: before add quizList size -> " + quizList.size());
                    quizAsJSONList.add(editedQuizIndex, quizAsJSON);
                    quizList.add(editedQuizIndex, quiz);
                    quizzesAdapter.notifyDataSetChanged();
                    quizzesAdapter.setQuizAsJSONList(quizAsJSONList);
                    Log.d(TAG, "onReceive: after add quizAsJsonList size -> " + quizAsJSONList.size());
                    Log.d(TAG, "onReceive: after add quizList size -> " + quizList.size());
                }
            }
        }
    }
}
