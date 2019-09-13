package com.example.micua.licenseapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";

    private Switch dynamicTheme;
    private Button apply;

    private Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Objects.requireNonNull(getSupportActionBar()).hide();

        parentIntent = getIntent();
        boolean isDynamic = parentIntent.getBooleanExtra("dynamic_theme", false);


        dynamicTheme = findViewById(R.id.sw_dynamic_theme);
        apply = findViewById(R.id.btn_apply_settings);

        dynamicTheme.setChecked(isDynamic);

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentIntent.putExtra("dynamic_theme", dynamicTheme.isChecked());
                setResult(RESULT_OK, parentIntent);
                finish();
            }
        });

    }
}
