package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class LowRezActivity extends AppCompatActivity {

    public static final String TAG = "LowRez";

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_rez);

        ImageView image = findViewById(R.id.iv_image);

        String path = getIntent().getStringExtra("image_path");

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //Log.d(TAG, "run: bm size -> " + bm.getByteCount());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

        //image.setImageBitmap(bm);

        byte[] b = byteArrayOutputStream.toByteArray();

        Log.d(TAG, "run: post encoded size -> " + b.length);

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        image.setImageBitmap(decodedByte);

    }
}
