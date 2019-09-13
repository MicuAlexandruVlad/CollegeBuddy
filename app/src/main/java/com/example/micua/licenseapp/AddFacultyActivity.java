package com.example.micua.licenseapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class AddFacultyActivity extends AppCompatActivity {

    private EditText name, description;
    private Button addFaculty;
    private CheckBox autoEnroll;

    private DBLinks dbLinks;
    private College currentCollege;
    private User currentUser;
    private Faculty faculty;
    private Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        Objects.requireNonNull(getSupportActionBar()).hide();

        parentIntent = getIntent();
        dbLinks = new DBLinks();
        currentCollege = (College) parentIntent.getSerializableExtra("currentCollege");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        bindViews();

        addFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nameVal, descVal;
                nameVal = name.getText().toString();
                descVal = description.getText().toString();

                if (nameVal.equalsIgnoreCase("")) {
                    Toast.makeText(AddFacultyActivity.this, "Name field is empty", Toast.LENGTH_SHORT).show();
                }
                else if (descVal.equalsIgnoreCase("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddFacultyActivity.this)
                            .setTitle("Optional Field Empty")
                            .setMessage("Looks like the description field is empty." +
                                    " Note that anyone can edit the fields of this faculty and add any extra info at any time." +
                                    "  Would you like to leave it uncompleted ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String url = dbLinks.getBaseLink() + "register-faculty";
                                    faculty = new Faculty();
                                    faculty.setFacultyName(nameVal);
                                    faculty.setNumGroups(0);
                                    faculty.setOriginallyAddedBby(currentUser.getFirstName() + " "
                                     + currentUser.getLastName());
                                    faculty.setLastEditedBy("");
                                    faculty.setFacultyDescription(description.getText().toString());
                                    if (autoEnroll.isChecked()) {
                                        faculty.setNumMembers(1);
                                        // TODO: also update the user faculty joined fields
                                }

                                    else
                                        faculty.setNumMembers(0);
                                    faculty.setReferencedCollegeName(currentCollege.getName());
                                    post(url, faculty);
                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    private void post(String url, Faculty faculty) {

        if (autoEnroll.isChecked()) {
            AsyncHttpClient client2 = new AsyncHttpClient();
            RequestParams params1 = new RequestParams();
            if (!currentUser.isJoinedFaculty())
                currentUser.setJoinedFacultyName(faculty.getFacultyName());
            else {
                currentUser.setJoinedFacultyName(currentUser.getJoinedFacultyName() + "_"
                        + faculty.getFacultyName());
            }
            params1.put("joinedFacultyName", currentUser.getJoinedFacultyName());
            params1.put("joinedFaculty", true);
            params1.put("email", currentUser.getEmail());

            client2.post(dbLinks.getBaseLink() + "update-user-faculty", params1, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("facultyName", faculty.getFacultyName());
        params.put("facultyNumMembers", faculty.getNumMembers());
        params.put("facultyReferencedCollegeName", faculty.getReferencedCollegeName());
        params.put("facultyNumGroups", faculty.getNumGroups());
        params.put("originallyAddedBy", faculty.getOriginallyAddedBy());
        params.put("lastEditedBy", faculty.getLastEditedBy());
        params.put("facultyDescription", faculty.getFacultyDescription());
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                OkHttpClient client1 = new OkHttpClient();
                Request request = new Request.Builder().url(dbLinks.getBaseLink() + "faculty-check").build();
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
                            boolean isFacultyInDb = Boolean.parseBoolean(object.getString("isFacultyInDb"));
                            if (!isFacultyInDb) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(AddFacultyActivity.this);
                                builder.setTitle("Success")
                                        .setMessage("Your faculty has been successfully added to our database.")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                updateCollegeNumFaculties(dbLinks.getBaseLink() + "update-college", currentCollege);
                                            }
                                        });
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder.create().show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void updateCollegeNumFaculties(String url, College college) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("collegeName", college.getName());
        params.put("collegeNumMembers", college.getNumMembers());
        params.put("collegeNumFaculties", college.getNumFaculties() + 1);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    Intent returnIntent = getIntent();
                    returnIntent.putExtra("addedFaculty", faculty);
                    returnIntent.putExtra("autoJoin", autoEnroll.isChecked());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void bindViews() {
        name = findViewById(R.id.et_faculty_name);
        description = findViewById(R.id.et_faculty_description);
        addFaculty = findViewById(R.id.btn_add_faculty);
        autoEnroll = findViewById(R.id.cb_auto_enroll_me);
    }
}
