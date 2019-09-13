package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShareQuizDialog extends Dialog {
    public static final String TAG = "ShareQuizDialog";

    private RecyclerView groupsRV;
    private Button share, cancel;

    private List<Group> groups;
    private User currentUser;
    private String quizJSON;
    private ShareQuizDialogAdapter adapter;
    private boolean isSharePressed = false;
    private Socket socket;
    private Repository repository;
    private Constants constants;
    private DBLinks dbLinks;

    public ShareQuizDialog(@NonNull Context context, User currentUser) {
        super(context);
        this.currentUser = currentUser;
        this.dbLinks = new DBLinks();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share_quiz);

        groupsRV = findViewById(R.id.rv_groups);
        share = findViewById(R.id.btn_share);
        cancel = findViewById(R.id.btn_cancel);

        groups = new ArrayList<>();
        adapter = new ShareQuizDialogAdapter(getContext(), groups);
        groupsRV.setAdapter(adapter);
        groupsRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!adapter.isGroupSelected())
                    Toast.makeText(getContext(), "No group selected", Toast.LENGTH_SHORT).show();
                else {
                    isSharePressed = true;

                    try {
                        socket = IO.socket(dbLinks.getBaseLink());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Group group = groups.get(adapter.getSelectedGroupPosition());
                    emitQuiz(group, quizJSON);

                    dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void emitQuiz(Group group, String quizJSON) {
        socket.connect();
        //socket.emit("chat", "user-connected");

        repository = new Repository(getContext());
        constants = new Constants();

        Log.d(TAG, "emitQuiz: group id -> " + group.getId());

        Message message = new Message();
        message.setMessage("Sent a quiz");
        message.setReferencedGroupId(group.getId());
        message.setSentBy(currentUser.getFirstName() + " " + currentUser.getLastName());
        message.setReferencedUserEmail(currentUser.getEmail());
        message.setTimestamp(getCurrentTime());
        message.setQuizData(quizJSON);
        message.setAlphaNumeric(getAlphaNumeric(16));
        message.setType(constants.MESSAGE_QUIZ);
        message.setImagePath("");

        Gson gson = new Gson();
        JSONObject jsonObject =  new JSONObject();
        Quiz quiz = gson.fromJson(message.getQuizData(), Quiz.class);

        try {
            jsonObject.put("message", "Sent a quiz");
            jsonObject.put("imagePath", "");
            jsonObject.put("quizData", gson.toJson(quiz));
            jsonObject.put("quizId", message.getQuizId());
            jsonObject.put("referencedUserEmail", message.getReferencedUserEmail());
            jsonObject.put("sentBy", message.getSentBy());
            jsonObject.put("timestamp", message.getTimestamp());
            jsonObject.put("type", message.getType());
            jsonObject.put("alphaNumeric", message.getAlphaNumeric());
            jsonObject.put("referencedGroupId", message.getReferencedGroupId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "emitQuiz: quiz message json -> " + jsonObject.toString());

        socket.emit("chat", jsonObject);
        // Log.d(TAG, "emitQuiz: message json -> " + gson.toJson(message));
        // long id = repository.insertMessage(message);
        // message.setId((int) id);

        Intent intent = new Intent("new-quiz-message");
        intent.putExtra("message", message);
        intent.putExtra("group", (Serializable) group);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private String getCurrentTime() {
        return Calendar.getInstance().getTime().toString();
    }

    public String getAlphaNumeric(int len) {

        char[] ch = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        char[] c = new char[len];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++) {
            c[i] = ch[random.nextInt(ch.length)];
        }

        return new String(c);
    }

    public void setGroups(List<Group> groups) {
        this.groups.addAll(groups);
        Log.d(TAG, "setGroups: groups size -> " + groups.size());
        adapter.notifyDataSetChanged();
    }

    public boolean isSharePressed() {
        return isSharePressed;
    }

    public void setQuiz(String quizJSON) {
        this.quizJSON = quizJSON;
    }
}
