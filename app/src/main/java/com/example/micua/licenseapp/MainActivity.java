package com.example.micua.licenseapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ViewPager pager;
    private TabLayout tabLayout;
    private RelativeLayout loadingPanel;

    private Intent parentIntent;
    private MainPagerAdapter pagerAdapter;
    private User currentUser;
    private DBLinks dbLinks;
    private List<Group> joinedGroups;
    private List<College> colleges;
    private List<Faculty> faculties;
    private boolean fromProfileSetup;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main);

        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        dbLinks = new DBLinks();

        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        pager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);
        loadingPanel = findViewById(R.id.loadingPanel);



        fromProfileSetup = parentIntent.getBooleanExtra("setupJustCompleted", false);

        if (!fromProfileSetup) {
            joinedGroups = (List<Group>) parentIntent.getSerializableExtra("userGroups");
            for (int i = 0; i < joinedGroups.size(); i++) {
                joinedGroups.get(i).setId(parentIntent.getStringExtra("id_" + i));
            }
            Log.d(TAG, "onCreate: first group id -> " + joinedGroups.get(0).getId());
            colleges = (List<College>) parentIntent.getSerializableExtra("userColleges");
            faculties = (List<Faculty>) parentIntent.getSerializableExtra("userFaculties");
            Bundle bundleGroups = new Bundle();
            Bundle bundleColleges = new Bundle();
            Bundle bundleFaculties = new Bundle();
            bundleGroups.putSerializable("groups", (Serializable) joinedGroups);
            bundleColleges.putSerializable("colleges", (Serializable) colleges);
            bundleFaculties.putSerializable("faculties", (Serializable) faculties);
            bundleGroups.putSerializable("currentUser", currentUser);
            bundleColleges.putSerializable("currentUser", currentUser);
            bundleFaculties.putSerializable("currentUser", currentUser);
            pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), bundleGroups, bundleColleges, bundleFaculties);

            pager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(pager);
            tabLayout.setVisibility(View.VISIBLE);
            pager.setVisibility(View.VISIBLE);
            loadingPanel.setVisibility(View.GONE);

            Objects.requireNonNull(tabLayout.getTabAt(0)).setText("Groups");
            Objects.requireNonNull(tabLayout.getTabAt(1)).setText("Faculties");
            Objects.requireNonNull(tabLayout.getTabAt(2)).setText("Colleges");
        }
        else {
            getDataForUser();
        }




    }

    private void getDataForUser() {
        String url = dbLinks.getBaseLink() + "user-data-to-check?email=" + currentUser.getEmail() +
                "&password=" + currentUser.getPassword();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                List<College> colleges = new ArrayList<>();
                List<Faculty> faculties = new ArrayList<>();
                List<Group> groups = new ArrayList<>();
                String json = response.body().string();
                Log.d(TAG, "onResponse: json email login -> " + json);
                try {
                    JSONObject obj = new JSONObject(json);
                    if (currentUser.isJoinedCollege())
                        colleges.addAll(getColleges(obj));
                    if (currentUser.isJoinedFaculty())
                        faculties.addAll(getFaculties(obj));
                    if (currentUser.isJoinedGroup())
                        groups.addAll(getGroups(obj));
                    final Bundle bundleGroups = new Bundle();
                    final Bundle bundleColleges = new Bundle();
                    final Bundle bundleFaculties = new Bundle();
                    bundleGroups.putSerializable("groups", (Serializable) groups);
                    bundleColleges.putSerializable("colleges", (Serializable) colleges);
                    bundleFaculties.putSerializable("faculties", (Serializable) faculties);
                    bundleGroups.putSerializable("currentUser", currentUser);
                    bundleColleges.putSerializable("currentUser", currentUser);
                    bundleFaculties.putSerializable("currentUser", currentUser);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), bundleGroups, bundleColleges, bundleFaculties);
                            pager.setAdapter(pagerAdapter);
                            tabLayout.setupWithViewPager(pager);
                            tabLayout.setVisibility(View.VISIBLE);
                            pager.setVisibility(View.VISIBLE);
                            loadingPanel.setVisibility(View.GONE);

                            Objects.requireNonNull(tabLayout.getTabAt(0)).setText("Groups");
                            Objects.requireNonNull(tabLayout.getTabAt(1)).setText("Faculties");
                            Objects.requireNonNull(tabLayout.getTabAt(2)).setText("Colleges");
                        }
                    });
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
                college.setWebsiteUrl(object.getString("websiteUrl"));
                college.setPhoneNumber(object.getString("phoneNumber"));

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
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return groups;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111 && resultCode == RESULT_OK) {
            if (data != null) {
                Message message = (Message) data.getSerializableExtra("last_message");
                String tag = "android:switcher:" + R.id.viewpager + ":" + 0;
                GroupsFragment groupsFragment = (GroupsFragment) getSupportFragmentManager()
                        .findFragmentByTag(tag);
                assert groupsFragment != null;
                groupsFragment.receiveMessage(message);
            }
        }

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null) {
                boolean autoJoin = data.getBooleanExtra("autoJoin", false);
                if (autoJoin) {
                    Faculty faculty = (Faculty) data.getSerializableExtra("addedFaculty");
                    String tag = "android:switcher:" + R.id.viewpager + ":" + 1;
                    FacultiesFragment facultiesFragment = (FacultiesFragment) getSupportFragmentManager()
                            .findFragmentByTag(tag);
                    assert facultiesFragment != null;
                    facultiesFragment.receiveFaculty(faculty);
                }
            }
        }

        if (requestCode == 222 && resultCode == RESULT_OK) {
            if (data != null) {
                List<Group> groups = (List<Group>) data.getSerializableExtra("new_groups");
                String tag = "android:switcher:" + R.id.viewpager + ":" + 0;
                GroupsFragment groupsFragment = (GroupsFragment) getSupportFragmentManager()
                        .findFragmentByTag(tag);
                assert groupsFragment != null;
                groupsFragment.receiveGroups(groups);
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
