package com.example.micua.licenseapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FacultiesFragment extends Fragment {
    public static final String TAG = "FacultiesFragment";

    private RecyclerView recyclerView;
    private Button joinFaculty;

    private String title;
    private int page;
    private List<Faculty> faculties;
    private RemasteredFacultyAdapter adapter;
    private User currentUser;

    public static FacultiesFragment newInstance(int page, String title, Bundle data) {
        FacultiesFragment facultiesFragment = new FacultiesFragment();
        data.putInt("page_count", page);
        data.putString("page_title", title);
        facultiesFragment.setArguments(data);
        return facultiesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        page = getArguments().getInt("page_count", 0);
        title = getArguments().getString("page_title");
        faculties = (List<Faculty>) getArguments().getSerializable("faculties");
        currentUser = (User) getArguments().getSerializable("currentUser");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faculties, container, false);

        recyclerView = view.findViewById(R.id.rv_joined_faculties);
        joinFaculty = view.findViewById(R.id.btn_join_faculty);

        adapter = new RemasteredFacultyAdapter(faculties, currentUser, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        joinFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), JoinFacultyActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, 3123);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3123 && resultCode == RESULT_OK) {
            if (data != null) {
                List<Faculty> newFaculties;
                newFaculties = (List<Faculty>) data.getSerializableExtra("joined_faculties");
                faculties.addAll(newFaculties);
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected void receiveFaculty(Faculty faculty) {
        Log.d(TAG, "receiveFaculty: added faculty -> " + faculty.getFacultyName());
        faculties.add(faculty);
        adapter.notifyItemInserted(faculties.size() - 1);
    }
}
