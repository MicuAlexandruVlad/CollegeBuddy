package com.example.micua.licenseapp;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddCollegeActivity extends AppCompatActivity {

    private EditText collegeCity, collegeCountry, collegeDesc, collegeAddress, phoneNumber, website;
    private CheckBox autoAddToCollege;
    private ImageView collegePhoto;
    private Button addCollege;
    private AutoCompleteTextView collegeName;

    private String addedBy, lastEditedBy;
    private String imageURL;
    private ArrayAdapter<String> adapter;
    private String[] colleges;
    private boolean imageTouch = false, autoJoin = false;
    private College college;
    private User currentUser;
    private DBLinks dbLinks;

    public static final String TAG = "AddCollege";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_college);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        showAlertDialog();
        bindViews();

        colleges = getIntent().getStringArrayExtra("colleges");
        initAutoCompleteView();

        dbLinks = new DBLinks();
        college = new College();
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        addCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name, city, country, desc, address, phone, webUrl;
                name = collegeName.getText().toString();
                city = collegeCity.getText().toString();
                country = collegeCountry.getText().toString();
                desc = collegeDesc.getText().toString();
                address = collegeAddress.getText().toString();
                phone = phoneNumber.getText().toString();
                webUrl = website.getText().toString();

                Log.d(TAG, "onClick: name -> " + name);

                if (name.equals("") || city.equals("") || country.equals(""))
                    Toast.makeText(AddCollegeActivity.this, "One or more " +
                            "fields are empty", Toast.LENGTH_SHORT).show();
                else if (desc.equals("") || address.equals("") || phone.equals("") || webUrl.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCollegeActivity.this)
                            .setTitle("Empty optional fields")
                            .setMessage("One or more optional fields are empty. After the college" +
                                    " is added anybody can edit or complete the rest of the form. " +
                                    "Would you like to leave them uncompleted ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // add college to db
                                    setCollegeData(webUrl, phone, name, address, city, country, desc);
                                    String url = dbLinks.getBaseLink() + "add-college";
                                    post(url, college);
                                }
                            });
                    builder.create().show();
                }
                if (!name.equals("") && !city.equals("") && !country.equals("") && !desc.equals("") &&
                    !address.equals("") && !phone.equals("") && !webUrl.equals("")) {
                    setCollegeData(webUrl, phone, name, address, city, country, desc);
                    String url = dbLinks.getBaseLink() + "add-college";
                    post(url, college);
                }
            }
        });

        collegePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imageTouch) {
                    if (collegeName.getText().toString().equals("")) {
                        Toast.makeText(AddCollegeActivity.this, "Please complete" +
                                " the college name first, before adding a photo.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        imageTouch = true;
                        String url = "https://www.google.com/search?tbm=isch&as_q=" +
                                collegeName.getText().toString() + "&tbs=isz:lt,islt:1mp,sur:fmc";
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                    }
                } else {
                    imageTouch = false;
                    Glide.with(AddCollegeActivity.this).load(getUrlFromClipboard()).into(collegePhoto);
                    Log.d(TAG, "Image url: " + getUrlFromClipboard());
                }
            }
        });
    }

    private void setCollegeData(String webUrl, String phone, String name, String address, String city, String country,
                                String desc) {
        college.setLastEditedBy(currentUser.getFirstName()
                + " " + currentUser.getLastName());
        college.setWebsiteUrl(webUrl);
        college.setPhoneNumber(phone);
        college.setName(name);
        college.setAddress(address);
        college.setCity(city);
        college.setCountry(country);
        college.setOriginallyAddedBy(currentUser.getFirstName()
                + " " + currentUser.getLastName());
        college.setNumPhotos(1);
        college.setDescription(desc);
        if (autoAddToCollege.isChecked()) {
            autoJoin = true;
            currentUser.setJoinedCollege(true);
            currentUser.setJoinedCollegeName(currentUser.getJoinedCollegeName() + "_"
                    + college.getName());
            college.setNumMembers(1);
            String updateUrl = dbLinks.getBaseLink() + "update-user";
            updateUser(updateUrl, currentUser);
        }

        else
            college.setNumMembers(0);
        college.setPhotoUrls(imageURL.toString());
        college.setNumGroups(0);
        college.setNumFaculties(0);
    }

    private void updateUser(String updateUrl, final User currentUser) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", currentUser.getEmail());
        params.put("joinedCollege", currentUser.isJoinedCollege());
        params.put("joinedCollegeName", currentUser.getJoinedCollegeName());

        asyncHttpClient.post(updateUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "User with email " + currentUser.getEmail() + " has been updated");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void post(String url, final College college) {
        Log.d(TAG, "post: working");

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("collegeName", college.getName());
        params.put("collegeCity", college.getCity());
        params.put("collegeCountry", college.getCountry());
        params.put("collegeDescription", college.getDescription());
        params.put("collegeAddress", college.getAddress());
        params.put("collegePhotoUrl", college.getPhotoUrls());
        params.put("collegeNumMembers", college.getNumMembers());
        params.put("collegeOriginallyAddedBy", college.getOriginallyAddedBy());
        params.put("collegeLastEditedBy", college.getLastEditedBy());
        params.put("collegeNumGroups", college.getNumGroups());
        params.put("collegeNumFaculties", college.getNumFaculties());
        params.put("phoneNumber", college.getPhoneNumber());
        params.put("websiteUrl", college.getWebsiteUrl());

        asyncHttpClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                OkHttpClient client = new OkHttpClient();
                String url = dbLinks.getBaseLink() + "college-check";
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
                            JSONObject jsonObject = new JSONObject(json);
                            final boolean isInDB = Boolean.parseBoolean(jsonObject.getString("isCollegeInDb"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCollegeActivity.this);

                                    if (!isInDB) {
                                        builder.setMessage("Your college has been successfully added" +
                                                " to database.")
                                                .setTitle("Success");
                                    }
                                    else {
                                        builder.setMessage("College already in database.")
                                                .setTitle("Oops");
                                    }
                                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = getIntent();
                                            intent.putExtra("addedCollege", college);
                                            intent.putExtra("autoJoin", autoJoin);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    });
                                    builder.create().show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Failed uploading college data to database");
            }
        });
    }

    private void bindViews() {
        collegeName = findViewById(R.id.actv_college_name);
        collegeAddress = findViewById(R.id.et_college_address);
        collegeCity = findViewById(R.id.et_college_city);
        collegeCountry = findViewById(R.id.et_college_country);
        collegeDesc = findViewById(R.id.et_college_description);
        phoneNumber = findViewById(R.id.et_college_phone_number);
        website = findViewById(R.id.et_college_website);
        autoAddToCollege = findViewById(R.id.cb_auto_enroll_me);
        collegePhoto = findViewById(R.id.iv_college);
        addCollege = findViewById(R.id.btn_add_college_finish);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddCollegeActivity.this)
                .setTitle("Notice")
                .setMessage("Please make sure all the data that you are about to add is real.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private String getUrlFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert clipboard != null;
        imageURL = (String) Objects.requireNonNull(clipboard.getPrimaryClip())
                .getItemAt(clipboard.getPrimaryClip().getItemCount() - 1).getText();
        return imageURL;
    }

    private void initAutoCompleteView() {
        adapter = new ArrayAdapter<>(AddCollegeActivity.this
                , android.R.layout.simple_dropdown_item_1line, colleges);
        collegeName.setAdapter(adapter);
        collegeName.setThreshold(4);
    }
}
