package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import cz.msebera.android.httpclient.Header;


public class EmailVerificationActivity extends AppCompatActivity {
    public static final String TAG = "EmailVerification";

    private EditText code;
    private TextView sentTo, resend;
    private Button verify;

    private String to;
    private String verificationId;
    private Intent parentIntent;
    private DBLinks dbLinks;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        Objects.requireNonNull(getSupportActionBar()).hide();

        parentIntent = getIntent();
        to = parentIntent.getStringExtra("to");

        sentTo = findViewById(R.id.tv_to);
        code = findViewById(R.id.et_verification_id);
        resend = findViewById(R.id.tv_resend_email);
        verify = findViewById(R.id.btn_verify);

        dbLinks = new DBLinks();

        startVerification();

        sentTo.setText(to);

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVerification();
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (code.getText().toString().equals(""))
                    Toast.makeText(EmailVerificationActivity.this,
                            "Code field is empty", Toast.LENGTH_SHORT).show();
                else {
                    if (code.getText().toString().equals(verificationId)) {
                        DialogVerified dialogVerified = new DialogVerified(EmailVerificationActivity.this);
                        dialogVerified.create();
                        dialogVerified.show();

                        dialogVerified.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });

                        setUserVerified(dbLinks.getBaseLink() + "set-user-verified");
                    }
                }
            }
        });
    }

    private void setUserVerified(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("userEmail", to);
        params.put("verified", true);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void startVerification() {
        verificationId = generateVerificationId();
        Log.d(TAG, "startVerification: verificationId -> " + verificationId);
        post(dbLinks.getBaseLink() + "send-email");
    }

    private void post(String s) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("to", to);
        params.put("subject", "Account Verification");
        params.put("verificationId", verificationId);

        client.post(s, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EmailVerificationActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private String generateVerificationId() {
        int d0 = new Random().nextInt(10);
        int d1 = new Random().nextInt(10);
        int d2 = new Random().nextInt(10);
        int d3 = new Random().nextInt(10);
        int d4 = new Random().nextInt(10);

        StringBuilder builder = new StringBuilder();
        builder.append(d0);
        builder.append(d1);
        builder.append(d2);
        builder.append(d3);
        builder.append(d4);

        return builder.toString();
    }
}
