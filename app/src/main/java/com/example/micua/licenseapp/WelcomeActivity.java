package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WelcomeActivity extends AppCompatActivity {

    private LinearLayout fbLogin, googleLogin;
    private TextView register, forgotPassword;
    private EditText email, password;
    private Button signIn;
    private ImageView logo;

    private GoogleSignInClient client;
    private DBLinks dbLinks;
    private CallbackManager callbackManager;
    private User currentUser;
    private List<Group> userGroups;
    private List<College> userColleges;
    private List<Faculty> userFaculties;
    private static Repository repository;
    private SharedPreferences sharedPreferences;

    private boolean isInDB;

    public static final String TAG = "Welcome";
    public static final int MAIN_ACTIVITY_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        FirebaseApp.initializeApp(this);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();



        dbLinks = new DBLinks();
        currentUser = new User();
        userColleges = new ArrayList<>();
        userFaculties = new ArrayList<>();
        userGroups = new ArrayList<>();
        repository = new Repository(this);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        client = GoogleSignIn.getClient(this, googleSignInOptions);

        bindViews();

        email.setText("test1@yahoo.com");
        password.setText("Test123..");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, PasswordResetActivity.class);
                startActivity(intent);
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailVal, passVal;
                emailVal = email.getText().toString();
                passVal = password.getText().toString();
                Log.d(TAG,"Email: " + email.getText().toString());

                if (emailVal.equals("") || passVal.equals("")) {
                    Toast.makeText(WelcomeActivity.this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                } else if (!isEmailValid())
                    Toast.makeText(WelcomeActivity.this, "Email address is not valid", Toast.LENGTH_SHORT).show();
                else {
                    OkHttpClient client1 = new OkHttpClient();

                    String url = dbLinks.getBaseLink() + "user-data-to-check?email=" + emailVal +
                            "&password=" + passVal;
                    Log.d(TAG, "onClick: url -> " + url);

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
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            try {
                                List<College> colleges = new ArrayList<>();
                                List<Faculty> faculties = new ArrayList<>();
                                List<Group> groups = new ArrayList<>();
                                String json = response.body().string();
                                Log.d(TAG, "onResponse: json email login -> " + json);
                                JSONObject obj = new JSONObject(json);
                                boolean exists = obj.getBoolean("exists");
                                if (exists) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(WelcomeActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    JSONObject user = obj.getJSONArray("user").getJSONObject(0);
                                    final User currentUser = new User();

                                    currentUser.setEmail(user.getString("email"));
                                    currentUser.setFirstName(user.getString("firstName"));
                                    currentUser.setId(user.getString("_id"));
                                    currentUser.setLastName(user.getString("lastName"));
                                    currentUser.setPassword(user.getString("password"));
                                    currentUser.setGender(user.getString("gender"));
                                    currentUser.setProfileSetupComplete(user.getBoolean("profileSetupComplete"));
                                    currentUser.setJoinedCollege(user.getBoolean("joinedCollege"));
                                    currentUser.setJoinedCollegeName(user.getString("joinedCollegeName"));
                                    currentUser.setJoinedFaculty(user.getBoolean("joinedFaculty"));
                                    currentUser.setJoinedFacultyName(user.getString("joinedFacultyName"));
                                    currentUser.setJoinedGroup(user.getBoolean("joinedGroup"));
                                    currentUser.setJoinedGroupsAccessTokens(user.getString("joinedGroupsAccessTokens"));
                                    currentUser.setVerified(user.getBoolean("verified"));

                                    if (currentUser.isVerified()) {
                                        if (currentUser.isJoinedCollege())
                                            colleges.addAll(getColleges(obj));
                                        if (currentUser.isJoinedFaculty())
                                            faculties.addAll(getFaculties(obj));
                                        if (currentUser.isJoinedGroup())
                                            groups.addAll(getGroups(obj));

                                        if (!currentUser.isProfileSetupComplete()) {
                                            Intent intent = new Intent(WelcomeActivity.this, FindCollegesActivity.class);
                                            intent.putExtra("currentUser", currentUser);
                                            startActivity(intent);
                                        }
                                        else {
                                            // readUserFromSharedPref(currentUser);
                                            computeLastMessages(groups, colleges, faculties, currentUser);
                                        }
                                    }
                                    else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                                                builder.setTitle("Account not verified")
                                                        .setMessage("You will be redirected to account verification" +
                                                                " page. After that you will have full access to" +
                                                                " your account.")
                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                Intent intent = new Intent(WelcomeActivity.this
                                                                        , EmailVerificationActivity.class);
                                                                intent.putExtra("to", currentUser.getEmail());
                                                                startActivity(intent);
                                                            }
                                                        });
                                                builder.create();
                                                builder.show();
                                            }
                                        });
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    });
                }
            }
        });

        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = client.getSignInIntent();
                startActivityForResult(signInIntent, 1);
            }
        });

        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Fb clicked");
                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                Log.d(TAG, "onSuccess: Success");
                                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object, GraphResponse response) {
                                                Log.d(TAG, "JSON: " + object.toString());
                                                String email = "", id = "", firstName = ""
                                                        , lastName = "", gender = "";
                                                try {
                                                    email = object.getString("email");
                                                    id = object.getString("id");
                                                    firstName = object.getString("name")
                                                            .split(" ")[0];
                                                    lastName = object.getString("name")
                                                            .split(" ")[1];
                                                    gender = object.getString("gender");
                                                    Log.d(TAG, "onCompleted: firstName -> " + firstName);

                                                    postFB(dbLinks.getBaseLink() + "register-user-fb",
                                                            id, email, gender, lastName, firstName);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id, name, email, gender");
                                graphRequest.setParameters(parameters);
                                graphRequest.executeAsync();
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(WelcomeActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(FacebookException error) {
                                Toast.makeText(WelcomeActivity.this, "There was an error" +
                                        " signing you in", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, error.getCause().getMessage());
                            }


                        });

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Collections.singletonList("public_profile"));
            }
        });
    }

    private void readUserFromSharedPref(User currentUser) {
        String userJSONData = getResources().getString(R.string.saved_user_json);
        Gson gson = new Gson();
        if (!userJSONData.equals("")) {
            currentUser = gson.fromJson(userJSONData, User.class);
        }
        else {
            currentUser = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void computeLastMessages(final List<Group> groups, List<College> colleges, List<Faculty> faculties,
                                     User currentUser) {
        Log.d(TAG, "computeLastMessages: working");

        Gson gson = new Gson();

        writeToSharedPreferences(gson.toJson(currentUser));

        final Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("userColleges", (Serializable) colleges);
        intent.putExtra("userFaculties", (Serializable) faculties);
        intent.putExtra("userGroups", (Serializable) groups);

        intent.putExtra("setupJustCompleted", false);
        for (int i = 0; i < groups.size(); i++) {
            intent.putExtra("id_" + i, groups.get(i).getId());
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i < groups.size(); i++) {
                    Log.d(TAG, "doInBackground: async process");
                    Log.d(TAG, "doInBackground: groupId -> " + groups.get(i).getId());
                    Message message = repository.getLastMessageForGroup(groups.get(i).getId());
                    if (message != null) {
                        groups.get(i).setLastMessage(message);
                        Log.d(TAG, "doInBackground: Message -> " + message.getMessage());
                    } else {
                        groups.get(i).setLastMessage(null);
                        Log.d(TAG, "doInBackground: null message");
                    }
                }

                startActivityForResult(intent, MAIN_ACTIVITY_REQ_CODE);

                return null;
            }
        }.execute();


    }

    private void bindViews() {
        signIn = findViewById(R.id.btn_log_in);
        fbLogin = findViewById(R.id.ll_fb_login);
        googleLogin = findViewById(R.id.ll_google_login);
        register = findViewById(R.id.tv_register);
        email = findViewById(R.id.et_email_log_in);
        password = findViewById(R.id.et_password_log_in);
        forgotPassword = findViewById(R.id.tv_forgot_password);
        logo = findViewById(R.id.iv_logo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if (requestCode == MAIN_ACTIVITY_REQ_CODE && resultCode == RESULT_CANCELED) {
            userGroups.clear();
            userFaculties.clear();
            userColleges.clear();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            post(account);
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null, false);
        }
    }

    // updateUI executes before post method in handleSignInResult so dialog with account
    // created shows even if account was previously created
    private void updateUI(GoogleSignInAccount account, boolean isInDB) {
        if (account == null) {
            String message = "There was a problem signing you in." +
                    " Please try again later.", title = "Google Authentication";
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setTitle(title)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            dialog.create().show();
        }
        else {
            String message, title = "Google Authentication";
            if (!isInDB) {
                message = "Your account has been successfully created" +
                        " with Google";

                AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setTitle(title)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialog.create().show();
            } else {
                Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void post(GoogleSignInAccount account) {

        String lastName = account.getFamilyName();
        String firstName = account.getGivenName();
        String email = account.getEmail();
        String passwordId = account.getId();
        String gender = "null";

        Log.d(TAG, "post: google last name -> " + lastName);
        Log.d(TAG, "post: given name -> " + account.getGivenName());
        Log.d(TAG, "post: display name -> " + account.getDisplayName());
        Log.d(TAG, "post: account id -> " + account.getId());

        String url = dbLinks.getBaseLink() + "register-user-google?" +
                "lastName=" + lastName + "&firstName=" + firstName
                + "&password=" + passwordId + "&gender=" + gender
                + "&email=" + email + "&profileSetupComplete=false"
                + "&joinedCollege=false&joinedCollegeName=&joinedFaculty=false"
                +"&joinedFacultyName=&joinedGroup=false&joinedGroupsAccessTokens=&verified=false";

        Log.d(TAG, "post: url -> " + url);
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
                    JSONObject object = new JSONObject(json);
                    boolean exists = object.getBoolean("exists");
                    boolean accountCreated = object.getBoolean("newAccountCreated");

                    if (exists) {
                        JSONArray userArray = object.getJSONArray("user");

                        User googleUser = new Gson().fromJson(userArray.getJSONObject(0).toString(),
                                User.class);
                        List<College> colleges = getColleges(object);
                        List<Faculty> faculties = getFaculties(object);
                        List<Group> groups = getGroups(object);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (!googleUser.isProfileSetupComplete()) {
                            Intent intent = new Intent(WelcomeActivity.this, FindCollegesActivity.class);
                            intent.putExtra("currentUser", googleUser);
                            startActivity(intent);
                        }
                        else {
                            computeLastMessages(groups, colleges, faculties, googleUser);
                        }
                    }

                    if (accountCreated) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = "Your account has been successfully created" +
                                        " with Google";

                                AlertDialog.Builder dialog = new AlertDialog.Builder(WelcomeActivity.this)
                                        .setMessage(message)
                                        .setTitle("Success")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                dialog.create().show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void postFB(String url, String pass, String email, String gender, String lastName, String firstName) {
        OkHttpClient client1 = new OkHttpClient();

        url = url + "?email=" + email + "&password=" + pass + "&gender=" + gender + "&lastName=" + lastName +
                "&firstName=" + firstName + "&profileSetupComplete=false"
                + "&joinedCollege=false&joinedCollegeName=&joinedFaculty=false"
                +"&joinedFacultyName=&joinedGroup=false&joinedGroupsAccessTokens=&verified=true"  ;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Log.d(TAG, "postFB: url -> " + url);

        Call call = client1.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject res = new JSONObject(json);
                    Log.d(TAG, "onResponse: response -> " + res.toString());
                    JSONObject user = res.getJSONArray("user").getJSONObject(0);
                    boolean newAccountCreated = res.getBoolean("newAccountCreated");
                    boolean exists = res.getBoolean("exists");
                    if (!newAccountCreated && exists) {
                        User fbUser = new User();
                        fbUser.setEmail(user.getString("email"));
                        fbUser.setFirstName(user.getString("firstName"));
                        fbUser.setId(user.getString("_id"));
                        fbUser.setLastName(user.getString("lastName"));
                        fbUser.setPassword(user.getString("password"));
                        fbUser.setGender(user.getString("gender"));
                        fbUser.setProfileSetupComplete(user.getBoolean("profileSetupComplete"));
                        fbUser.setJoinedCollege(user.getBoolean("joinedCollege"));
                        fbUser.setJoinedCollegeName(user.getString("joinedCollegeName"));
                        fbUser.setJoinedFaculty(user.getBoolean("joinedFaculty"));
                        fbUser.setJoinedFacultyName(user.getString("joinedFacultyName"));
                        fbUser.setJoinedGroup(user.getBoolean("joinedGroup"));
                        fbUser.setJoinedGroupsAccessTokens(user.getString("joinedGroupsAccessTokens"));
                        fbUser.setVerified(true);
                        if (fbUser.isJoinedCollege())
                            userColleges.addAll(getColleges(res));
                        if (fbUser.isJoinedFaculty())
                            userFaculties.addAll(getFaculties(res));
                        if (fbUser.isJoinedGroup())
                            userGroups.addAll(getGroups(res));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (!fbUser.isProfileSetupComplete()) {
                            Intent intent = new Intent(WelcomeActivity.this, FindCollegesActivity.class);
                            intent.putExtra("currentUser", fbUser);
                            startActivity(intent);
                        }
                        else {
                            computeLastMessages(userGroups, userColleges, userFaculties, fbUser);
                        }
                    }

                    if (!exists && newAccountCreated) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<College> getColleges(JSONObject res) {
        List<College> colleges = new ArrayList<>();

        try {
            JSONArray serverArray = res.getJSONArray("joinedColleges");

            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject object = serverArray.getJSONObject(i);
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
                college.setPhoneNumber(object.getString("phoneNumber"));
                college.setWebsiteUrl(object.getString("websiteUrl"));

                colleges.add(college);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return colleges;
    }

    private List<Faculty> getFaculties(JSONObject res) {
        List<Faculty> faculties = new ArrayList<>();

        try {
            JSONArray serverArray = res.getJSONArray("joinedFaculties");
            for (int i = 0; i < serverArray.length(); i++) {
                Faculty faculty = new Faculty();
                JSONObject object = serverArray.getJSONObject(i);
                faculty.setReferencedCollegeName(object.getString("facultyReferencedCollegeName"));
                faculty.setFacultyDescription(object.getString("facultyDescription"));
                faculty.setNumMembers(Integer.parseInt(object.getString("facultyNumMembers")));
                faculty.setNumGroups(Integer.parseInt(object.getString("facultyNumGroups")));
                faculty.setFacultyName(object.getString("facultyName"));
                faculty.setId(object.getString("_id"));
                faculties.add(faculty);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faculties;
    }

    private List<Group> getGroups(JSONObject res) {
        List<Group> groups = new ArrayList<>();

        try {
            JSONArray serverArray = res.getJSONArray("joinedGroups");
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject object = serverArray.getJSONObject(i);
                Group group = new Group();

                group.setNumMembers(Integer.parseInt(object.getString("numMembers")));
                group.setGroupDescription(object.getString("groupDescription"));
                group.setReferencedFacultyName(object.getString("referencedFacultyName"));
                group.setGroupSecondaryAdministrator(object.getString("groupSecondaryAdministrator"));
                group.setSecondaryAdministrator(Boolean.parseBoolean(object.getString("hasSecondaryAdministrator")));
                group.setGroupAdministrator(object.getString("groupAdministrator"));
                group.setGroupType(object.getString("groupType"));
                group.setGroupName(object.getString("groupName"));
                group.setReferencedCollegeName(object.getString("referencedCollegeName"));
                group.setAccessToken(object.getString("groupAccessToken"));
                group.setId(object.getString("_id"));

                groups.add(group);

                FirebaseMessaging.getInstance().subscribeToTopic(group.getId());
                Log.d(TAG, "getGroups: subscribed to -> " + group.getId());
                createNotificationChannel(group.getId());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return groups;
    }

    private void createNotificationChannel(String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
    }

    private boolean isEmailValid() {
        int pass = 0;
        if (email.getText().toString().split("\\.").length == 2) {
            if (email.getText().toString().split("\\.")[1].equals("com"))
                pass++;
        }
        if (email.getText().toString().split("@").length == 2)
            pass++;
        return pass == 2;
    }

    private void showMessage(boolean isInDB) {
        if (isInDB) {
            Toast.makeText(this, "Signed In successfully", Toast.LENGTH_SHORT).show();
        } else {
            String message, title = "Google Authentication";
            message = "Your account has been successfully created" +
                    " with Google";

            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setTitle(title)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            dialog.create().show();
        }
    }

    private void writeToSharedPreferences(String userJSONData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.saved_user_json), userJSONData);
        editor.apply();
    }
}
