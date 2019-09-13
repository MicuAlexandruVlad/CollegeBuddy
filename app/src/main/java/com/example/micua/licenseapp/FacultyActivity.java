package com.example.micua.licenseapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class FacultyActivity extends AppCompatActivity {

    private static final int ADD_FACULTY_REQ_CODE = 31;
    public static final int EDIT_COLLEGE_REQ_CODE = 22;
    public static final String TAG = "Faculty";
    private College currentCollege;
    private FacultyAdapter adapter;
    private User currentUser;
    private DBLinks dbLinks;
    private List<Faculty> faculties;
    private String[] colleges;

    private FloatingActionButton addFaculty;
    private LinearLayout noFaculties;
    private RecyclerView facultiesList;
    private ScrollView recyclerHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Faculties");

        bindViews();

        faculties = new ArrayList<>();
        colleges = new String[11089];

        currentCollege = (College) getIntent().getSerializableExtra("currentCollege");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        colleges = getIntent().getStringArrayExtra("collegesAll");

        adapter = new FacultyAdapter(faculties, currentUser, currentCollege);
        facultiesList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        facultiesList.setAdapter(adapter);

        if (currentCollege.getNumFaculties() == 0) {
            recyclerHolder.setVisibility(View.GONE);
            noFaculties.setVisibility(View.VISIBLE);
        } else {
            recyclerHolder.setVisibility(View.VISIBLE);
            noFaculties.setVisibility(View.GONE);
        }

        dbLinks = new DBLinks();
        reqFacultiesForCollege(dbLinks.getBaseLink() + "faculties-for-college", currentCollege);

        addFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FacultyActivity.this, AddFacultyActivity.class);
                intent.putExtra("currentCollege", currentCollege);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, ADD_FACULTY_REQ_CODE);
            }
        });
    }

    private void bindViews() {
        noFaculties = findViewById(R.id.ll_no_faculties_found);
        addFaculty = findViewById(R.id.fab_add_faculty);
        facultiesList = findViewById(R.id.rv_faculty_list);
        recyclerHolder = findViewById(R.id.sv_faculties_holder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_faculty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_college_details:
                Intent intent = new Intent(FacultyActivity.this, CollegeDetailsActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentCollege", currentCollege);
                startActivity(intent);
                return true;
            case R.id.menu_edit_college:
                Intent intent1 = new Intent(FacultyActivity.this, EditCollegeActivity.class);
                intent1.putExtra("currentCollege", currentCollege);
                intent1.putExtra("currentUser", currentUser);
                intent1.putExtra("collegesAll", colleges);
                startActivityForResult(intent1, EDIT_COLLEGE_REQ_CODE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_FACULTY_REQ_CODE) {
                if (data != null) {
                    Faculty faculty = (Faculty) data.getSerializableExtra("addedFaculty");
                    faculties.add(faculty);
                    adapter.notifyDataSetChanged();
                }
            }
            if (requestCode == EDIT_COLLEGE_REQ_CODE) {
                if (data != null) {
                    College college = (College) data.getSerializableExtra("editedCollege");
                }
            }
        }
    }

    private void reqFacultiesForCollege(String url, College college) {
        faculties.clear();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("facultyReferencedCollegeName", college.getName());
        client.get(url + "?facultyReferencedCollegeName=" + college.getName(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    String json = new String(responseBody);
                    try {
                        JSONArray array = new JSONArray(json);
                        for (int i = 0; i < array.length(); i++) {
                           Faculty faculty = new Faculty();
                           JSONObject object = array.getJSONObject(i);
                           faculty.setReferencedCollegeName(object.getString("facultyReferencedCollegeName"));
                           faculty.setFacultyDescription(object.getString("facultyDescription"));
                           faculty.setNumMembers(Integer.parseInt(object.getString("facultyNumMembers")));
                           faculty.setNumGroups(Integer.parseInt(object.getString("facultyNumGroups")));
                           faculty.setFacultyName(object.getString("facultyName"));
                           faculties.add(faculty);
                           adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}
