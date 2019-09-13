package com.example.micua.licenseapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddGroupActivity extends AppCompatActivity {

    private LinearLayout accessTokenHolder;
    private TextView accessTokenTv;
    private ImageView copyAccessToken;
    private Button addGroup;
    private EditText groupName, groupDescription;
    private RadioGroup groupTypeRg;
    private RadioButton privateRb;
    private RadioButton publicRb;

    private Intent parentIntent;
    private User currentUser;
    private Faculty currentFaculty;
    private Group currentGroup;
    private DBLinks dbLinks;
    private String accessToken;
    private boolean isPrivate = false;

    public static final String TAG = "AddGroup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        parentIntent = getIntent();
        dbLinks = new DBLinks();
        currentFaculty = (Faculty) parentIntent.getSerializableExtra("currentFaculty");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        currentGroup = new Group();

        bindViews();
        accessTokenHolder.setVisibility(View.GONE);

        accessToken = generateAccessToken();

        publicRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrivate = !b;
                if (b)
                    accessTokenHolder.setVisibility(View.GONE);
            }
        });

        privateRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrivate = b;
                if (b) {
                    accessTokenHolder.setVisibility(View.VISIBLE);
                    accessTokenTv.setText(accessToken);
                }

            }
        });

        copyAccessToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard(accessToken);
            }
        });

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentGroup.setAccessToken(accessToken);
                Log.d(TAG, "Access Token: " + accessToken);
                if (groupName.getText().toString().equalsIgnoreCase(""))
                    Toast.makeText(AddGroupActivity.this, "Field with group name is empty"
                            , Toast.LENGTH_SHORT).show();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this)
                            .setTitle("Group Administrator")
                            .setMessage("Upon adding this group you will become it's administrator. " +
                                    "You can always add a secondary administrator in the group settings." +
                                    " Are you sure you want to proceed ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    currentGroup.setGroupName(groupName.getText().toString());
                                    if (isPrivate)
                                        currentGroup.setGroupType("private");
                                    else
                                        currentGroup.setGroupType("public");
                                    currentGroup.setGroupAdministrator(currentUser.getEmail());
                                    currentGroup.setGroupSecondaryAdministrator("");
                                    currentGroup.setSecondaryAdministrator(false);
                                    currentGroup.setNumMembers(1);
                                    currentGroup.setReferencedCollegeName(currentFaculty.getReferencedCollegeName());
                                    currentGroup.setGroupDescription(groupDescription.getText().toString());
                                    currentGroup.setReferencedFacultyName(currentFaculty.getFacultyName());
                                    currentUser.setJoinedGroup(true);
                                    currentUser.setJoinedGroupsAccessTokens(currentGroup.getAccessToken()
                                     + "!@!" + currentUser.getJoinedGroupsAccessTokens());
                                    postGroup(currentGroup);
                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    private void postGroup(final Group currentGroup) {
        currentFaculty.setNumGroups(currentFaculty.getNumGroups() + 1);
        Log.d(TAG, currentGroup.getGroupName());
        String url = dbLinks.getBaseLink() + "register-group-update-faculty?groupName="
                + currentGroup.getGroupName() + "&groupAdministrator=" + currentGroup.getGroupAdministrator()
                + "&groupSecondaryAdministrator=" + currentGroup.getGroupSecondaryAdministrator()
                + "&hasSecondaryAdministrator=" + currentGroup.hasSecondaryAdministrator()
                + "&numMembers=" +  currentGroup.getNumMembers()
                + "&groupType=" + currentGroup.getGroupType()
                + "&groupAccessToken=" + accessToken
                + "&referencedFacultyName=" + currentGroup.getReferencedFacultyName()
                + "&facultyName=" + currentFaculty.getFacultyName()
                + "&facultyNumGroups=" + currentFaculty.getNumGroups()
                + "&groupDescription=" + currentGroup.getGroupDescription()
                + "&joinedGroup=" + currentUser.isJoinedGroup()
                + "&referencedCollegeName=" + currentFaculty.getReferencedCollegeName()
                + "&joinedGroupsAccessTokens=" + currentUser.getJoinedGroupsAccessTokens()
                + "&email=" + currentUser.getEmail();

        OkHttpClient client1 = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client1.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject object = new JSONObject(json);
                    if (!object.getBoolean("isGroupInDb")) {
                        String id = object.getString("groupId");
                        currentGroup.setId(id);
                        currentGroup.setLastMessage(null);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this)
                                        .setTitle("Success")
                                        .setMessage("Your group has been successfully added to our database.")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                // notify parent activity that a new group was added
                                                // then add it to recyclerView and notify adapter
                                                currentFaculty.setNumGroups(currentFaculty.getNumGroups() + 1);
                                                parentIntent.putExtra("newGroup", (Serializable) currentGroup);
                                                parentIntent.putExtra("modifiedFaculty", currentFaculty);
                                                setResult(RESULT_OK, parentIntent);
                                                finish();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this)
                                        .setTitle("Oops")
                                        .setMessage("Your group already exists our database.")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private String generateAccessToken() {
        String alphaNumeric = getAlphaNumeric(16);
        accessToken = alphaNumeric + "_" + currentFaculty.getReferencedCollegeName()
                + currentFaculty.getFacultyName() + currentFaculty.getNumGroups();
        return accessToken;
    }

    private void bindViews() {
        accessTokenHolder = findViewById(R.id.ll_private_access_token_holder);
        accessTokenTv = findViewById(R.id.tv_group_access_token);
        copyAccessToken = findViewById(R.id.iv_copy_access_token);
        groupName = findViewById(R.id.et_group_name);
        addGroup = findViewById(R.id.btn_add_group_finish);
        groupDescription = findViewById(R.id.et_group_description);
        groupTypeRg = findViewById(R.id.rg_group_type);
        privateRb = findViewById(R.id.rb_private);
        publicRb = findViewById(R.id.rb_public);
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

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("accessToken", text);
        Objects.requireNonNull(clipboard).setPrimaryClip(clip);
        Toast.makeText(this, "Access token copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
