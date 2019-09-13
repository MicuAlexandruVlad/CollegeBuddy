package com.example.micua.licenseapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JoinPrivateGroupActivity extends AppCompatActivity {

    private Intent parentIntent;
    private User currentUser;
    private Faculty currentFaculty;
    private DBLinks dbLinks;

    private EditText accessToken;
    private Button join;

    public static final String TAG = "JoinPrivate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_private_group);

        parentIntent = getIntent();
        currentFaculty = (Faculty) parentIntent.getSerializableExtra("currentFaculty");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        dbLinks = new DBLinks();

        join = findViewById(R.id.btn_join_group);
        accessToken = findViewById(R.id.et_access_token);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessToken.getText().toString().equals(""))
                    Toast.makeText(JoinPrivateGroupActivity.this,
                            "Token field is empty", Toast.LENGTH_SHORT).show();
                else if (currentUser.getJoinedGroupsAccessTokens().contains(accessToken.getText().toString()))
                    Toast.makeText(JoinPrivateGroupActivity.this,
                            "You are already part of this group", Toast.LENGTH_SHORT).show();
                else {
                    String token = accessToken.getText().toString();
                    String userId = currentUser.getId();
                    String userTokens = currentUser.getJoinedGroupsAccessTokens() + "!@!" + token + "!@!";
                    String url = dbLinks.getBaseLink() +
                            "join-private-group?token=" + token + "&userId=" + userId
                            + "&userTokens=" + userTokens;
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Call call = client.newCall(request);

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String json = response.body().string();

                            Log.d(TAG, "onResponse: json -> " + json);

                            try {
                                JSONObject res = new JSONObject(json);
                                boolean userUpdated = res.getBoolean("userUpdated");
                                boolean groupUpdated = res.getBoolean("groupUpdated");

                                if (userUpdated && groupUpdated) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog
                                                    .Builder(JoinPrivateGroupActivity.this);
                                            builder.setTitle("Success")
                                                    .setMessage("Your data has been successfully updated. " +
                                                            "Please log out and" +
                                                            " log back in to see the group that you have joined")
                                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    });
                                            builder.create();
                                            builder.show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showSuccessDialog(Group group, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(JoinPrivateGroupActivity.this)
                        .setTitle("Success")
                        .setMessage(message)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(JoinPrivateGroupActivity.this)
                        .setTitle("Oops")
                        .setMessage("Something went wrong. Please try again.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void showInvalidAccessTokenDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(JoinPrivateGroupActivity.this)
                        .setTitle("Error")
                        .setMessage("The access token you have entered " +
                                "does not exist. Please check it and then " +
                                "try again")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void showAlreadyMemberDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(JoinPrivateGroupActivity.this)
                .setTitle("Error")
                .setMessage("You are already part of this group.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private void showSetupCompleteDialog(final Group group) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(JoinPrivateGroupActivity.this)
                .setTitle("Congratulations")
                .setMessage("You have completed the initial setup. You can now fully experience" +
                        " <<AppName>>.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(JoinPrivateGroupActivity.this, MainActivity.class);
                        intent.putExtra("currentGroup", (Serializable) group);
                        intent.putExtra("currentUser", currentUser);
                        intent.putExtra("currentFaculty", currentFaculty);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }
}
