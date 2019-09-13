package com.example.micua.licenseapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JoinGroupActivity extends AppCompatActivity {
    public static final int ADD_GROUP_REQ_CODE = 1413;
    public static final String TAG = "JoinGroupActivity";

    private LottieAnimationView loading;
    private RelativeLayout loadingAnimationHolder, contentHolder, noDataHolder;
    private RecyclerView groupsRV;
    private FloatingActionButton addGroup;

    private Intent parentIntent;
    private Faculty currentFaculty;
    private User currentUser;
    private List<Group> groups;
    private DBLinks dbLinks;
    private int facultyIndex;
    private List<Group> newGroups;

    private JoinGroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_faculty);

        loading = findViewById(R.id.l_loading);
        loadingAnimationHolder = findViewById(R.id.rl_loading_animation_holder);
        contentHolder = findViewById(R.id.rl_content_holder);
        noDataHolder = findViewById(R.id.rl_no_data_holder);
        groupsRV = findViewById(R.id.rv_groups);
        addGroup = findViewById(R.id.fab_add_group);

        newGroups = new ArrayList<>();

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Group group = (Group) intent.getSerializableExtra("joinedGroup");
                group.setLastMessage(null);
                newGroups.add(group);
                parentIntent.putExtra("new_groups", (Serializable) newGroups);
                JoinGroupActivity.this.setResult(RESULT_OK, parentIntent);
                Toast.makeText(context, "Joined group " + group.getGroupName(), Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter("group-joined-adapter"));

        parentIntent = getIntent();
        facultyIndex = parentIntent.getIntExtra("facultyPosition", -1);
        currentFaculty = (Faculty) parentIntent.getSerializableExtra("currentFaculty");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        Objects.requireNonNull(getSupportActionBar()).setTitle(currentFaculty.getFacultyName());

        dbLinks = new DBLinks();
        groups = new ArrayList<>();

        groupAdapter = new JoinGroupAdapter(groups, currentUser, currentFaculty);
        groupsRV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        groupsRV.setAdapter(groupAdapter);
        groupsRV.setNestedScrollingEnabled(false);

        noDataHolder.setVisibility(View.GONE);
        contentHolder.setVisibility(View.GONE);
        loadingAnimationHolder.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);
        loading.playAnimation();

        reqGroupsForFaculty();

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JoinGroupActivity.this, AddGroupActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentFaculty", currentFaculty);
                intent.putExtra("groupType", "public");
                startActivityForResult(intent, ADD_GROUP_REQ_CODE);
            }
        });

    }

    private void reqGroupsForFaculty() {
        String url = dbLinks.getBaseLink() +
                "groups-for-faculty?referencedFacultyName=" + currentFaculty.getFacultyName();
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

                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Group group = new Group();
                        if (!object.getString("groupType").equals("private") &&
                                !currentUser.getJoinedGroupsAccessTokens()
                                        .contains(object.getString("groupAccessToken"))) {
                            Log.d(TAG, "onResponse: user tokens -> " + currentUser.getJoinedGroupsAccessTokens());
                            Log.d(TAG, "onResponse: group token -> " + object.getString("groupAccessToken"));
                            group.setId(object.getString("_id"));
                            group.setLastMessage(new Message());
                            group.setGroupName(object.getString("groupName"));
                            group.setGroupAdministrator(object.getString("groupAdministrator"));
                            group.setGroupSecondaryAdministrator(object.getString("groupSecondaryAdministrator"));
                            group.setSecondaryAdministrator(object.getBoolean("hasSecondaryAdministrator"));
                            group.setNumMembers(object.getInt("numMembers"));
                            group.setGroupType(object.getString("groupType"));
                            group.setAccessToken(object.getString("groupAccessToken"));
                            group.setReferencedFacultyName(object.getString("referencedFacultyName"));
                            group.setReferencedCollegeName(object.getString("referencedCollegeName"));
                            group.setGroupDescription(object.getString("groupDescription"));

                            groups.add(group);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    groupAdapter.notifyItemInserted(groups.size() - 1);
                                }
                            });
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loading.isAnimating())
                                loading.cancelAnimation();
                            loadingAnimationHolder.setVisibility(View.GONE);
                            loading.setVisibility(View.GONE);
                            noDataHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.VISIBLE);
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (groups.isEmpty())
                                noDataHolder.setVisibility(View.VISIBLE);
                            else {
                                contentHolder.setVisibility(View.VISIBLE);
                                noDataHolder.setVisibility(View.GONE);
                            }
                        }
                    });
                    Log.d(TAG, "onResponse: groups -> " + groups.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ADD_GROUP_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Group group = (Group) data.getSerializableExtra("newGroup");
                newGroups.add(group);
                parentIntent.putExtra("new_groups", (Serializable) newGroups);
                setResult(RESULT_OK, parentIntent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_join_group_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_faculty:
                Intent intent = new Intent(JoinGroupActivity.this, EditFacultyActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentFaculty", currentFaculty);
                startActivityForResult(intent, 1);
                return true;
            case R.id.menu_join_private_group:
                Intent privateGroupIntent = new Intent(JoinGroupActivity.this,
                        JoinPrivateGroupActivity.class);
                privateGroupIntent.putExtra("currentFaculty", currentFaculty);
                privateGroupIntent.putExtra("currentUser", currentUser);
                startActivityForResult(privateGroupIntent, 551
                );
                return true;
            default: return false;
        }
    }
}
