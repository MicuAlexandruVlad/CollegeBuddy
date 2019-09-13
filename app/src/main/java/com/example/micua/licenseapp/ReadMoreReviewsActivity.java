package com.example.micua.licenseapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadMoreReviewsActivity extends AppCompatActivity {
    public static final String TAG = "ReadMoreReviews";

    private RecyclerView reviewsRV;

    private Intent parentIntent;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_more_reviews);

        Objects.requireNonNull(getSupportActionBar()).hide();

        reviewsRV = findViewById(R.id.rv_reviews);

        reviews = new ArrayList<>();
        parentIntent = getIntent();
        reviews = (List<Review>) parentIntent.getSerializableExtra("all_reviews");
        reviewAdapter = new ReviewAdapter(reviews);

        reviewsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        reviewsRV.setAdapter(reviewAdapter);

    }
}
