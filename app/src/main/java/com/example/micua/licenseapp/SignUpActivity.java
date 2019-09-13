package com.example.micua.licenseapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class SignUpActivity extends AppCompatActivity {

    private ImageView male, female;
    private EditText firstName, lastName, email, pass, confirmPass;
    private Button signUp;
    private CheckBox agree;
    private CardView maleHolder, femaleHolder;

    private boolean maleSelected = false, femaleSelected = false;
    private String firstNameVal, lastNameVal, emailVal, passVal, confirmPassVal;
    private DBLinks dbLinks;

    public static final String TAG = "SignUp";
    public String BASE_URL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        dbLinks = new DBLinks();
        BASE_URL = dbLinks.getBaseLink();

        bindViews();

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maleSelected = true;
                femaleSelected = false;
                animateElevation(maleHolder, 0f, 16f);
                if (femaleHolder.getElevation() != 0f)
                    animateElevation(femaleHolder, 16f, 0f);
            }
        });

        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maleSelected = false;
                femaleSelected = true;
                animateElevation(femaleHolder, 0f, 16f);
                if (maleHolder.getElevation() != 0f)
                    animateElevation(maleHolder, 16f, 0f);
            }
        });

        maleHolder.setElevation(0f);
        femaleHolder.setElevation(0f);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (areFieldsEmpty())
                    Toast.makeText(SignUpActivity.this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                else if (!isGenderSelected()) {
                    Toast.makeText(SignUpActivity.this, "Gender is not selected", Toast.LENGTH_SHORT).show();
                }
                else if (!agree.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "You need to agree with the Terms of Service", Toast.LENGTH_SHORT).show();
                } else if (!passVal.equals(confirmPassVal)) {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else if (!isPasswordLong()) {
                    Toast.makeText(SignUpActivity.this, "Password should be at least 8 " +
                            "characters long with a lowercase, uppercase, number and a special" +
                            " character", Toast.LENGTH_SHORT).show();
                } else if (!isPasswordStrong())
                    Toast.makeText(SignUpActivity.this, "Password is not strong enough", Toast.LENGTH_SHORT).show();
                else {
                    String postUrl = BASE_URL + "register-user";
                    post(postUrl);
                }

            }
        });




    }

    private void animateElevation(CardView cardView, float start, float end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "cardElevation", start, end);
        animator.setDuration(200);
        animator.start();
    }

    private boolean isGenderSelected() {
        return maleSelected || femaleSelected;
    }

    private void bindViews() {
        male = findViewById(R.id.iv_male);
        female = findViewById(R.id.iv_female);
        firstName = findViewById(R.id.et_first_name);
        lastName = findViewById(R.id.et_last_name);
        email = findViewById(R.id.et_email);
        pass = findViewById(R.id.et_password);
        confirmPass = findViewById(R.id.et_confirm_password);
        signUp = findViewById(R.id.btn_sign_up);
        agree = findViewById(R.id.cb_agree);
        maleHolder = findViewById(R.id.cv_male_holder);
        femaleHolder = findViewById(R.id.cv_female_holder);
    }

    private boolean areFieldsEmpty() {
        firstNameVal = firstName.getText().toString();
        lastNameVal = lastName.getText().toString();
        emailVal = email.getText().toString();
        passVal = pass.getText().toString();
        confirmPassVal = confirmPass.getText().toString();

        return Objects.equals(firstNameVal, "") || Objects.equals(lastNameVal, "") ||
                Objects.equals(emailVal, "") || Objects.equals(passVal, "") ||
                Objects.equals(confirmPassVal, "");
    }

    private void post(String url) {
        OkHttpClient client1 = new OkHttpClient();

        String gender = "";
        if (isGenderSelected()) {
            if (maleSelected)
                gender = "male";
            else
                gender = "female";
        }

        Request request = new Request.Builder()
                .url(url + "?lastName=" + lastNameVal + "&firstName=" + firstNameVal
                        + "&password=" + passVal + "&gender=" + gender
                        + "&email=" + emailVal + "&profileSetupComplete=false"
                        + "&joinedCollege=false&joinedCollegeName=&joinedFaculty=false"
                        +"&joinedFacultyName=&joinedGroup=false&joinedGroupsAccessTokens=&verified=false")
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

                try {
                    JSONObject object = new JSONObject(json);
                    JSONArray r = object.getJSONArray("result");
                    if (r.length() != 0)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUpActivity.this, "User already in database", Toast.LENGTH_SHORT).show();
                            }
                        });
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUpActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, EmailVerificationActivity.class);
                                intent.putExtra("to", emailVal);
                                startActivityForResult(intent, 1);
                            }
                        });
                        // finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isPasswordStrong() {
        char[] p = passVal.toCharArray();
        boolean hasUppercase = false, hasLowercase = false, hasNumber = false, hasSpecial = false;
        for (char aP : p) {
            if (aP >= 'A' && aP <= 'Z')
                hasUppercase = true;
            if (aP >= 'a' && aP <= 'z')
                hasLowercase = true;
            if (aP >= '0' && aP <= '9')
                hasNumber = true;
            if (aP > 'a' && aP < 'z')
                hasSpecial = true;
        }

        return hasUppercase && hasLowercase && hasNumber && hasSpecial;
    }

    private boolean isPasswordLong() {
        char[] p = passVal.toCharArray();
        if (p.length >= 8)
            return true;
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK)
            finish();
    }
}
