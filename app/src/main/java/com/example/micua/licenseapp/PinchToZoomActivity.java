package com.example.micua.licenseapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

public class PinchToZoomActivity extends AppCompatActivity {
    private static final String TAG = "PinchToZoomActivity";
    private BigImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));

        setContentView(R.layout.activity_pinch_to_zoom);

        Objects.requireNonNull(getSupportActionBar()).hide();

        imageView = findViewById(R.id.mBigImage);

        // Glide.with(this).load(new File(getIntent().getStringExtra("image_path"))).into(imageView);

        imageView.showImage(Uri.parse("file://" + getIntent().getStringExtra("image_path")));

        Log.d(TAG, "onCreate: path -> " + getIntent().getStringExtra("image_path"));
    }
}
