package com.example.micua.licenseapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollegeDetailsActivity extends AppCompatActivity {
    public static final String TAG = "CollegeDetailsActivity";
    public static final int ADD_REVIEW_REQ_CODE = 222;
    private static final int EDIT_DETAILS_REQ_CODE = 1442;
    public static final int ADD_FACULTY_REQ_CODE = 1123;

    private ImageView collegePhoto;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton addReview;
    private RecyclerView reviewsRV;
    private TextView ratingValue, address, phoneNumber, numStudents, numFaculties, description;
    private RelativeLayout call, navigate, website;
    private Button editDetails, addFaculty;
    private RatingBar ratingBar;
    private LinearLayout readMoreReviews;

    private ReviewStarAdapter starAdapter;
    private College currentCollege;
    private Intent parentIntent;
    private User currentUser;
    private List<Double> starValues;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewsAll, displayedReviews;
    private DBLinks dbLinks;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_details);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        parentIntent = getIntent();

        currentCollege = (College) parentIntent.getSerializableExtra("currentCollege");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        bindViews();

        readMoreReviews.setVisibility(View.GONE);

        ratingBar.setMax(5);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(0.01f);
        ratingBar.setEnabled(false);

        dbLinks = new DBLinks();
        reviewsAll = new ArrayList<>();
        displayedReviews = new ArrayList<>();

        reviewAdapter = new ReviewAdapter(displayedReviews);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        reviewsRV.setLayoutManager(layoutManager1);
        reviewsRV.setAdapter(reviewAdapter);

        requestReviews(dbLinks.getBaseLink() + "get-reviews-for-id?referencedId=" + currentCollege.getId());

        initViews();

        Glide.with(this).load(currentCollege.getPhotoUrls()).into(collegePhoto);
        collapsingToolbarLayout.setTitle(currentCollege.getName());

        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollegeDetailsActivity.this, AddReviewActivity.class);
                intent.putExtra("currentCollege", currentCollege);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("review_type", 0);
                startActivityForResult(intent, ADD_REVIEW_REQ_CODE);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCollege.getPhoneNumber().equals(""))
                    Toast.makeText(CollegeDetailsActivity.this,
                            "No phone number provided for this college", Toast.LENGTH_SHORT).show();
                else {
                    final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currentCollege.getPhoneNumber()));
                    if (ActivityCompat.checkSelfPermission(CollegeDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CollegeDetailsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 111);

                        return;
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CollegeDetailsActivity.this);
                        builder.setMessage("This action will initiate a phone call. Are you sure you want to proceed ?")
                                .setTitle("Notice")
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @SuppressLint("MissingPermission")
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(intent);
                                    }
                                });
                        builder.create();
                        builder.show();
                    }

                }
            }
        });

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCollege.getAddress().equals(""))
                    Toast.makeText(CollegeDetailsActivity.this,
                            "No address provided for this college", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:q=" + currentCollege.getAddress()));
                    startActivity(intent);
                }
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentCollege.getWebsiteUrl().equals("")) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(currentCollege.getWebsiteUrl()));
                    startActivity(i);
                }
                else
                    Toast.makeText(CollegeDetailsActivity.this,
                            "No website provided for this college", Toast.LENGTH_SHORT).show();
            }
        });

        editDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollegeDetailsActivity.this, EditCollegeDetailsActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentCollege", currentCollege);
                startActivityForResult(intent, EDIT_DETAILS_REQ_CODE);
            }
        });

        addFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollegeDetailsActivity.this, AddFacultyActivity.class);
                intent.putExtra("currentCollege", currentCollege);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, ADD_FACULTY_REQ_CODE);
            }
        });

        readMoreReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollegeDetailsActivity.this, ReadMoreReviewsActivity.class);
                intent.putExtra("all_reviews", (Serializable) reviewsAll);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        numStudents.setText(currentCollege.getNumMembers() + "");
        numFaculties.setText(currentCollege.getNumFaculties() + "");
        if (currentCollege.getAddress().equals(""))
            address.setText("No address provided");
        else
            address.setText(currentCollege.getAddress());
        if (currentCollege.getPhoneNumber().equals(""))
            phoneNumber.setText("No phone number provided");
        else
            phoneNumber.setText(currentCollege.getPhoneNumber());
        if (currentCollege.getDescription().equals(""))
            description.setText("No description available");
        else
            description.setText(currentCollege.getDescription());
    }

    private void bindViews() {
        collegePhoto = findViewById(R.id.iv_backdrop);
        collapsingToolbarLayout = findViewById(R.id.ctl_collapsing);
        addReview = findViewById(R.id.fab_add_review);
        ratingBar = findViewById(R.id.rb_overall_rating);
        reviewsRV = findViewById(R.id.rv_reviews);
        numFaculties = findViewById(R.id.tv_num_faculties);
        numStudents = findViewById(R.id.tv_num_students);
        ratingValue = findViewById(R.id.tv_rating);
        address = findViewById(R.id.tv_address);
        phoneNumber = findViewById(R.id.tv_phone_number);
        description = findViewById(R.id.tv_college_description);
        editDetails = findViewById(R.id.btn_edit_college_details);
        addFaculty = findViewById(R.id.btn_add_faculty);
        call = findViewById(R.id.rl_call);
        navigate = findViewById(R.id.rl_navigate);
        website = findViewById(R.id.rl_website);
        readMoreReviews = findViewById(R.id.ll_read_more_reviews);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REVIEW_REQ_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Review review = (Review) data.getSerializableExtra("new_review");
                if (displayedReviews.size() < 10) {
                    displayedReviews.add(review);
                    reviewAdapter.notifyItemInserted(displayedReviews.size() - 1);
                }
                reviewsAll.add(review);
            }
        }

        if (requestCode == EDIT_DETAILS_REQ_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                currentCollege = (College) data.getSerializableExtra("editedCollege");
                collapsingToolbarLayout.setTitle(currentCollege.getName());
                initViews();
                Glide.with(this).load(currentCollege.getPhotoUrls()).into(collegePhoto);
                uploadCollegeData();
            }
        }

        if (requestCode == ADD_FACULTY_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                boolean autoJoin = data.getBooleanExtra("autoJoin", false);
                parentIntent.putExtra("autoJoin", autoJoin);
                currentCollege.setNumFaculties(currentCollege.getNumFaculties() + 1);
                numFaculties.setText(currentCollege.getNumFaculties() + "");
                if (autoJoin) {
                    Faculty faculty = (Faculty) data.getSerializableExtra("addedFaculty");
                    parentIntent.putExtra("addedFaculty", faculty);
                }
            }
        }
    }

    private void uploadCollegeData() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", currentCollege.getId());
        params.put("collegeName", currentCollege.getName());
        params.put("collegeAddress", currentCollege.getAddress());
        params.put("collegeCity", currentCollege.getCity());
        params.put("collegeCountry", currentCollege.getCountry());
        params.put("collegeDescription", currentCollege.getDescription());
        params.put("lastEditedBy", currentCollege.getLastEditedBy());
        params.put("collegeNumFaculties", currentCollege.getNumFaculties());
        params.put("collegeNumGroups", currentCollege.getNumGroups());
        params.put("collegeNumMembers", currentCollege.getNumMembers());
        params.put("collegeNumPhotos", currentCollege.getNumPhotos());
        params.put("originallyAddedBy", currentCollege.getOriginallyAddedBy());
        params.put("phoneNumber", currentCollege.getPhoneNumber());
        params.put("collegePhotoUrls", currentCollege.getPhotoUrls());
        params.put("websiteUrl", currentCollege.getWebsiteUrl());

        String url = dbLinks.getBaseLink() + "update-college-all-data";

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void requestReviews(String url) {
        OkHttpClient client1 = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client1.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                double average = 0.0;
                try {

                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject entry = result.getJSONObject(i);
                        Review review;
                        Gson gson = new Gson();

                        review = gson.fromJson(entry.toString(), Review.class);
                        reviewsAll.add(review);

                        // display only 10 reviews
                        if (i < 10)
                            displayedReviews.add(review);
                        average += review.getRating();
                    }
                    final double finalAverage = average;
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            if (displayedReviews.size() > 10)
                                readMoreReviews.setVisibility(View.VISIBLE);
                            else {
                                readMoreReviews.setVisibility(View.GONE);
                            }
                            if (!reviewsAll.isEmpty()) {
                                double val = finalAverage / reviewsAll.size();
                                String rating = val + "";

                                ratingValue.setText(rating.split("\\.")[0] + "."
                                        + rating.split("\\.")[1].toCharArray()[0]);
                                ratingBar.setRating((float) val);
                                reviewAdapter.notifyDataSetChanged();
                            }
                            else
                                ratingValue.setText("0.0");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, parentIntent);
        finish();
    }
}
