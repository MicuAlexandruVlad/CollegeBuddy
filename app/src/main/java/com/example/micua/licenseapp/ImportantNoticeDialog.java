package com.example.micua.licenseapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class ImportantNoticeDialog extends Dialog {
    private String title;
    private String message;

    private LottieAnimationView warningAnim;
    private TextView dialogTitle, dialogMessage;
    private Button ok;

    public ImportantNoticeDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_important_notice);

        warningAnim = findViewById(R.id.lav_warning);
        dialogTitle = findViewById(R.id.tv_dialog_title);
        dialogMessage = findViewById(R.id.tv_dialog_message);
        ok = findViewById(R.id.btn_ok);

        dialogTitle.setText(title);
        dialogMessage.setText(message);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
