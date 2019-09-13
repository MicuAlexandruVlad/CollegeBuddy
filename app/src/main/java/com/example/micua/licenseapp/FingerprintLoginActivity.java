package com.example.micua.licenseapp;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FingerprintLoginActivity extends AppCompatActivity {

    private ImageView fingerprint;

    private OkHttpClient client;

    public static final String TAG = "FingerprintLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_login);


        fingerprint = findViewById(R.id.iv_fingerprint);
        Button trigger = findViewById(R.id.btn_trigger);

        client = new OkHttpClient();

        trigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = fingerprint.getDrawable();
                if (drawable instanceof Animatable)
                    ((Animatable) drawable).start();
            }
        });

        final String url = "http://efe86603.ngrok.io/users";

        getJSONData(url);
    }

    private void getJSONData(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call,@NonNull Response response) throws IOException {
                String jsonData = response.body().string();

                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    Log.d("Test", jsonArray.getJSONObject(0).getString("_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void hasFingerPrint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Fingerprint API only available on from Android 6.0 (M)
            FingerprintManager fingerprintManager = (FingerprintManager) FingerprintLoginActivity.this.getSystemService(Context.FINGERPRINT_SERVICE);
            if (!fingerprintManager.isHardwareDetected()) {
                // Device doesn't support fingerprint authentication
                Toast.makeText(this, "Device does not support fingerprint authentication", Toast.LENGTH_SHORT).show();
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                // User hasn't enrolled any fingerprints to authenticate with
                Toast.makeText(this, "No fingerprint registered", Toast.LENGTH_SHORT).show();
            } else {
                // Everything is ready for fingerprint authentication
                Toast.makeText(this, "Fingerprint available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

