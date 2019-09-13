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

public class JoinCollegeActivity extends AppCompatActivity {
    public static final String TAG = "JoinCollegeActivity";

    private RelativeLayout loadingHolder;
    private LottieAnimationView loading;
    private RecyclerView collegesRV;

    private Intent parentIntent;
    private User currentUser;
    private DBLinks dbLinks;
    private List<College> colleges, joinedColleges;
    private JoinCollegeAdapter collegeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_college);

        collegesRV = findViewById(R.id.rv_colleges);
        loading = findViewById(R.id.lav_loading);
        loadingHolder = findViewById(R.id.rl_1);

        loadingHolder.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);
        collegesRV.setVisibility(View.GONE);

        Objects.requireNonNull(getSupportActionBar()).hide();

        dbLinks = new DBLinks();

        colleges = new ArrayList<>();
        joinedColleges = new ArrayList<>();
        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        collegeAdapter = new JoinCollegeAdapter(colleges, currentUser);
        collegesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        collegesRV.setAdapter(collegeAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                College college = (College) intent.getSerializableExtra("joinedCollege");
                joinedColleges.add(college);
                parentIntent.putExtra("joined_colleges", (Serializable) joinedColleges);
                JoinCollegeActivity.this.setResult(RESULT_OK, parentIntent);
                Log.d(TAG, "onReceive: joined college -> " + college.getName());
            }
        }, new IntentFilter("college-joined-adapter"));

        reqColleges();
    }

    private void reqColleges() {
        String url = dbLinks.getBaseLink() + "colleges-list";
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
                        College college = new College();
                        college.setId(object.getString("_id"));
                        college.setNumFaculties(object.getInt("collegeNumFaculties"));
                        college.setName(object.getString("collegeName"));
                        college.setCountry(object.getString("collegeCountry"));
                        college.setAddress(object.getString("collegeAddress"));
                        college.setLastEditedBy(object.getString("collegeLastEditedBy"));
                        college.setOriginallyAddedBy(object.getString("collegeOriginallyAddedBy"));
                        college.setCity(object.getString("collegeCity"));
                        college.setDescription(object.getString("collegeDescription"));
                        college.setNumMembers(object.getInt("collegeNumMembers"));
                        college.setNumGroups(object.getInt("collegeNumGroups"));
                        college.setPhotoUrls(object.getString("collegePhotoUrl"));
                        college.setPhoneNumber(object.getString("phoneNumber"));
                        college.setWebsiteUrl(object.getString("websiteUrl"));
                        college.setNumPhotos(1);

                        if (!currentUser.getJoinedCollegeName().contains(college.getName()))
                            colleges.add(college);

                    }
                    Log.d(TAG, "onResponse: colleges -> " + colleges.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.cancelAnimation();
                            loading.setVisibility(View.GONE);
                            loadingHolder.setVisibility(View.GONE);
                            collegesRV.setVisibility(View.VISIBLE);
                            collegeAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
