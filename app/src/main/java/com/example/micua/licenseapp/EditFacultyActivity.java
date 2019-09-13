package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO: test if this works properly
public class EditFacultyActivity extends AppCompatActivity {

    public static final String TAG = "EditFacultyActivity";

    private EditText facultyName, facultyDescription;
    private Button finishEditing;

    private String desc;
    private Faculty currentFaculty;
    private User currentUser;
    private Intent parentIntent;
    private College currentCollege;
    private DBLinks dbLinks;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_faculty);

        bindViews();

        facultyName.setClickable(false);
        facultyName.setFocusable(false);

        parentIntent = getIntent();

        dbLinks = new DBLinks();

        currentFaculty = (Faculty) parentIntent.getSerializableExtra("currentFaculty");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        Log.d(TAG, "onCreate: Faculty id -> " + currentFaculty.getId());

        facultyDescription.setText(currentFaculty.getFacultyDescription());
        facultyName.setText(currentFaculty.getFacultyName());

        getInitialDetails();

        finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
        finishEditing.setText("Cancel");

        facultyDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isInfoDifferent()) {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                    finishEditing.setText("FINISH EDITING");
                } else {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                    finishEditing.setText("Cancel");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        finishEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String descVal;
                descVal = facultyDescription.getText().toString();

                if (!isInfoDifferent())
                    finish();
                else {
                    currentFaculty.setLastEditedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                    currentFaculty.setFacultyDescription(descVal);
                    String url = dbLinks.getBaseLink() + "faculty-edit-data?"
                            + "facultyId=" + currentFaculty.getId()
                            + "&facultyDescription=" + descVal
                            + "&lastEditedBy=" + currentFaculty.getLastEditedBy();

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
                                boolean updated = object.getBoolean("updated");
                                if (updated) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(EditFacultyActivity.this)
                                                    .setTitle("Success")
                                                    .setMessage("Faculty has been successfully updated")
                                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            setResult(RESULT_OK, parentIntent);
                                                            finish();
                                                        }
                                                    });
                                            builder.create().show();
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(EditFacultyActivity.this)
                                                    .setTitle("Error")
                                                    .setMessage("Something happened. Please try again.")
                                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    });
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
            }
        });
    }

    private void bindViews() {
        facultyName = findViewById(R.id.et_faculty_name);
        facultyDescription = findViewById(R.id.et_faculty_description);
        finishEditing = findViewById(R.id.btn_finish_editing_faculty);
    }

    private boolean isInfoDifferent() {
        if (desc.equalsIgnoreCase(facultyDescription.getText().toString()))
            return false;
        return true;
    }

    private void getInitialDetails() {
        desc = currentFaculty.getFacultyDescription();
    }
}
