package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xw.repo.BubbleSeekBar;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class QuizWizardActivity extends AppCompatActivity {
    public static final String TAG = "QuizWizard";

    private RelativeLayout step1Holder, step2Holder, addCategory, step3Holder;
    private Button toStep2, toStep3, addQuestion, saveQuiz, postQuiz;
    private FloatingActionButton addAnswer;
    private ImageView goBack;
    private MaterialEditText quizName, quizDescription, questionText, questionExplanation;
    private TextView firstStepQuestion2, answerTip, answersTV, reviewQuizName, reviewQuizDescription;
    private TextView reviewQuizTimed, reviewQuizTimerLimit, questionExplanationLabel;
    private CheckBox timedQuiz;
    private LinearLayout limitedTimerOptions;
    private RadioButton limitedTimer, unlimitedTimer, trueOrFalse, singleChoice, multipleChoice;
    private EditText timerLimit;
    private RecyclerView categoriesRV, answersRV, reviewQuestionsRV, reviewCategoriesRV;
    private BubbleSeekBar difficulty;

    private int transitionDuration;
    private int currentStep;
    private List<String> categoriesList;
    private List<Answer> answersList;
    private CategoryAdapter categoryAdapter;
    private AnswersAdapter answersAdapter;
    private QuestionsAdapter questionsAdapter;
    private List<Question> questions;
    private int correctAnswers, quizIndex;
    private Quiz quiz;
    private User currentUser;
    private Intent parentIntent;
    private Repository repository;
    private boolean isEdit;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_wizard);

        step1Holder = findViewById(R.id.rl_step_1);
        step2Holder = findViewById(R.id.rl_step_2);
        step3Holder = findViewById(R.id.rl_step_3);
        addCategory = findViewById(R.id.rl_add_category);
        toStep2 = findViewById(R.id.btn_to_step_2);
        toStep3 = findViewById(R.id.btn_to_step_3);
        addQuestion = findViewById(R.id.btn_add_question);
        saveQuiz = findViewById(R.id.btn_save_quiz);
        postQuiz = findViewById(R.id.btn_post_quiz);
        goBack = findViewById(R.id.iv_go_back);
        quizDescription = findViewById(R.id.met_quiz_description);
        quizName = findViewById(R.id.met_quiz_name);
        questionText = findViewById(R.id.met_question_text);
        questionExplanation = findViewById(R.id.met_question_explanation);
        firstStepQuestion2 = findViewById(R.id.tv_4);
        answerTip = findViewById(R.id.tv_answer_tip);
        answersTV = findViewById(R.id.tv_6);
        reviewQuizName = findViewById(R.id.tv_review_quiz_name);
        reviewQuizDescription = findViewById(R.id.tv_review_quiz_description);
        reviewQuizTimed = findViewById(R.id.tv_review_quiz_is_timed);
        reviewQuizTimerLimit = findViewById(R.id.tv_review_quiz_timer_limit);
        questionExplanationLabel = findViewById(R.id.tv_explanation_label);
        timedQuiz = findViewById(R.id.cb_timed_quiz);
        limitedTimerOptions = findViewById(R.id.ll_limited_timer_options);
        limitedTimer = findViewById(R.id.rb_limited_timer_yes);
        unlimitedTimer = findViewById(R.id.rb_limited_timer_no);
        trueOrFalse = findViewById(R.id.rb_true_or_false);
        singleChoice = findViewById(R.id.rb_single_choice);
        multipleChoice = findViewById(R.id.rb_multiple_choice);
        timerLimit = findViewById(R.id.et_timer_limit);
        categoriesRV = findViewById(R.id.rv_categories);
        answersRV = findViewById(R.id.rv_answers);
        reviewQuestionsRV = findViewById(R.id.rv_review_questions);
        reviewCategoriesRV = findViewById(R.id.rv_review_quiz_categories_review);
        addAnswer = findViewById(R.id.fab_add_answer);
        difficulty = findViewById(R.id.bsb_difficulty);

        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        isEdit = parentIntent.getBooleanExtra("isEdit", false);

        repository = new Repository(this);
        quiz = new Quiz();
        categoriesList = new ArrayList<>();
        answersList = new ArrayList<>();
        questions = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoriesList);
        answersAdapter = new AnswersAdapter(answersList);
        questionsAdapter = new QuestionsAdapter(questions);

        Objects.requireNonNull(getSupportActionBar()).hide();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        categoriesRV.setLayoutManager(layoutManager);
        categoriesRV.setAdapter(categoryAdapter);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        answersRV.setLayoutManager(layoutManager1);
        answersRV.setAdapter(answersAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        reviewQuestionsRV.setLayoutManager(layoutManager2);
        reviewQuestionsRV.setAdapter(questionsAdapter);

        transitionDuration = 300;
        currentStep = 1;

        goBack.setVisibility(View.GONE);
        step2Holder.setVisibility(View.GONE);
        step3Holder.setVisibility(View.GONE);
        step1Holder.setVisibility(View.VISIBLE);
        addAnswer.setVisibility(View.GONE);
        answersTV.setVisibility(View.GONE);

        difficulty.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
            @NonNull
            @Override
            public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                array.clear();
                array.put(0, "Low");
                array.put(1, "Medium");
                array.put(2, "High");

                return array;
            }
        });

        difficulty.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                int color;

                quiz.setDifficulty(progress);

                if (progress == 0) {
                    color = ContextCompat.getColor(QuizWizardActivity.this, R.color.difficultyLow);
                } else if (progress == 1) {
                    color = ContextCompat.getColor(QuizWizardActivity.this, R.color.difficultyMedium);
                } else
                    color = ContextCompat.getColor(QuizWizardActivity.this, R.color.difficultyHigh);

                bubbleSeekBar.setSecondTrackColor(color);
                bubbleSeekBar.setThumbColor(color);
                bubbleSeekBar.setBubbleColor(color);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        if (!isEdit) {
            quiz.setDifficulty(0);
            difficulty.setProgress(0);
        }

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddQuizCategoryDialog addQuizCategoryDialog =
                        new AddQuizCategoryDialog(QuizWizardActivity.this);
                addQuizCategoryDialog.setAdd(true);
                addQuizCategoryDialog.create();
                addQuizCategoryDialog.show();

                addQuizCategoryDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (addQuizCategoryDialog.isAddOrEditPressed()) {
                            categoriesList.add(addQuizCategoryDialog.getCategoryName());
                            categoryAdapter.notifyItemInserted(categoriesList.size() - 1);
                        }
                    }
                });
            }
        });

        timedQuiz.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                timerLimit.setVisibility(View.GONE);
                if (b) {
                    firstStepQuestion2.setVisibility(View.VISIBLE);
                    limitedTimerOptions.setVisibility(View.VISIBLE);
                    if (limitedTimer.isChecked())
                        timerLimit.setVisibility(View.VISIBLE);
                }
                else {
                    firstStepQuestion2.setVisibility(View.GONE);
                    limitedTimerOptions.setVisibility(View.GONE);
                }
            }
        });

        limitedTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    unlimitedTimer.setChecked(false);
                    timerLimit.setVisibility(View.VISIBLE);
                }
            }
        });

        unlimitedTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    limitedTimer.setChecked(false);
                    timerLimit.setVisibility(View.GONE);
                }
            }
        });

        toStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quizDescription.getText().toString().equals("")
                        || quizName.getText().toString().equals(""))
                    Toast.makeText(QuizWizardActivity.this,
                            "One or more fields are empty", Toast.LENGTH_SHORT).show();
                else {
                    quiz.setName(quizName.getText().toString());
                    quiz.setDescription(quizDescription.getText().toString());
                    if (!timedQuiz.isChecked()) {
                        goForward(step1Holder, step2Holder, 2);
                        currentStep = 2;
                        quiz.setTimed(false);
                    } else {
                        quiz.setTimed(true);
                        if (!limitedTimer.isChecked() && !unlimitedTimer.isChecked())
                            Toast.makeText(QuizWizardActivity.this,
                                    "You need to select a timer option", Toast.LENGTH_SHORT).show();
                        if (unlimitedTimer.isChecked()) {
                            goForward(step1Holder, step2Holder, 2);
                            currentStep = 2;
                            quiz.setLimitedTimer(false);
                        }
                        if (limitedTimer.isChecked()) {
                            quiz.setLimitedTimer(true);
                            if (timerLimit.getText().toString().equals(""))
                                Toast.makeText(QuizWizardActivity.this,
                                        "No timer limit provided", Toast.LENGTH_SHORT).show();
                            else {
                                quiz.setTimerValue(timerLimit.getText().toString());
                                goForward(step1Holder, step2Holder, 2);
                                currentStep = 2;
                            }
                        }
                    }
                }
            }
        });

        questionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    answerTip.setVisibility(View.VISIBLE);
                    questionExplanationLabel.setVisibility(View.GONE);
                    questionExplanation.setVisibility(View.GONE);
                }
                else {
                    answerTip.setVisibility(View.GONE);
                    questionExplanationLabel.setVisibility(View.VISIBLE);
                    questionExplanation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        trueOrFalse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    answersTV.setVisibility(View.VISIBLE);
                    addAnswer.setVisibility(View.GONE);
                    singleChoice.setChecked(false);
                    multipleChoice.setChecked(false);

                    Answer trueAnswer = new Answer();
                    trueAnswer.setText("True");
                    trueAnswer.setCorrectAnswer(false);

                    Answer falseAnswer = new Answer();
                    falseAnswer.setText("False");
                    falseAnswer.setCorrectAnswer(false);

                    answersAdapter.setQuestionType(0);
                    answersList.clear();
                    answersList.add(trueAnswer);
                    answersList.add(falseAnswer);
                    answersAdapter.notifyDataSetChanged();
                }
                else {
                    answersList.clear();
                    answersAdapter.notifyDataSetChanged();
                }
            }
        });

        singleChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    addAnswer.setVisibility(View.VISIBLE);
                    trueOrFalse.setChecked(false);
                    multipleChoice.setChecked(false);
                    answersAdapter.setQuestionType(2);
                }
            }
        });

        multipleChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    addAnswer.setVisibility(View.VISIBLE);
                    singleChoice.setChecked(false);
                    trueOrFalse.setChecked(false);
                    answersAdapter.setQuestionType(1);
                }
            }
        });

        addAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddAnswerDialog addAnswerDialog = new AddAnswerDialog(QuizWizardActivity.this);
                addAnswerDialog.setAdd(true);
                addAnswerDialog.create();
                addAnswerDialog.show();

                addAnswerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        String text = addAnswerDialog.getAnswerText();
                        boolean isCorrectAnswer = addAnswerDialog.isCorrectAnswer();

                        Answer answer = new Answer();
                        answer.setText(text);
                        answer.setCorrectAnswer(isCorrectAnswer);

                        answersList.add(answer);
                        if (answersList.size() != 0)
                            answersTV.setVisibility(View.VISIBLE);
                        else
                            answersTV.setVisibility(View.GONE);
                        answersAdapter.notifyItemInserted(answersList.size() - 1);
                    }
                });
            }
        });

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logAnswers();
                boolean b = computeQuestionCompletionAndRegisterQuestion();
            }
        });



        toStep3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: questions -> " + questions.size());
                if (!questionText.getText().toString().equals("") || trueOrFalse.isChecked()
                        || singleChoice.isChecked() || multipleChoice.isChecked() || !answersList.isEmpty()) {
                    if (computeQuestionCompletionAndRegisterQuestion()) {
                        startTransitionOut(step2Holder, transitionDuration);
                        startTransitionIn(step3Holder, transitionDuration);
                        currentStep = 3;
                        initStep3Views();
                    }
                }
                else if (questions.size() > 0) {
                    if (answersList.size() == 0)
                        if (!trueOrFalse.isChecked() && !singleChoice.isChecked() && !multipleChoice.isChecked())
                            if (questionText.getText().toString().equals("")) {
                                startTransitionOut(step2Holder, transitionDuration);
                                startTransitionIn(step3Holder, transitionDuration);
                                currentStep = 3;
                                initStep3Views();
                            }
                }
                else {
                    Toast.makeText(QuizWizardActivity.this,
                            "You have no questions for this quiz", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStep == 2) {
                    startTransitionIn(step1Holder, transitionDuration);
                    startTransitionOut(step2Holder, transitionDuration);
                    currentStep = 1;
                    goBack.setVisibility(View.GONE);
                }
                if (currentStep == 3) {
                    startTransitionIn(step2Holder, transitionDuration);
                    startTransitionOut(step3Holder, transitionDuration);
                    currentStep = 2;
                }

            }
        });

        saveQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quiz.setQuestionsList(questions);
                quiz.setNumQuestions(questions.size());
                quiz.setCreatedOn(Calendar.getInstance().getTime().toString());
                quiz.setCreatedBy(currentUser.getEmail());
                quiz.setCategories(categoriesList);
                quiz.setCreatedByName(currentUser.getFirstName() + " " + currentUser.getLastName());

                if (!isEdit) {
                    quiz.setIdToReference(getAlphaNumeric(16) + "_" + currentUser.getId());
                    Gson gson = new Gson();
                    String json = gson.toJson(quiz);
                    Log.d(TAG, "onClick: quiz -> " + json);
                    QuizAsJSON quizAsJSON = new QuizAsJSON();
                    quizAsJSON.setPublished(0);
                    quizAsJSON.setQuizJson(json);
                    quizAsJSON.setReferencedGroupTokens("");
                    quizAsJSON.setReferencedUserEmail(currentUser.getEmail());
                    repository.insertQuiz(quizAsJSON);
                    Log.d(TAG, "onClick: quiz inserted");
                    finish();
                }
                else {
                    parentIntent.putExtra("edited_quiz", quiz);
                    Gson gson = new Gson();
                    String json = gson.toJson(quiz);
                    Log.d(TAG, "onClick: quiz -> " + json);
                    QuizAsJSON quizAsJSON = new QuizAsJSON();
                    quizAsJSON.setPublished(0);
                    quizAsJSON.setQuizJson(json);
                    quizAsJSON.setReferencedGroupTokens("");
                    quizAsJSON.setReferencedUserEmail(currentUser.getEmail());
                    parentIntent.putExtra("edited_quiz_as_json", quizAsJSON);
                    parentIntent.putExtra("quiz_index", quizIndex);
                    setResult(RESULT_OK, parentIntent);
                    finish();
                }

            }
        });

        postQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: do not forget about me pls :)
            }
        });

        if (isEdit) {
            Log.d(TAG, "onCreate: difficulty -> " + quiz.getDifficulty());
            quiz = (Quiz) parentIntent.getSerializableExtra("quiz_data");
            quizIndex = parentIntent.getIntExtra("quiz_index", -1);
            questions.addAll(quiz.getQuestionsList());
            quizName.setText(quiz.getName());
            quizDescription.setText(quiz.getDescription());
            categoriesList.addAll(quiz.getCategories());
            categoryAdapter.notifyDataSetChanged();
            questionsAdapter.notifyDataSetChanged();
            if (quiz.isTimed())
                timedQuiz.setChecked(true);
            else
                timedQuiz.setChecked(false);
            if (quiz.getDifficulty() == 0)
                difficulty.setProgress(0f);
            else if (quiz.getDifficulty() == 1)
                difficulty.setProgress(1f);
            else
                difficulty.setProgress(2f);
            if (quiz.isLimitedTimer()) {
                limitedTimer.setChecked(true);
                unlimitedTimer.setChecked(false);
                timerLimit.setText(quiz.getTimerValue());
            }
            else {
                limitedTimer.setChecked(false);
                unlimitedTimer.setChecked(true);
            }
        }

    }

    public String getAlphaNumeric(int len) {

        char[] ch = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        char[] c = new char[len];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++) {
            c[i] = ch[random.nextInt(ch.length)];
        }

        return new String(c);
    }

    @SuppressLint("SetTextI18n")
    private void initStep3Views() {
        reviewQuizDescription.setText(quizDescription.getText().toString());
        reviewQuizName.setText(quizName.getText().toString());
        if (timedQuiz.isChecked())
            reviewQuizTimed.setText("Timed");
        else
            reviewQuizTimed.setText("Not timed");
        if (limitedTimer.isChecked())
            reviewQuizTimerLimit.setText(timerLimit.getText().toString());
        if (unlimitedTimer.isChecked())
            reviewQuizTimerLimit.setText("None");
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                QuizWizardActivity.this,LinearLayoutManager.HORIZONTAL, false);
        reviewCategoriesRV.setLayoutManager(layoutManager);
        reviewCategoriesRV.setAdapter(categoryAdapter);
    }

    private boolean computeQuestionCompletionAndRegisterQuestion() {
        if (questionText.getText().toString().equals(""))
            Toast.makeText(QuizWizardActivity.this,
                    "Question field is empty", Toast.LENGTH_SHORT).show();
        else
        if (answersList.isEmpty())
            Toast.makeText(QuizWizardActivity.this,
                    "You have no answers for this question", Toast.LENGTH_SHORT).show();
        else if (!checkForCorrectAnswers()){
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizWizardActivity.this);
            builder.setTitle("No correct answer")
                    .setMessage("There are no correct answers marked for this" +
                            " question. Please press on the correct answer or" +
                            " answers and check the checkbox.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create();
            builder.show();
        }
        else if (correctAnswers == answersList.size()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizWizardActivity.this);
            builder.setTitle("No wrong answer")
                    .setMessage("There are no incorrect answers marked for this" +
                            " question. Please uncheck the checkbox on one of the questions.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create();
            builder.show();
        }
        else {
            if (multipleChoice.isChecked()) {
                if (correctAnswers == 1)
                    Toast.makeText(QuizWizardActivity.this,
                            "Please select one more correct answer", Toast.LENGTH_SHORT).show();
                else {
                    registerQuestion();
                    return true;
                }
            }
            if (singleChoice.isChecked()) {
                if (correctAnswers > 1)
                    Toast.makeText(QuizWizardActivity.this,
                            "Only one correct answer allowed", Toast.LENGTH_SHORT).show();
                else {
                    registerQuestion();
                    return true;
                }
            }
            if (trueOrFalse.isChecked()) {
                if (correctAnswers > 1)
                    Toast.makeText(QuizWizardActivity.this,
                            "Only one correct answer allowed", Toast.LENGTH_SHORT).show();
                else {
                    registerQuestion();
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressLint("RestrictedApi")
    private void registerQuestion() {
        Question question = new Question();
        question.setQuestionText(questionText.getText().toString());
        question.setNumCorrectAnswers(correctAnswers);
        question.setExplanation(questionExplanation.getText().toString());
        if (trueOrFalse.isChecked())
            question.setQuestionType(0);
        else if (multipleChoice.isChecked())
            question.setQuestionType(1);
        else
            question.setQuestionType(2);
        for (int i = 0; i < answersList.size(); i++) {
            Answer a = answersList.get(i);
            if (a.isCorrectAnswer())
                question.addCorrectAnswer(a.getText());
            else
                question.addIncorrectAnswer(a.getText());
        }

        questions.add(question);
        questionsAdapter.notifyItemInserted(questions.size() - 1);
        questionText.setText("");
        answersList.clear();
        answersAdapter.notifyDataSetChanged();
        trueOrFalse.setChecked(false);
        singleChoice.setChecked(false);
        multipleChoice.setChecked(false);
        addAnswer.setVisibility(View.GONE);
        answersTV.setVisibility(View.GONE);
    }

    private void logAnswers() {
        for (int i = 0; i < answersList.size(); i++) {
            Log.d(TAG, "logAnswers: answer text -> " + answersList.get(i).getText());
            Log.d(TAG, "logAnswers: is correct answer -> " + answersList.get(i).isCorrectAnswer());
        }
    }

    private boolean checkForCorrectAnswers() {
        correctAnswers = 0;
        for (int i = 0; i < answersList.size(); i++) {
            if (answersList.get(i).isCorrectAnswer())
                correctAnswers++;
        }
        Log.d(TAG, "checkForCorrectAnswers: correct answers -> " + correctAnswers);
        if (correctAnswers == 0)
            return false;
        return true;
    }

    private void goForward(RelativeLayout from, RelativeLayout to, int step) {
        startTransitionOut(from, transitionDuration);
        startTransitionIn(to, transitionDuration);
        currentStep = step;
        goBack.setVisibility(View.VISIBLE);
    }

    private void startTransitionOut(final RelativeLayout relativeLayout, int transitionDuration) {
        relativeLayout.clearAnimation();
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        if (false)
            fadeOut.setStartOffset(transitionDuration);
        fadeOut.setDuration(transitionDuration);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                relativeLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                relativeLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        relativeLayout.startAnimation(fadeOut);
    }

    private void startTransitionIn(final RelativeLayout relativeLayout, int transitionDuration) {
        relativeLayout.clearAnimation();
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(transitionDuration);
        if (true)
            fadeIn.setStartOffset(transitionDuration);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                relativeLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                relativeLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        relativeLayout.startAnimation(fadeIn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null) {
                int index = data.getIntExtra("index", -1);
                Question editedQuestion = (Question) data.getSerializableExtra("question");
                Question question = questions.get(index);

                questions.set(index, editedQuestion);
                questionsAdapter.notifyItemChanged(index);
            }
        }
    }
}
