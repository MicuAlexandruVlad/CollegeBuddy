package com.example.micua.licenseapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class JoinFacultyActivity extends AppCompatActivity {
    public static final String TAG = "JoinFacultyActivity";

    private RecyclerView facultiesRV;
    private LottieAnimationView loading;
    private RelativeLayout loadingHolder;

    private List<Faculty> faculties, joinedFaculties;
    private JoinFacultyAdapter facultyAdapter;
    private DBLinks dbLinks;
    private User currentUser;
    private Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_faculty);

        Objects.requireNonNull(getSupportActionBar()).hide();

        facultiesRV = findViewById(R.id.rv_faculties);
        loading = findViewById(R.id.lav_loading);
        loadingHolder = findViewById(R.id.rl_1);

        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        faculties = new ArrayList<>();
        joinedFaculties = new ArrayList<>();
        facultyAdapter = new JoinFacultyAdapter(faculties, currentUser);

        facultiesRV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        facultiesRV.setAdapter(facultyAdapter);

        dbLinks = new DBLinks();

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Faculty faculty = (Faculty) intent.getSerializableExtra("joinedFaculty");
                joinedFaculties.add(faculty);
                parentIntent.putExtra("joined_faculties", (Serializable) joinedFaculties);
                JoinFacultyActivity.this.setResult(RESULT_OK, parentIntent);
                Log.d(TAG, "onReceive: joined faculty -> " + faculty.getFacultyName());
            }
        }, new IntentFilter("faculty-joined-adapter"));

        reqFaculties();
    }

    private void reqFaculties() {
        Log.d(TAG, "reqFaculties: joined colleges -> " + currentUser.getJoinedCollegeName());
        String url = dbLinks.getBaseLink() + "faculties-to-join?joinedCollegeName=" + currentUser.getJoinedCollegeName();
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
                    JSONObject obj = new JSONObject(json);
                    JSONArray res = obj.getJSONArray("result");
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject object = res.getJSONObject(i);
                        Faculty faculty = new Faculty();
                        faculty.setId(object.getString("_id"));
                        faculty.setNumMembers(object.getInt("facultyNumMembers"));
                        faculty.setReferencedCollegeName(object.getString("facultyReferencedCollegeName"));
                        faculty.setNumGroups(object.getInt("facultyNumGroups"));
                        faculty.setFacultyName(object.getString("facultyName"));
                        faculty.setOriginallyAddedBby(object.getString("originallyAddedBy"));
                        faculty.setLastEditedBy(object.getString("lastEditedBy"));
                        faculty.setFacultyDescription(object.getString("facultyDescription"));

                        if (!currentUser.getJoinedFacultyName().contains(faculty.getId()))
                            faculties.add(faculty);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            facultyAdapter.notifyDataSetChanged();
                            loading.cancelAnimation();
                            loadingHolder.setVisibility(View.GONE);
                            facultiesRV.setVisibility(View.VISIBLE);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
