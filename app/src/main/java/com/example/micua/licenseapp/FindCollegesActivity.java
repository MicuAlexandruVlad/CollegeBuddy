package com.example.micua.licenseapp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

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

public class FindCollegesActivity extends AppCompatActivity {

    private FloatingActionButton search;
    private RelativeLayout shapeIn;
    private CoordinatorLayout shapeOut;
    private Button searchBtn, addCollege;
    private MaterialAutoCompleteTextView collegeName;
    private RecyclerView collegeList, collegeListFiltered;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayAdapter<String> adapter;
    private CollegeAdapter collegeAdapter, collegeAdapterFiltered;
    private String[] colleges;
    private DBLinks dbLinks;
    private User currentUser;
    private List<College> registeredColleges, filteredRegisteredColleges;
    private List<String> collegesRegisteredSuggestion;

    private boolean outRevealEnded = true;

    public static final String TAG = "Find";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_colleges);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        colleges = new String[11089];
        collegesRegisteredSuggestion = new ArrayList<>();
        dbLinks = new DBLinks();
        registeredColleges = new ArrayList<>();
        filteredRegisteredColleges = new ArrayList<>();
        collegeAdapterFiltered = new CollegeAdapter(filteredRegisteredColleges, currentUser, collegeListFiltered, colleges);
        collegeAdapter = new CollegeAdapter(registeredColleges, currentUser, collegeList, colleges);



        bindViews();
        initAutoCompleteView();
        reqRegisteredColleges(dbLinks.getBaseLink() + "colleges-list");
        reqColleges(dbLinks.getBaseLink() + "colleges-all");

        if (!currentUser.isProfileSetupComplete())
            showSetupDialog();

        //collegeList.setHasFixedSize(true);
        collegeList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        collegeList.setAdapter(collegeAdapter);
        collegeList.setNestedScrollingEnabled(false);

        collegeListFiltered.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        collegeListFiltered.setAdapter(collegeAdapterFiltered);
        collegeListFiltered.setNestedScrollingEnabled(false);

        shapeIn.setVisibility(View.INVISIBLE);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                search.clearAnimation();

                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(100);
                fadeOut.setFillAfter(true);
                search.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        shapeOut.setVisibility(View.VISIBLE);
                        shapeIn.setVisibility(View.VISIBLE);

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);


                        Animator circularIn = ViewAnimationUtils.createCircularReveal(
                                shapeIn, metrics.widthPixels - 130, metrics.heightPixels - 130, 0,
                                (float) Math.hypot(shapeOut.getWidth(), shapeOut.getHeight()));
                        circularIn.setInterpolator(new AccelerateDecelerateInterpolator());
                        circularIn.setDuration(400);
                        circularIn.start();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        search.setClickable(false);
                        search.setFocusable(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                shapeOut.setVisibility(View.VISIBLE);
                shapeIn.setVisibility(View.INVISIBLE);

                outRevealEnded = false;

                search.clearAnimation();

                String searchVal = collegeName.getText().toString();

                filter(searchVal);

                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setInterpolator(new AccelerateInterpolator());
                fadeIn.setDuration(100);
                fadeIn.setStartOffset(200);
                fadeIn.setFillAfter(true);
                search.startAnimation(fadeIn);

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);


                Animator circularIn = ViewAnimationUtils.createCircularReveal(
                        shapeOut, metrics.widthPixels / 2, metrics.heightPixels - 130, 0,
                        (float) Math.hypot(shapeOut.getWidth(), shapeOut.getHeight()));
                circularIn.setInterpolator(new AccelerateDecelerateInterpolator());
                circularIn.setDuration(400);
                circularIn.start();

                circularIn.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        search.setClickable(false);
                        search.setFocusable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        search.setClickable(true);
                        search.setFocusable(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });

        addCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindCollegesActivity.this, AddCollegeActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("colleges", colleges);
                startActivityForResult(intent, 1);
            }
        });

    }

    private void showSetupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Welcome")
                .setMessage("Welcome to <<AppName>>. This is the initial setup that you have to complete" +
                        " in order to fully use the app. Please join a college, a faculty and finally, " +
                        "a group where you can communicate with other students." +
                        " Don't see your college, or faculty ? Or maybe you are not interested in any of" +
                        " the already created groups ? No problem. You can create all the above whenever you please.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private void bindViews() {
        search = findViewById(R.id.fab_search);
        shapeOut = findViewById(R.id.rl_shape_out);
        shapeIn = findViewById(R.id.rl_shape_in);
        searchBtn = findViewById(R.id.btn_search);
        collegeName = findViewById(R.id.met_search);
        addCollege = findViewById(R.id.btn_add_college);
        collegeList = findViewById(R.id.rv_college_list);
        collegeListFiltered = findViewById(R.id.rv_college_list_filtered);
    }

    private void initAutoCompleteView() {
        adapter = new ArrayAdapter<>(FindCollegesActivity.this
                , android.R.layout.simple_dropdown_item_1line, collegesRegisteredSuggestion);
        collegeName.setAdapter(adapter);
        collegeName.setThreshold(4);
    }

    private void reqColleges(String url) {
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
                        colleges[i] = object.getString("collegeName");
                    }
                    Log.d(TAG, "Colleges: " + array.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void reqRegisteredColleges(String url) {
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
                    int size = array.length();
                    //collegesRegisteredSuggestion = new String[size];
                    for (int i = 0; i < size; i++) {
                        JSONObject object = array.getJSONObject(i);
                        collegesRegisteredSuggestion.add(object.getString("collegeName"));
                        College college = new College();
                        college.setNumMembers(Integer.parseInt(object.getString("collegeNumMembers")));
                        college.setDescription(object.getString("collegeDescription"));
                        college.setCity(object.getString("collegeCity"));
                        college.setAddress(object.getString("collegeAddress"));
                        college.setCountry(object.getString("collegeCountry"));
                        college.setName(object.getString("collegeName"));
                        college.setOriginallyAddedBy(object.getString("collegeOriginallyAddedBy"));
                        college.setLastEditedBy(object.getString("collegeLastEditedBy"));
                        college.setPhotoUrls(object.getString("collegePhotoUrl"));
                        college.setNumGroups(Integer.parseInt(object.getString("collegeNumGroups")));
                        college.setId(object.getString("_id"));
                        college.setNumFaculties(Integer.parseInt(object.getString("collegeNumFaculties")));
                        college.setWebsiteUrl(object.getString("websiteUrl"));
                        college.setPhoneNumber(object.getString("phoneNumber"));

                        registeredColleges.add(college);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                collegeAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data !=  null) {
                College college = (College) data.getSerializableExtra("addedCollege");
                registeredColleges.add(college);
                collegeAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == 123 && resultCode == RESULT_OK) {
            if (data != null) {

            }
        }
    }

    private void filter(String value) {
        Log.d(TAG, "Query: " + value);
        if (value.equalsIgnoreCase("")) {
            filteredRegisteredColleges.clear();
            collegeAdapterFiltered.notifyDataSetChanged();
            collegeList.setVisibility(View.VISIBLE);
            collegeListFiltered.setVisibility(View.GONE);
        } else {
            for (College college : registeredColleges){
                if (college.getName().toLowerCase().contains(value.toLowerCase())) {
                    filteredRegisteredColleges.add(college);
                    collegeAdapterFiltered.notifyDataSetChanged();
                    Log.d(TAG, "Added to filtered list: " + college.getName());
                }
                collegeListFiltered.setVisibility(View.VISIBLE);
                collegeList.setVisibility(View.GONE);
            }
        }
    }
}
