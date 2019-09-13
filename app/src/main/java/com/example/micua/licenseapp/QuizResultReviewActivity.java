package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QuizResultReviewActivity extends AppCompatActivity {
    public static final String TAG = "QuizResultReview";

    private TextView questionText, explanation, questionNumber;
    private RecyclerView answersRV;
    private Button nextQuestion;

    private Intent parentIntent;
    private List<Answer> answerList;
    private Map<Integer, ArrayList<Answer>> pickedAnswers;
    private List<Question> questions;
    private ReviewAnswerAdapter answerAdapter;
    private List<Double> scoreArray;

    private int questionIndex = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result_review);

        Objects.requireNonNull(getSupportActionBar()).hide();

        bindViews();

        questions = new ArrayList<>();
        answerList = new ArrayList<>();
        scoreArray = new ArrayList<>();

        parentIntent = getIntent();
        questions = (List<Question>) parentIntent.getSerializableExtra("question_list");
        pickedAnswers = (Map<Integer, ArrayList<Answer>>) parentIntent.getSerializableExtra("picked_answers");
        scoreArray = (List<Double>) parentIntent.getSerializableExtra("score_array");

        for (int i = 0; i < scoreArray.size(); i++) {
            Log.d(TAG, "onCreate: score for answer " + (i + 1) + " is -> " + scoreArray.get(i));
        }

        questionText.setText(questions.get(questionIndex).getQuestionText());
        if (!questions.get(questionIndex).getExplanation().equals(""))
            explanation.setText(questions.get(questionIndex).getExplanation());
        else
            explanation.setText("No explanation available");
        questionNumber.setText((questionIndex + 1) + " of " + (questions.size()));

        answerAdapter = new ReviewAnswerAdapter(answerList, this, scoreArray);
        answerAdapter.setQuestionIndex(questionIndex);
        answersRV.setAdapter(answerAdapter);
        answersRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initAnswerList(questionIndex);


        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionIndex == questions.size() - 2)
                    nextQuestion.setText("Finish");
                questionIndex++;
                answerAdapter.setQuestionIndex(questionIndex);
                if (questionIndex < questions.size())
                    goToNextQuestion();
                else {
                    finish();
                }
            }
        });

    }

    private void bindViews() {
        questionNumber = findViewById(R.id.tv_question_number);
        questionText = findViewById(R.id.tv_question_text);
        explanation = findViewById(R.id.tv_question_explanation);
        answersRV = findViewById(R.id.rv_review_answers);
        nextQuestion = findViewById(R.id.btn_next_question);
    }

    @SuppressLint("SetTextI18n")
    private void goToNextQuestion() {
        questionText.setText(questions.get(questionIndex).getQuestionText());
        if (!questions.get(questionIndex).getExplanation().equals(""))
            explanation.setText(questions.get(questionIndex).getExplanation());
        else
            explanation.setText("No explanation available");
        questionNumber.setText((questionIndex + 1) + " of " + (questions.size()));
        initAnswerList(questionIndex);
    }

    private void initAnswerList(int questionIndex) {
        answerList.clear();
        answerAdapter.notifyDataSetChanged();
        Question question = questions.get(questionIndex);
        for (int i = 0; i < question.getCorrectAnswers().size(); i++) {
            Answer answer = new Answer();
            answer.setCorrectAnswer(true);
            answer.setText(question.getCorrectAnswers().get(i));
            answerList.add(answer);
            answerAdapter.notifyItemInserted(answerList.size() - 1);
        }

        for (int i = 0; i < question.getIncorrectAnswers().size(); i++) {
            Answer answer = new Answer();
            answer.setCorrectAnswer(false);
            answer.setText(question.getIncorrectAnswers().get(i));
            answerList.add(answer);
            answerAdapter.notifyItemInserted(answerList.size() - 1);
        }

        setPicks(questionIndex);
    }

    private void setPicks(int questionIndex) {
        ArrayList<Answer> answersForCurrentQuestion = pickedAnswers.get(questionIndex);
        for (int i = 0; i < answersForCurrentQuestion.size(); i++) {
            if (answersForCurrentQuestion.get(i).isChecked()) {
                for (int j = 0; j < answerList.size(); j++) {
                    if (answerList.get(j).getText().equals(answersForCurrentQuestion.get(i).getText()) &&
                                !answerList.get(j).isChecked()) {
                        answerList.get(j).setChecked(true);
                        answerAdapter.notifyDataSetChanged();
                        Log.d(TAG, "setPicks: user picked -> " + answersForCurrentQuestion.get(i).getText());
                    }
                    else {
                        answerList.get(j).setChecked(false);
                        answerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
