package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

public class RunQuizAnswerAdapter extends RecyclerView.Adapter<RunQuizAnswerAdapter.ViewHolder> {
    public static final String TAG = "RunQuizAnswerAdapter";

    private List<Answer> answers;
    private Context context;
    private User currentUser;
    private boolean isMultipleChoice = false;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView answerText;
        private RadioButton radioButton;
        private RelativeLayout answerBody;

        public ViewHolder(View view) {
            super(view);
            answerBody = view.findViewById(R.id.rl_answer_body);
            radioButton = view.findViewById(R.id.rb_answer_option);
            answerText = view.findViewById(R.id.tv_answer_text);
        }
    }

    public RunQuizAnswerAdapter(List<Answer> answers, Context context, User currentUser) {
        this.answers = answers;
        this.context = context;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public RunQuizAnswerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.run_quiz_answer_list_item, viewGroup, false);

        return new RunQuizAnswerAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RunQuizAnswerAdapter.ViewHolder viewHolder, int i) {
        final Answer answer = answers.get(i);

        final int pos = i;

        viewHolder.radioButton.setChecked(answer.isChecked());
        viewHolder.answerText.setText(answer.getText());

        if (!isMultipleChoice) {
            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.radioButton.setChecked(true);
                    answer.setChecked(true);
                    setRemainingAnswers(false, pos);
                }
            });
            viewHolder.answerBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.radioButton.setChecked(true);
                    answer.setChecked(true);
                    setRemainingAnswers(false, pos);
                }
            });
        }
        else {
            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.radioButton.setChecked(!viewHolder.radioButton.isChecked());
                    answer.setChecked(!answer.isChecked());
                }
            });
            viewHolder.answerBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.radioButton.setChecked(!viewHolder.radioButton.isChecked());
                    answer.setChecked(!answer.isChecked());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    private void setRemainingAnswers(boolean bool, int indexToExclude) {
        for (int i = 0; i < answers.size(); i++) {
            if (i != indexToExclude)
                answers.get(i).setChecked(bool);
        }
        notifyDataSetChanged();
    }

    public void setMultipleChoice(boolean multipleChoice) {
        isMultipleChoice = multipleChoice;
    }

    public double getScoreForAnswer() {
        double numCorrectAnswers = 0.0, numCheckedCorrectAnswers = 0.0;
        double numCheckedIncorrectAnswers = 0.0;
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).isCorrectAnswer()) {
                Log.d(RunQuizActivity.TAG, "getScoreForAnswer: text -> " + answers.get(i).getText());
                numCorrectAnswers++;
                if (answers.get(i).isChecked())
                    numCheckedCorrectAnswers++;
            }
            if (!answers.get(i).isCorrectAnswer()) {
                if (answers.get(i).isChecked())
                    numCheckedIncorrectAnswers++;
            }
        }
        Log.d(RunQuizActivity.TAG, "getScoreForAnswer: numCorrectAnswers -> " + numCorrectAnswers);
        Log.d(RunQuizActivity.TAG, "getScoreForAnswer: numCheckedCorrectAnswers -> " + numCheckedCorrectAnswers);
        Log.d(RunQuizActivity.TAG, "getScoreForAnswer: numCheckedIncorrectAnswers -> " + numCheckedIncorrectAnswers);

        if (numCheckedIncorrectAnswers != 0.0) {
            return 0.0;
        }
        return numCheckedCorrectAnswers / numCorrectAnswers;
    }
}
