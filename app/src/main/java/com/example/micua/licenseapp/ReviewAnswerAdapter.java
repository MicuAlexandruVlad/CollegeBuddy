package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class ReviewAnswerAdapter extends RecyclerView.Adapter<ReviewAnswerAdapter.ViewHolder> {
    public static final String TAG = "ReviewAnswerAdapter";

    private List<Answer> answers;
    private List<Double> scoreArray;
    private Context context;
    private int questionIndex;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView answerText;
        private LottieAnimationView animationView;
        private RelativeLayout answerBody;

        public ViewHolder(View view) {
            super(view);
            answerBody = view.findViewById(R.id.rl_answer_body);
            animationView = view.findViewById(R.id.lv_ans);
            answerText = view.findViewById(R.id.tv_answer_text);
        }
    }

    public ReviewAnswerAdapter(List<Answer> answers, Context context, List<Double> scoreArray) {
        this.answers = answers;
        this.context = context;
        this.scoreArray = scoreArray;
    }

    @NonNull
    @Override
    public ReviewAnswerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_answer_list_item, viewGroup, false);

        return new ReviewAnswerAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ReviewAnswerAdapter.ViewHolder viewHolder, int i) {
        final Answer answer = answers.get(i);

        viewHolder.answerText.setText(answer.getText());

        Log.d(QuizResultReviewActivity.TAG, "onBindViewHolder: score in adapter -> " + scoreArray.get(questionIndex));
        Log.d(QuizResultReviewActivity.TAG, "onBindViewHolder: questionIndex in adapter -> " + questionIndex);

        if (scoreArray.get(questionIndex) == 1.0) {
            // all the correct answers are picked
            if (answer.isChecked() && answer.isCorrectAnswer()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.correct_answer_border_round));
                viewHolder.animationView.setVisibility(View.VISIBLE);
                viewHolder.animationView.setAnimation("correct01.json");
            }

            if (!answer.isChecked()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.unpicked_answer_border_round));
                viewHolder.animationView.setVisibility(View.INVISIBLE);
            }
        }

        if (scoreArray.get(questionIndex) < 1.0){
            if (answer.isChecked() && !answer.isCorrectAnswer()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.wrong_answer_border_round));
                viewHolder.animationView.setVisibility(View.VISIBLE);
                viewHolder.animationView.setAnimation("wrong.json");
            }

            if (answer.isChecked() && answer.isCorrectAnswer()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.correct_answer_border_round));
                viewHolder.animationView.setVisibility(View.VISIBLE);
                viewHolder.animationView.setAnimation("correct01.json");
            }

            if (answer.isCorrectAnswer() && !answer.isChecked()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.correct_answer_border_round));
                viewHolder.animationView.setVisibility(View.VISIBLE);
                viewHolder.animationView.setAnimation("correct01.json");
            }

            if (!answer.isChecked() && !answer.isCorrectAnswer()) {
                viewHolder.answerBody.setBackground(ContextCompat.getDrawable(context, R.drawable.unpicked_answer_border_round));
                viewHolder.animationView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }
}
