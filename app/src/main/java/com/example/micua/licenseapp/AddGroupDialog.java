package com.example.micua.licenseapp;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

public class AddGroupDialog extends Dialog {

    private CardView privateGroup, publicGroup;
    private Button cancel, ok;

    private boolean privateSelected = false, publicSelected = false, goToNextActivity = false;

    public AddGroupDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_group);

        privateGroup = findViewById(R.id.cv_private_holder);
        publicGroup = findViewById(R.id.cv_public_holder);
        cancel = findViewById(R.id.btn_cancel);
        ok = findViewById(R.id.btn_ok);

        privateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                privateSelected = true;
                publicSelected = false;
                if (privateGroup.getElevation() == 0)
                    animateElevation(privateGroup, 0f, 16f);
                if (publicGroup.getElevation() != 0f)
                    animateElevation(publicGroup, 16f, 0f);
            }
        });

        publicGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                privateSelected = false;
                publicSelected = true;
                if (publicGroup.getElevation() == 0)
                    animateElevation(publicGroup, 0f, 16f);
                if (privateGroup.getElevation() != 0f)
                    animateElevation(privateGroup, 16f, 0f);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextActivity = true;
                dismiss();
            }
        });
    }

    private void animateElevation(CardView cardView, float start, float end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "cardElevation", start, end);
        animator.setDuration(200);
        animator.start();
    }

    public boolean isGoToNextActivity() {
        return goToNextActivity;
    }

    public boolean isPrivateSelected() {
        return privateSelected;
    }

    public boolean isPublicSelected() {
        return publicSelected;
    }
}
