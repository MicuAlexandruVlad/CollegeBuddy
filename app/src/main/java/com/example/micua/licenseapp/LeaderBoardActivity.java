package com.example.micua.licenseapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeaderBoardActivity extends AppCompatActivity {
    public static final String TAG = "LeaderBoardActivity";

    private RecyclerView leaderBoardRV;

    private Intent parentIntent;
    private List<LeaderBoardEntry> leaderBoardEntries;
    private LeaderBoardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        Objects.requireNonNull(getSupportActionBar()).hide();

        leaderBoardRV = findViewById(R.id.rv_leader_board);

        leaderBoardEntries = new ArrayList<>();
        parentIntent = getIntent();
        leaderBoardEntries = (List<LeaderBoardEntry>) parentIntent.getSerializableExtra("entries");

        adapter = new LeaderBoardAdapter(leaderBoardEntries);
        leaderBoardRV.setAdapter(adapter);
        leaderBoardRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
}
