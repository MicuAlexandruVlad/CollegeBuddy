package com.example.micua.licenseapp;

import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

public class EditCollegeDetailsActivity extends AppCompatActivity {
    public static final String TAG = "EditCollegeDetails";

    private EditText collegeName;
    private MaterialEditText collegeDescription ,collegeCountry, collegePhoneNumber;
    private MaterialEditText collegeWebsite, collegeAddress;
    private ImageView collegeImage;
    private Button done;

    private Intent parentIntent;
    private College currentCollege;
    private User currentUser;
    private boolean photoChanged = false;
    private boolean launchedBrowser = false;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_college_details);

        Objects.requireNonNull(getSupportActionBar()).hide();

        showDialog();
        bindViews();

        parentIntent = getIntent();
        currentCollege = (College) parentIntent.getSerializableExtra("currentCollege");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        if (!currentCollege.getPhotoUrls().equalsIgnoreCase("")) {
            Glide.with(this).load(currentCollege.getPhotoUrls()).into(collegeImage);
        }

        collegeName.setText(currentCollege.getName());
        collegePhoneNumber.setText(currentCollege.getPhoneNumber());
        collegeWebsite.setText(currentCollege.getWebsiteUrl());
        collegeCountry.setText(currentCollege.getCountry());
        collegeAddress.setText(currentCollege.getAddress());
        collegeDescription.setText(currentCollege.getDescription());

        collegeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (collegeName.getText().toString().equals(""))
                    Toast.makeText(EditCollegeDetailsActivity.this,
                            "Please set the name first", Toast.LENGTH_SHORT).show();
                else if (!launchedBrowser){
                    String url = "https://www.google.com/search?tbm=isch&as_q=" +
                            collegeName.getText().toString() + "&tbs=isz:lt,islt:1mp,sur:fmc";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }

                if (launchedBrowser) {
                    photoChanged = true;
                    imageUrl = getUrlFromClipboard();

                    if (imageUrl.contains("https://") || imageUrl.contains("http://")) {
                        if (imageUrl.contains(".png") || imageUrl.contains(".jpg") ||
                                imageUrl.contains(".jpeg") || imageUrl.contains(".JPG") ||
                                imageUrl.contains(".PNG") || imageUrl.contains(".JPEG")) {
                            currentCollege.setPhotoUrls(imageUrl);
                            Glide.with(EditCollegeDetailsActivity.this).load(imageUrl).into(collegeImage);
                        }
                        else
                            Toast.makeText(EditCollegeDetailsActivity.this, "Not image", Toast.LENGTH_SHORT).show();
                    }

                }

                launchedBrowser = !launchedBrowser;
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (collegeName.getText().toString().equals(""))
                    Toast.makeText(EditCollegeDetailsActivity.this,
                            "College name can not be empty", Toast.LENGTH_SHORT).show();
                else {
                    currentCollege.setLastEditedBy(currentUser.getId());
                    currentCollege.setPhoneNumber(collegePhoneNumber.getText().toString());
                    currentCollege.setAddress(collegeAddress.getText().toString());
                    currentCollege.setCountry(collegeCountry.getText().toString());
                    currentCollege.setDescription(collegeDescription.getText().toString());
                    currentCollege.setName(collegeName.getText().toString());
                    currentCollege.setWebsiteUrl(collegeWebsite.getText().toString());

                    Log.d(TAG, "onClick: edited college -> " + new Gson().toJson(currentCollege));
                    parentIntent.putExtra("editedCollege", currentCollege);
                    setResult(RESULT_OK, parentIntent);
                    finish();
                }
            }
        });
    }

    private void showDialog() {
        ImportantNoticeDialog dialog = new ImportantNoticeDialog(this);
        dialog.setTitle("Important Notice");
        dialog.setMessage("Please make sure all the data your are about to add/modify is 100% real.");
        dialog.create();
        dialog.show();
    }

    private void bindViews() {
        collegeName = findViewById(R.id.et_college_name);
        collegeDescription = findViewById(R.id.met_college_description);
        collegeAddress = findViewById(R.id.met_college_address);
        collegeCountry = findViewById(R.id.met_college_country);
        collegeWebsite = findViewById(R.id.met_college_website);
        collegePhoneNumber = findViewById(R.id.met_college_phone_number);
        collegeImage = findViewById(R.id.iv_college_photo);
        done = findViewById(R.id.btn_done);
    }

    private String getUrlFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert clipboard != null;
        SpannableString imageURL;
        imageURL = (SpannableString) Objects.requireNonNull(clipboard.getPrimaryClip())
                .getItemAt(clipboard.getPrimaryClip().getItemCount() - 1).getText();
        Log.d(TAG, "getUrlFromClipboard: image url -> " + imageURL);
        return imageURL.toString();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
