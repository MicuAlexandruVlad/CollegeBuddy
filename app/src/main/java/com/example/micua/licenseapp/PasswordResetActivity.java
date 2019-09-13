package com.example.micua.licenseapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class PasswordResetActivity extends AppCompatActivity {
    public static final String TAG = "PasswordResetActivity";

    private MaterialEditText email, newPassword, confirmPassword;
    private Button sendEmail, verify, changePassword;
    private RelativeLayout step1, step2, step3;
    private TextView resend;
    private EditText verificationField;

    private DBLinks dbLinks;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        Objects.requireNonNull(getSupportActionBar()).hide();

        email = findViewById(R.id.met_email);
        newPassword = findViewById(R.id.met_new_password);
        confirmPassword = findViewById(R.id.met_repeat_new_password);
        sendEmail = findViewById(R.id.btn_send_email);
        changePassword = findViewById(R.id.btn_change_password);
        step1 = findViewById(R.id.rl_step_1);
        step2 = findViewById(R.id.rl_step_2);
        step3 = findViewById(R.id.rl_step_3);
        resend = findViewById(R.id.tv_resend_email);
        verificationField = findViewById(R.id.et_verification_id);
        verify = findViewById(R.id.btn_verify_code);

        step1.setVisibility(View.VISIBLE);
        step2.setVisibility(View.GONE);
        step3.setVisibility(View.GONE);

        dbLinks = new DBLinks();

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailVal = email.getText().toString();

                if (emailVal.equals(""))
                    Toast.makeText(PasswordResetActivity.this,
                            "Email field is empty", Toast.LENGTH_SHORT).show();
                else if (emailVal.split("@").length != 2)
                    Toast.makeText(PasswordResetActivity.this,
                            "Email address is not valid", Toast.LENGTH_SHORT).show();
                else {
                    startVerification(emailVal);
                    step1.setVisibility(View.GONE);
                    step2.setVisibility(View.VISIBLE);
                }
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVerification(email.getText().toString());
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verifyVal = verificationField.getText().toString();
                if (verifyVal.equals(id)) {
                    step2.setVisibility(View.GONE);
                    step3.setVisibility(View.VISIBLE);
                }
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassVal = newPassword.getText().toString();
                String confirmPassVal = confirmPassword.getText().toString();

                if (newPassVal.equals(confirmPassVal)) {
                    // TODO: update user password - done
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.put("email", email.getText().toString());
                    params.put("password", newPassVal);

                    String url = dbLinks.getBaseLink() + "change-password";

                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PasswordResetActivity.this,
                                            "Password Changed", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                }
            }
        });
    }

    private void startVerification(String emailVal) {
        id = generateVerificationId();
        Log.d(TAG, "onClick: id -> " + id);

        post(dbLinks.getBaseLink() + "send-email", emailVal, id);
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

    private void post(String s, String to, String verificationId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("to", to);
        params.put("subject", "Password Reset");
        params.put("verificationId", verificationId);

        client.post(s, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PasswordResetActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}
