package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddQuizCategoryDialog extends Dialog {
    public static final String TAG = "AddQuizCategoryDialog";

    private Button addOrEdit, delete;
    private EditText categoryNameET;

    private boolean isEdit, isAdd;
    private boolean isDeletePressed = false, isAddOrEditPressed = false;
    private String categoryName;

    public AddQuizCategoryDialog(@NonNull Context context) {
        super(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_quiz_category);

        addOrEdit = findViewById(R.id.btn_add_or_edit_category);
        delete = findViewById(R.id.btn_delete_category);
        categoryNameET = findViewById(R.id.et_category_name);

        addOrEdit.setText("ADD");
        if (isEdit) {
            categoryNameET.setText(categoryName);
            delete.setVisibility(View.VISIBLE);
            addOrEdit.setText("EDIT");
        }
        else {
            delete.setVisibility(View.GONE);
        }

        addOrEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryNameET.getText().toString().equals(""))
                    Toast.makeText(getContext(), "Category name is empty", Toast.LENGTH_SHORT).show();
                else {
                    isAddOrEditPressed = true;
                    categoryName = categoryNameET.getText().toString();
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isDeletePressed() {
        return isDeletePressed;
    }

    public boolean isAddOrEditPressed() {
        return isAddOrEditPressed;
    }
}
