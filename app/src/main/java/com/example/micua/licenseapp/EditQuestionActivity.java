package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditQuestionActivity extends AppCompatActivity {
    public static final String TAG = "EditQuestionActivity";

    private MaterialEditText questionText, questionExplanation;
    private RecyclerView answers;
    private ImageView done;
    private FloatingActionButton addAnswer;
    private RadioButton trueOrFalse, singleChoice, multipleChoice;

    private Intent parentIntent;
    private Question question;
    private List<Answer> answersList;
    private AnswersAdapter adapter;
    private int index;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question);

        Objects.requireNonNull(getSupportActionBar()).hide();

        parentIntent = getIntent();
        question = (Question) parentIntent.getSerializableExtra("question");
        index = parentIntent.getIntExtra("index", -1);

        bindViews();

        if (question.getQuestionType() == 0)
            trueOrFalse.setChecked(true);
        else if (question.getQuestionType() == 1)
            multipleChoice.setChecked(true);
        else
            singleChoice.setChecked(true);

        answersList = new ArrayList<>();
        initAnswers(question);

        if (question.getQuestionType() == 0)
            addAnswer.setVisibility(View.GONE);

        questionText.setText(question.getQuestionText());
        questionExplanation.setText(question.getExplanation());
        adapter = new AnswersAdapter(answersList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        answers.setLayoutManager(layoutManager);
        answers.setAdapter(adapter);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = questionText.getText().toString();
                if (text.equals(""))
                    Toast.makeText(EditQuestionActivity.this,
                            "Question field is empty", Toast.LENGTH_SHORT).show();
                else if (answersList.isEmpty())
                    Toast.makeText(EditQuestionActivity.this,
                            "No answers for this question", Toast.LENGTH_SHORT).show();
                else if (answersList.size() == computeNumCorrectAnswers())
                    Toast.makeText(EditQuestionActivity.this,
                            "No incorrect answers for this question", Toast.LENGTH_SHORT).show();
                else if (computeNumCorrectAnswers() == 0)
                    Toast.makeText(EditQuestionActivity.this,
                            "No correct answers for this question", Toast.LENGTH_SHORT).show();
                else if (computeNumCorrectAnswers() != 1 && question.getQuestionType() == 0)
                    Toast.makeText(EditQuestionActivity.this,
                            "Only one answer should be correct for this question", Toast.LENGTH_SHORT).show();
                else if (computeNumCorrectAnswers() != 1 && question.getQuestionType() == 2)
                    Toast.makeText(EditQuestionActivity.this,
                            "Only one answer should be correct for this question", Toast.LENGTH_SHORT).show();
                else if (computeNumCorrectAnswers() < 2 && question.getQuestionType() == 1)
                    Toast.makeText(EditQuestionActivity.this,
                            "More than 1 answer should be correct for this question", Toast.LENGTH_SHORT).show();
                else {
                    question.setQuestionText(text);
                    question.setExplanation(questionExplanation.getText().toString());
                    registerAnswers(answersList, question);
                    parentIntent.putExtra("index", index);
                    parentIntent.putExtra("question", question);
                    setResult(RESULT_OK, parentIntent);
                    finish();
                }
            }
        });

        addAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddAnswerDialog addAnswerDialog = new AddAnswerDialog(EditQuestionActivity.this);
                addAnswerDialog.setAdd(true);
                addAnswerDialog.create();
                addAnswerDialog.show();
                addAnswerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (addAnswerDialog.isAddOrEditPressed()) {
                            Answer answer = new Answer();
                            if (addAnswerDialog.isCorrectAnswer()) {
                                answer.setCorrectAnswer(true);
                            }
                            else {
                                answer.setCorrectAnswer(false);
                            }
                            answer.setText(addAnswerDialog.getAnswerText());
                            answersList.add(answer);
                            adapter.notifyItemInserted(answersList.size() - 1);
                        }
                    }
                });
            }
        });

        trueOrFalse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    trueOrFalse.setChecked(true);
                    singleChoice.setChecked(false);
                    multipleChoice.setChecked(false);
                    question.setQuestionType(0);
                }
            }
        });

        multipleChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    trueOrFalse.setChecked(false);
                    singleChoice.setChecked(false);
                    multipleChoice.setChecked(true);
                    addAnswer.setVisibility(View.VISIBLE);
                    question.setQuestionType(1);
                }
            }
        });

        singleChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    trueOrFalse.setChecked(false);
                    singleChoice.setChecked(true);
                    multipleChoice.setChecked(false);
                    addAnswer.setVisibility(View.VISIBLE);
                    question.setQuestionType(2);
                }
            }
        });
    }

    private int computeNumCorrectAnswers() {
        int nr = 0;
        for (int i = 0; i < answersList.size(); i++) {
            if (answersList.get(i).isCorrectAnswer())
                nr++;
        }
        return nr;
    }

    private void registerAnswers(List<Answer> answersList, Question question) {
        question.resetAnswers();
        for (int i = 0; i < answersList.size(); i++) {
            Answer answer = answersList.get(i);
            if (answer.isCorrectAnswer())
                question.addCorrectAnswer(answer.getText());
            else
                question.addIncorrectAnswer(answer.getText());
        }
    }

    private void initAnswers(Question question) {
        List<Answer> correctAnswers, incorrectAnswers;
        correctAnswers = new ArrayList<>();
        incorrectAnswers = new ArrayList<>();

        for (int i = 0; i < question.getCorrectAnswers().size(); i++) {
            String text = question.getCorrectAnswers().get(i);
            correctAnswers.add(makeAnswer(text, true));
        }

        for (int i = 0; i < question.getIncorrectAnswers().size(); i++) {
            String text = question.getIncorrectAnswers().get(i);
            incorrectAnswers.add(makeAnswer(text, false));
        }

        answersList.addAll(correctAnswers);
        answersList.addAll(incorrectAnswers);
    }

    private Answer makeAnswer(String text, boolean isCorrect) {
        Answer answer = new Answer();
        answer.setCorrectAnswer(isCorrect);
        answer.setText(text);

        return answer;
    }

    private void bindViews() {
        questionText = findViewById(R.id.met_question_text);
        questionExplanation = findViewById(R.id.met_question_explanation);
        answers = findViewById(R.id.rv_answers);
        done = findViewById(R.id.iv_done);
        addAnswer = findViewById(R.id.fab_add_answer);
        trueOrFalse = findViewById(R.id.rb_true_or_false);
        singleChoice = findViewById(R.id.rb_single_choice);
        multipleChoice = findViewById(R.id.rb_multiple_choice);
    }
}
