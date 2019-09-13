package com.example.micua.licenseapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindGroupsActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private LinearLayout noGroupsFound;
    private RelativeLayout groupsHolder;
    private FloatingActionButton addGroup;
    private RecyclerView groupList;

    private User currentUser;
    private College currentCollege;
    private Faculty currentFaculty;
    private List<Group> groups;
    private GroupAdapter adapter;
    private DBLinks dbLinks;

    public static final int ADD_GROUP_REQ_CODE = 2;
    public static final int JOIN_PRIVATE_GROUP_REQ_CODE = 3;

    public static final String TAG = "FindGroupsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_groups);

        bindViews();

        currentCollege = (College) getIntent().getSerializableExtra("currentCollege");
        currentFaculty = (Faculty) getIntent().getSerializableExtra("currentFaculty");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        groups = new ArrayList<>();
        dbLinks = new DBLinks();

        adapter = new GroupAdapter(groups, currentUser, currentFaculty);
        groupList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        groupList.setAdapter(adapter);
        groupList.setNestedScrollingEnabled(false);

        reqGroups();

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindGroupsActivity.this, AddGroupActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentCollege", currentCollege);
                intent.putExtra("currentFaculty", currentFaculty);
                startActivityForResult(intent, ADD_GROUP_REQ_CODE);
            }
        });

    }

    private void reqGroups() {
        String url = dbLinks.getBaseLink() + "groups-for-faculty?referencedFacultyName=" + currentFaculty.getFacultyName();
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
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Group group = new Group();

                        group.setNumMembers(Integer.parseInt(object.getString("numMembers")));
                        group.setGroupDescription(object.getString("groupDescription"));
                        group.setReferencedFacultyName(object.getString("referencedFacultyName"));
                        group.setGroupSecondaryAdministrator(object.getString("groupSecondaryAdministrator"));
                        group.setSecondaryAdministrator(Boolean.parseBoolean(object.getString("hasSecondaryAdministrator")));
                        group.setGroupAdministrator(object.getString("groupAdministrator"));
                        group.setGroupType(object.getString("groupType"));
                        group.setGroupName(object.getString("groupName"));
                        group.setReferencedCollegeName(object.getString("referencedCollegeName"));
                        group.setAccessToken(object.getString("groupAccessToken"));

                        groups.add(group);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (currentFaculty.getNumGroups() == 0) {
                                groupsHolder.setVisibility(View.GONE);
                                noGroupsFound.setVisibility(View.VISIBLE);
                            } else {
                                groupsHolder.setVisibility(View.VISIBLE);
                                noGroupsFound.setVisibility(View.GONE);
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

    private void bindViews() {
        scrollView = findViewById(R.id.scrollView2);
        noGroupsFound = findViewById(R.id.ll_no_groups_found);
        addGroup = findViewById(R.id.fab_add_group);
        groupList = findViewById(R.id.rv_group_list);
        groupsHolder = findViewById(R.id.rl_groups);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_GROUP_REQ_CODE) {
                Group group = (Group) data.getSerializableExtra("newGroup");
                groups.add(group);
                adapter.notifyDataSetChanged();
                Faculty faculty = (Faculty) data.getSerializableExtra("modifiedFaculty");
                currentFaculty.setNumGroups(faculty.getNumGroups());
                if (faculty.getNumGroups() != 0) {
                    groupsHolder.setVisibility(View.VISIBLE);
                    noGroupsFound.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_join_private_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_join_private_group:
                Intent intent = new Intent(FindGroupsActivity.this, JoinPrivateGroupActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentFaculty", currentFaculty);
                intent.putExtra("currentCollege", currentCollege);
                startActivityForResult(intent, JOIN_PRIVATE_GROUP_REQ_CODE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
