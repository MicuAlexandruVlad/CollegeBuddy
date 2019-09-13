package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

public class AddAnswerDialog extends Dialog {
    public static final String TAG = "AddQuizCategoryDialog";

    private Button addOrEdit, delete;
    private MaterialEditText answerTextET;
    private CheckBox correctAnswer;

    private boolean isEdit, isAdd, isCorrectAnswer;
    private boolean isDeletePressed = false, isAddOrEditPressed = false;
    private String answerText;

    public AddAnswerDialog(@NonNull Context context) {
        super(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_answer);

        addOrEdit = findViewById(R.id.btn_add_or_edit_answer);
        delete = findViewById(R.id.btn_delete_answer);
        answerTextET = findViewById(R.id.met_answer_text);
        correctAnswer = findViewById(R.id.cb_correct_answer);

        addOrEdit.setText("ADD");
        if (isEdit) {
            answerTextET.setText(answerText);
            delete.setVisibility(View.VISIBLE);
            addOrEdit.setText("EDIT");
            correctAnswer.setChecked(isCorrectAnswer);
        }
        else {
            delete.setVisibility(View.GONE);
        }

        addOrEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (answerTextET.getText().toString().equals(""))
                    Toast.makeText(getContext(), "Answer field is empty", Toast.LENGTH_SHORT).show();
                else {
                    isAddOrEditPressed = true;
                    answerText = answerTextET.getText().toString();
                    dismiss();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDeletePressed = true;
                dismiss();
                Log.d(TAG, "onClick: delete -> true");
            }
        });
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        isCorrectAnswer = correctAnswer;
    }

    public boolean isDeletePressed() {
        return isDeletePressed;
    }

    public boolean isAddOrEditPressed() {
        return isAddOrEditPressed;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer.isChecked();
    }
}
