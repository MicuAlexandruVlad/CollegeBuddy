package com.example.micua.licenseapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;

public class DialogVerified extends Dialog {

    private Button ok;
    private LottieAnimationView lottieAnimationView;

    public DialogVerified(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verified_dialog);

        ok = findViewById(R.id.btn_ok);
        lottieAnimationView = findViewById(R.id.l_success);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
