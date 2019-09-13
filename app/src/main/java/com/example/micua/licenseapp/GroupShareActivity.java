package com.example.micua.licenseapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

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

public class GroupShareActivity extends AppCompatActivity {
    public static final String TAG = "GroupShareActivity";

    private RecyclerView groupRV;
    private TextView header;

    private Intent intent;
    private User currentUser;
    private Quiz quiz;
    private DBLinks dbLinks;
    private List<Group> groups;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_share);

        header = findViewById(R.id.tv_header);
        groupRV = findViewById(R.id.rv_group_share);

        intent = getIntent();
        dbLinks = new DBLinks();
        groups = new ArrayList<>();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        quiz = (Quiz) intent.getSerializableExtra("quiz");

        groupAdapter = new GroupAdapter(groups, currentUser, new Faculty());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        groupRV.setLayoutManager(layoutManager);
        groupRV.setAdapter(groupAdapter);

        requestGroups();

    }

    private void requestGroups() {
        OkHttpClient client = new OkHttpClient();

        String url = dbLinks.getBaseLink() + "groups-for-user?joinedGroupsAccessTokens=" +
                currentUser.getJoinedGroupsAccessTokens();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                final Gson gson = new Gson();
                try {
                    JSONObject res = new JSONObject(json);
                    JSONArray array = res.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        groups.add(gson.fromJson(array.getJSONObject(i).toString(), Group.class));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupAdapter.notifyItemInserted(groups.size() - 1);
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
