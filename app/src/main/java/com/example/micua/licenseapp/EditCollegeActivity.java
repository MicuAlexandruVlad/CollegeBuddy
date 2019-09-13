package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditCollegeActivity extends AppCompatActivity {

    public static final String TAG = "EditCollege";

    private EditText collegeCountry, collegeCity,
        collegeAddress, collegeDescription;
    private AutoCompleteTextView collegeName;
    private ImageView collegePhoto;
    private Button finishEditing;

    private String[] colleges;
    private DBLinks dbLinks;
    private College currentCollege;
    private User currentUser;
    private ArrayAdapter<String> adapter;
    private boolean imageTouch = false, photoUrlChanged = false;
    private String url, address, city, country, desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_college);

        Intent parentIntent = getIntent();

        currentCollege = (College) parentIntent.getSerializableExtra("currentCollege");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        colleges = new String[11089];
        colleges = parentIntent.getStringArrayExtra("collegesAll");

        dbLinks = new DBLinks();
        reqColleges(dbLinks.getBaseLink() + "colleges-all");
        bindViews();
        initAutoCompleteView();
        if (!currentCollege.getPhotoUrls().equalsIgnoreCase("")) {
            url = currentCollege.getPhotoUrls();
            Picasso.get().load(url).into(collegePhoto);
        }
        collegeAddress.setText(currentCollege.getAddress());
        collegeCountry.setText(currentCollege.getCountry());
        collegeCity.setText(currentCollege.getCity());
        collegeDescription.setText(currentCollege.getDescription());
        collegeName.setText(currentCollege.getName());

        getInitialDetails();

        finishEditing.setBackgroundResource(R.drawable.btn_round_primary);


        collegePhoto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (!imageTouch) {
                    if (collegeName.getText().toString().equals("")) {
                        Toast.makeText(EditCollegeActivity.this, "Please complete" +
                                " the college name first, before adding a photo.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        imageTouch = true;
                        String url = "https://www.google.com/search?tbm=isch&as_q=" +
                                collegeName.getText().toString() + "&tbs=isz:lt,islt:2mp,sur:fmc";
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                    }
                } else {
                    imageTouch = false;
                    Picasso.get().load(getUrlFromClipboard()).into(collegePhoto);
                    photoUrlChanged = true;
                    if (url.equals(currentCollege.getPhotoUrls())) {
                        finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                        finishEditing.setText("CANCEL");
                    }
                    else {
                        finishEditing.setText("FINISH EDITING");
                        finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                    }
                    Log.d(TAG, "Image url: " + getUrlFromClipboard());
                }
            }
        });

        finishEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameVal, countryVal, cityVal, addressVal, descriptionVal,
                        photoUrlVal = "";

                nameVal = collegeName.getText().toString();
                countryVal = collegeCountry.getText().toString();
                cityVal = collegeCity.getText().toString();
                addressVal = collegeAddress.getText().toString();
                descriptionVal = collegeDescription.getText().toString();
                if (photoUrlChanged)
                    photoUrlVal = url;
                else
                    photoUrlVal = currentCollege.getPhotoUrls();

                if (!isInfoDifferent()) {
                    finish();
                } else {
                    currentCollege.setLastEditedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                    String url = dbLinks.getBaseLink() + "college-edit-data?"
                            + "collegeName=" + currentCollege.getName()
                            + "&collegeCountry=" + countryVal
                            + "&collegeAddress=" + addressVal
                            + "&collegeLastEditedBy=" + currentCollege.getLastEditedBy()
                            + "&collegeOriginallyAddedBy=" + currentCollege.getOriginallyAddedBy()
                            + "&collegeCity=" + cityVal
                            + "&collegeDescription=" + descriptionVal
                            + "&collegeNumMembers=" + currentCollege.getNumMembers()
                            + "&collegeNumGroups=" + currentCollege.getNumGroups()
                            + "&collegePhotoUrl=" + photoUrlVal
                            + "&collegeNumFaculties=" + currentCollege.getNumFaculties();

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
                                            AlertDialog.Builder builder = new AlertDialog.Builder(EditCollegeActivity.this)
                                                    .setTitle("Success")
                                                    .setMessage("College has been successfully updated")
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
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(EditCollegeActivity.this)
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

        collegeCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isInfoDifferent()) {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                    finishEditing.setText("CANCEL");
                }
                else {
                    finishEditing.setText("FINISH EDITING");
                    finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        collegeCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isInfoDifferent()) {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                    finishEditing.setText("CANCEL");
                }
                else {
                    finishEditing.setText("FINISH EDITING");
                    finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        collegeAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isInfoDifferent()) {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                    finishEditing.setText("CANCEL");
                }
                else {
                    finishEditing.setText("FINISH EDITING");
                    finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        collegeDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isInfoDifferent()) {
                    finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
                    finishEditing.setText("CANCEL");
                }
                else {
                    finishEditing.setText("FINISH EDITING");
                    finishEditing.setBackgroundResource(R.drawable.btn_round_primary);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @SuppressLint("SetTextI18n")
    private void getInitialDetails() {
        address = currentCollege.getAddress();
        city = currentCollege.getCity();
        country = currentCollege.getCountry();
        desc = currentCollege.getDescription();
    }

    @SuppressLint("SetTextI18n")
    private void setButtonDrawableDisabled() {
        finishEditing.setBackgroundResource(R.drawable.btn_round_deactivated);
        finishEditing.setText("Cancel");
    }

    private void bindViews() {
        finishEditing = findViewById(R.id.btn_edit_college_finish);
        collegeDescription = findViewById(R.id.et_college_description);
        collegeName = findViewById(R.id.actv_college_name);
        collegeCity = findViewById(R.id.et_college_city);
        collegeCountry = findViewById(R.id.et_college_country);
        collegeAddress = findViewById(R.id.et_college_address);
        collegePhoto = findViewById(R.id.iv_college);
    }

    private void reqColleges(String url) {
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
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        colleges[i] = object.getString("collegeName");
                    }
                    Log.d(TAG, "Colleges: " + array.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getUrlFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert clipboard != null;
        url = (String) Objects.requireNonNull(clipboard.getPrimaryClip())
                .getItemAt(clipboard.getPrimaryClip().getItemCount() - 1).getText();
        return url;
    }

    private boolean isInfoDifferent() {
        if (country.equalsIgnoreCase(collegeCountry.getText().toString())
                && city.equalsIgnoreCase(collegeCity.getText().toString())
                && address.equalsIgnoreCase(collegeAddress.getText().toString())
                && desc.equalsIgnoreCase(collegeDescription.getText().toString())
                && !photoUrlChanged)
            return false;
        return true;
    }

    private void initAutoCompleteView() {
        adapter = new ArrayAdapter<>(EditCollegeActivity.this
                , android.R.layout.simple_dropdown_item_1line, colleges);
        collegeName.setAdapter(adapter);
        collegeName.setThreshold(4);
    }
}
