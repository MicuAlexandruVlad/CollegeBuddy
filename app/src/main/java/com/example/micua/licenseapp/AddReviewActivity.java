package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class AddReviewActivity extends AppCompatActivity {

    private Review review;
    private RecyclerView scoreRV;
    private EditText reviewText;
    private BubbleSeekBar reviewBar;
    private Button postReview;

    private List<Double> starList;
    private ReviewStarAdapter starAdapter;
    private Intent parentIntent;
    private User currentUser;
    private College currentCollege;
    private Faculty currentFaculty;
    // 0 - college
    // 1 - faculty
    private int reviewType;
    private DBLinks dbLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        Objects.requireNonNull(getSupportActionBar()).hide();

        review = new Review();
        dbLinks = new DBLinks();
        parentIntent = getIntent();

        reviewType = parentIntent.getIntExtra("review_type", -1);
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        currentCollege = new College();
        currentFaculty = new Faculty();
        if (reviewType == 0)
            currentCollege = (College) parentIntent.getSerializableExtra("currentCollege");
        if (reviewType == 1)
            currentFaculty = (Faculty) parentIntent.getSerializableExtra("currentFaculty");

        starList = new ArrayList<>();
        starList.add(0.0);
        starList.add(0.0);
        starList.add(0.0);
        starList.add(0.0);
        starList.add(0.0);
        starAdapter = new ReviewStarAdapter(starList, -1);

        reviewBar = findViewById(R.id.bsb_rating);
        scoreRV = findViewById(R.id.rv_score);
        reviewText = findViewById(R.id.et_review_text);
        postReview = findViewById(R.id.btn_post_review);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        scoreRV.setLayoutManager(layoutManager);
        scoreRV.setAdapter(starAdapter);

        reviewBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                setAdapterData(progressFloat);

            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        postReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double finalScore = 0;
                for (int i = 0; i < starList.size(); i++) {
                    finalScore += starList.get(i);
                }

                String text = reviewText.getText().toString();
                review.setReviewText(text);
                review.setUserEmail(currentUser.getEmail());
                review.setReviewType(reviewType);
                if (reviewType == 0)
                    review.setReferencedId(currentCollege.getId());
                if (reviewType == 1)
                    review.setReferencedId(currentFaculty.getId());
                review.setDatePosted(Calendar.getInstance().getTime().toString());
                review.setUserFullName(currentUser.getFirstName() + " " + currentUser.getLastName());
                review.setRating(finalScore);

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("reviewText", review.getReviewText());
                params.put("userEmail", review.getUserEmail());
                params.put("reviewType", review.getReviewType());
                params.put("referencedId", review.getReferencedId());
                params.put("datePosted", review.getDatePosted());
                params.put("userFullName", review.getUserFullName());
                params.put("rating", review.getRating());

                String url = dbLinks.getBaseLink() + "upload-review";
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(AddReviewActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        parentIntent.putExtra("new_review", review);
                        setResult(RESULT_OK, parentIntent);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
            }
        });
    }

    private void setAdapterData(float progressFloat) {
        if (progressFloat == 0.0) {
            starList.set(0, 0.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 0.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 0.5) {
            starList.set(0, 0.5);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 0.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 1.0) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 0.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 1.5) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 0.5);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 2.0) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 2.5) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 0.5);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 3.0) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 1.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 3.5) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 1.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 0.5);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 4.0) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 1.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 1.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.0);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 4.5) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 1.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 1.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 0.5);
            starAdapter.notifyItemChanged(4);
        }
        if (progressFloat == 5.0) {
            starList.set(0, 1.0);
            starAdapter.notifyItemChanged(0);
            starList.set(1, 1.0);
            starAdapter.notifyItemChanged(1);
            starList.set(2, 1.0);
            starAdapter.notifyItemChanged(2);
            starList.set(3, 1.0);
            starAdapter.notifyItemChanged(3);
            starList.set(4, 1.0);
            starAdapter.notifyItemChanged(4);
        }
    }
}
