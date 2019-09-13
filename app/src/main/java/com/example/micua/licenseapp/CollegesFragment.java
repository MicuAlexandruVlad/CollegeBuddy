package com.example.micua.licenseapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class CollegesFragment extends Fragment {
    public static final String TAG = "CollegesFragment";
    public static final int ADD_COLLEGE_REQ_CODE = 1111;
    public static final int JOIN_COLLEGE_REQ_CODE = 144;

    private RecyclerView recyclerView;
    private FloatingActionButton addCollege;
    private Button joinCollege;

    private String title;
    private String[] collegesList;
    private int page;
    private List<College> colleges;
    private RemasteredCollegeAdapter adapter;
    private User currentUser;
    private DBLinks dbLinks;

    public static CollegesFragment newInstance(int page, String title, Bundle data) {
        CollegesFragment collegesFragment = new CollegesFragment();
        data.putInt("page_count", page);
        data.putString("page_title", title);
        collegesFragment.setArguments(data);
        return collegesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        page = getArguments().getInt("page_count", 0);
        title = getArguments().getString("page_title");
        colleges = (List<College>) getArguments().getSerializable("colleges");
        currentUser = (User) getArguments().getSerializable("currentUser");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colleges, container, false);

        recyclerView = view.findViewById(R.id.rv_colleges);
        addCollege = view.findViewById(R.id.fab_add_college);
        joinCollege = view.findViewById(R.id.btn_join_college);

        dbLinks = new DBLinks();

        reqColleges(dbLinks.getBaseLink() + "colleges-all");

        adapter = new RemasteredCollegeAdapter(colleges, currentUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        recyclerView.setNestedScrollingEnabled(false);

        addCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddCollegeActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("colleges", collegesList);
                startActivityForResult(intent, ADD_COLLEGE_REQ_CODE);
            }
        });

        joinCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), JoinCollegeActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, JOIN_COLLEGE_REQ_CODE);
            }
        });

        return view;
    }

    private void reqColleges(String url) {
        collegesList = new String[11089];
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
                        collegesList[i] = object.getString("collegeName");
                    }
                    Log.d(TAG, "Colleges: " + array.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_COLLEGE_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getBooleanExtra("autoJoin", false)) {
                    Log.d(TAG, "onActivityResult: autoJoin -> " + data.getBooleanExtra("autoJoin", false));
                    College college = (College) data.getSerializableExtra("addedCollege");
                    colleges.add(college);
                    adapter.notifyItemInserted(colleges.size() - 1);
                }
            }
        }

        if (requestCode == JOIN_COLLEGE_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                List<College> newColleges = (List<College>) data.getSerializableExtra("joined_colleges");
                colleges.addAll(newColleges);
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected void receiveCollege(College college) {
        Log.d(TAG, "receiveCollege: received college -> " + college.getName());
        colleges.add(college);
        adapter.notifyItemInserted(colleges.size() - 1);
    }
}
