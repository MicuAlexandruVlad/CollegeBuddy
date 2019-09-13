package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {
    private List<Question> questions;
    private Context context;

    // 0 - true or false
    // 1 - multiple choice
    // 2 - single choice
    private int questionType;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView questionText, numAnswers, type, numCorrectAnswers, numIncorrectAnswers;
        private TextView questionExplanation;
        private Button edit;

        public ViewHolder(View view) {
            super(view);
            questionText = view.findViewById(R.id.tv_question);
            numAnswers = view.findViewById(R.id.tv_num_possible_answers);
            numCorrectAnswers = view.findViewById(R.id.tv_num_correct_answers);
            numIncorrectAnswers = view.findViewById(R.id.tv_num_incorrect_answers);
            type = view.findViewById(R.id.tv_question_type);
            questionExplanation = view.findViewById(R.id.tv_question_explanation);
            edit = view.findViewById(R.id.btn_edit_question);
        }
    }

    public QuestionsAdapter(List<Question> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuestionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_question_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new QuestionsAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Question question = questions.get(i);

        viewHolder.questionText.setText("Q: " + question.getQuestionText());
        viewHolder.questionExplanation.setText("Explanation: " + question.getExplanation());
        int totalAnswers = question.getCorrectAnswers().size() +
                question.getIncorrectAnswers().size();
        viewHolder.numAnswers.setText("Possible answers: " + totalAnswers);
        viewHolder.numCorrectAnswers.setText("Correct answers: " + question.getNumCorrectAnswers());
        viewHolder.numIncorrectAnswers.setText("Incorrect answers: "
                + question.getIncorrectAnswers().size());
        if (question.getQuestionType() == 0)
            viewHolder.type.setText("Question type: True or false");
        else if (question.getQuestionType() == 1)
            viewHolder.type.setText("Question type: Multiple choice");
        else
            viewHolder.type.setText("Question type: Single choice");

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditQuestionActivity.class);
                intent.putExtra("index", i);
                intent.putExtra("question", question);
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });


    }

    @Override
    public int getItemCount() {
        return questions.size();
    }
}
