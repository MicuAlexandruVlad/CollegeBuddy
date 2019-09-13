package com.example.micua.licenseapp;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import me.gujun.android.taggroup.TagGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RunQuizActivity extends AppCompatActivity {
    public static final String TAG = "RunQuizActivity";

    private TextView title, description, createdBy, numQuestions, timer, questionText, remainingQuestions;
    private TextView cancelQuiz, timerValue, completionRate, tv1, tv2, lastMessage, tv5, placement;
    private RelativeLayout startQuiz, body, quizLayout, timerHolder, scoreLayout;
    private ImageView difficulty, back, illustration1;
    private ScrollView questionSV;
    private RecyclerView answersRV;
    private Button nextQuestion, finish, seeResults;
    private LinearLayout buttonsHolder;
    private TagGroup tagGroup;

    private Intent parentIntent;
    private Quiz quiz;
    private int questionIndex = -1, seconds = 0, minutes = 0;
    private List<Answer> answers;
    private List<Question> questions;
    private RunQuizAnswerAdapter answerAdapter;
    private List<Double> scoreArray;
    private CountDownTimer countDownTimer;
    private int quizId;
    private List<LeaderBoardEntry> leaderBoardEntries;
    private LeaderBoardEntry userLeaderBoardEntry;
    private DBLinks dbLinks;
    private User currentUser;
    private long millis = 0;
    private HashMap<Integer, ArrayList<Answer>> pickedAnswers;

    private AnimationFactory animationFactory;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_quiz);

        Objects.requireNonNull(getSupportActionBar()).hide();

        dbLinks = new DBLinks();
        userLeaderBoardEntry = new LeaderBoardEntry();
        answers = new ArrayList<>();
        scoreArray = new ArrayList<>();
        leaderBoardEntries = new ArrayList<>();
        animationFactory = new AnimationFactory();
        parentIntent = getIntent();
        quiz = (Quiz) parentIntent.getSerializableExtra("quiz");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");

        questions = quiz.getQuestionsList();
        reqLeaderBoardForQuiz();

        bindViews();

        pickedAnswers = new HashMap<>();

        questionText.setVisibility(View.INVISIBLE);
        answersRV.setVisibility(View.INVISIBLE);

        answerAdapter = new RunQuizAnswerAdapter(answers, this, new User());
        answersRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        answersRV.setAdapter(answerAdapter);

        title.setText(quiz.getName());
        description.setText(quiz.getDescription());
        createdBy.setText(quiz.getCreatedByName());
        numQuestions.setText(quiz.getNumQuestions() + " questions");
        if (quiz.getDifficulty() == 0)
            difficulty.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.difficulty_low));
        else if (quiz.getDifficulty() == 1)
            difficulty.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.difficulty_medium));
        else
            difficulty.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.difficulty_high));

        if (!quiz.isTimed())
            timer.setText("No timer");
        else if (quiz.isLimitedTimer())
            timer.setText(quiz.getTimerValue());
        else
            timer.setText("No limit");

        String[] categories = new String[quiz.getCategories().size()];

        for (int i = 0; i < quiz.getCategories().size(); i++) {
            categories[i] = quiz.getCategories().get(i);
        }

        tagGroup.setTags(categories);

        back.setVisibility(View.GONE);
        quizLayout.setVisibility(View.GONE);
        scoreLayout.setVisibility(View.GONE);
        body.setVisibility(View.VISIBLE);

        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasMultipleChoice()) {
                    showMultipleChoiceDialog();
                    initializeTimer();
                }
                else {
                    body.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                    quizLayout.setVisibility(View.VISIBLE);
                    questionIndex++;
                    switchQuestion();
                    animateQuestionViewsIn(false);
                    initializeTimer();
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionIndex == questions.size() - 1)
                    nextQuestion.setText("Next Question");

                if (questionIndex == 0) {
                    back.setVisibility(View.GONE);
                    quizLayout.setVisibility(View.GONE);
                    body.setVisibility(View.VISIBLE);
                    answers.clear();
                    answerAdapter.notifyDataSetChanged();
                    questionIndex--;
                    countDownTimer.cancel();
                    seconds = 0;
                    minutes = 0;
                }
                else {
                    scoreArray.remove(scoreArray.size() - 1);
                    pickedAnswers.remove(questionIndex);
                    questionIndex--;
                    //switchQuestion();
                    animateQuestionViewsOut(true);
                }
            }
        });

        cancelQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionIndex < questions.size() - 1) {
                    ArrayList<Answer> answers1 = new ArrayList<>();
                    for (int i = 0; i < answers.size(); i++) {
                        if (answers.get(i).isChecked()) {
                            answers1.add(answers.get(i));
                        }
                    }
                    pickedAnswers.put(questionIndex, answers1);

                    questionIndex++;
                    scoreArray.add(answerAdapter.getScoreForAnswer());
                    animateQuestionViewsOut(true);
                }

                if (nextQuestion.getText().toString().equalsIgnoreCase("Finish Attempt")) {
                    ArrayList<Answer> answers1 = new ArrayList<>();
                    for (int i = 0; i < answers.size(); i++) {
                        if (answers.get(i).isChecked()) {
                            answers1.add(answers.get(i));
                        }
                    }
                    pickedAnswers.put(questionIndex, answers1);

                    scoreArray.add(answerAdapter.getScoreForAnswer());
                    double score = 0.0;
                    countDownTimer.cancel();
                    for (int i = 0; i < scoreArray.size(); i++) {
                        score += scoreArray.get(i);
                    }
                    Log.d(TAG, "onClick: user score -> " + score);
                    Log.d(TAG, "onClick: possible score -> " + quiz.getQuestionsList().size());
                    long scorePercentage = Math.round((score / quiz.getQuestionsList().size()) * 100);

                    postLeaderBoardEntry(scorePercentage);

                    quizLayout.setVisibility(View.GONE);
                    scoreLayout.setVisibility(View.VISIBLE);
                    if (quiz.isTimed()) {
                        if (quiz.isLimitedTimer())
                            placement.setText(formatPlacement(findLeaderBoardPlace(millis, scorePercentage, leaderBoardEntries)));
                        else
                            placement.setText(formatPlacement(findLeaderBoardPlace((minutes * 60 + seconds) * 1000, scorePercentage, leaderBoardEntries)));
                    }
                    else
                        placement.setText(formatPlacement(findLeaderBoardPlace(-1, scorePercentage, leaderBoardEntries)));

                    animateScoreScreen(scorePercentage);
                    Log.d(TAG, "onClick: score percentage -> " + scorePercentage);
                }

                if (questionIndex == questions.size() - 1)
                    nextQuestion.setText("Finish Attempt");
            }
        });

        placement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RunQuizActivity.this, LeaderBoardActivity.class);
                intent.putExtra("entries", (Serializable) leaderBoardEntries);
                startActivity(intent);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        seeResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RunQuizActivity.this, QuizResultReviewActivity.class);
                intent.putExtra("question_list", (Serializable) questions);
                intent.putExtra("picked_answers", (Serializable) pickedAnswers);
                intent.putExtra("score_array", (Serializable) scoreArray);
                startActivity(intent);
            }
        });
    }

    private String formatPlacement(int leaderBoardPlace) {
        Map<String, String> map = new HashMap<>();
        map.put("0", "th");
        map.put("1", "st");
        map.put("2", "nd");
        map.put("3", "rd");
        map.put("4", "th");
        map.put("5", "th");
        map.put("6", "th");
        map.put("7", "th");
        map.put("8", "th");
        map.put("9", "th");

        String lastDigit = (leaderBoardPlace + "").toCharArray()[(leaderBoardPlace + "")
                .toCharArray().length - 1] + "";
        return leaderBoardPlace + map.get(lastDigit);
    }

    private void postLeaderBoardEntry(double percentage) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("referencedUserId", currentUser.getId());
        params.put("referencedQuizId", quiz.getIdToReference());
        params.put("referencedUserFullName", currentUser.getFirstName() + " " + currentUser.getLastName());
        params.put("quizHasTimer", quiz.isTimed());
        if (quiz.isTimed()) {
            if (quiz.isLimitedTimer()) {
                params.put("completionTime", millisInMinutesAndSeconds(millis));
                params.put("completionTimeInMillis", millis);
            }
            else {
                params.put("completionTime", minutes + ":" + seconds);
                params.put("completionTimeInMillis", (minutes * 60 + seconds) * 1000);
            }
        }
        params.put("completionPercentage", percentage);

        client.post(dbLinks.getBaseLink() + "insert-leader-board-entry",
                params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "onSuccess: inserted leader board entry");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
    }

    private void reqLeaderBoardForQuiz() {
        String url = dbLinks.getBaseLink() + "leader-board-for-quiz?referencedQuizId="
                + quiz.getIdToReference();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Log.d(TAG, "postFB: url -> " + url);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject res = new JSONObject(json);
                    JSONArray resArray = res.getJSONArray("result");
                    for (int i = 0; i < resArray.length(); i++) {
                        JSONObject object = resArray.getJSONObject(i);
                        LeaderBoardEntry entry = new LeaderBoardEntry();
                        entry.setId(object.getString("_id"));
                        entry.setCompletionTime(object.getString("completionTime"));
                        entry.setReferencedUserId(object.getString("referencedUserId"));
                        entry.setReferencedUserFullName(object.getString("referencedUserFullName"));
                        entry.setReferencedQuizId(object.getString("referencedQuizId"));
                        entry.setCompletionTimeInMillis(object.getLong("completionTimeInMillis"));
                        entry.setCompletionPercentage(object.getDouble("completionPercentage"));
                        entry.setQuizHasTimer(object.getBoolean("quizHasTimer"));
                        leaderBoardEntries.add(entry);
                    }
                    Log.d(TAG, "onResponse: json -> " + json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void animateQuestionViewsOut(final boolean switchQ) {
        Animation questionFadeIn = animationFactory.generateFadeOutAnimation(400, 0,
                new DecelerateInterpolator(), true);
        Animation rvFadeIn = animationFactory.generateFadeOutAnimation(400, 150,
                new DecelerateInterpolator(), true);
        questionText.clearAnimation();
        answersRV.clearAnimation();
        questionText.startAnimation(questionFadeIn);
        answersRV.startAnimation(rvFadeIn);

        if (switchQ) {
            rvFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    switchQuestion();
                    animateQuestionViewsIn(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void animateQuestionViewsIn(boolean switchQ) {
        Animation questionFadeIn = animationFactory.generateFadeInAnimation(400, 0,
                new DecelerateInterpolator(), true);
        Animation rvFadeIn = animationFactory.generateFadeInAnimation(400, 150,
                new DecelerateInterpolator(), true);
        questionText.clearAnimation();
        answersRV.clearAnimation();
        questionText.startAnimation(questionFadeIn);
        answersRV.startAnimation(rvFadeIn);

        if (switchQ) {
            rvFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    switchQuestion();
                    animateQuestionViewsOut(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void animateScoreScreen(final long score) {
        completionRate.setText("0 %");
        if (score <= 50) {
            lastMessage.setText("Seems like you need a little bit more studying.");
        }
        else {
            lastMessage.setText("Well done ! You are a natural.");
        }
        tv1.setVisibility(View.INVISIBLE);
        tv2.setVisibility(View.INVISIBLE);
        lastMessage.setVisibility(View.INVISIBLE);
        illustration1.setVisibility(View.INVISIBLE);
        buttonsHolder.setVisibility(View.INVISIBLE);
        completionRate.setVisibility(View.INVISIBLE);

        Animation a1 = animationFactory.generateFadeInAnimation(600, 0,
                new AccelerateDecelerateInterpolator(), true);
        Animation a2 = animationFactory.generateFadeInAnimation(600, 150,
                new AccelerateDecelerateInterpolator(), true);
        Animation a3 = animationFactory.generateFadeInAnimation(600, 250,
                new AccelerateDecelerateInterpolator(), true);
        Animation a4 = animationFactory.generateFadeInAnimation(600, 350,
                new AccelerateDecelerateInterpolator(), true);
        final Animation a5 = animationFactory.generateFadeInAnimation(600, 0,
                new AccelerateDecelerateInterpolator(), true);
        Animation a6 = animationFactory.generateFadeInAnimation(600, 550,
                new AccelerateDecelerateInterpolator(), true);

        tv1.startAnimation(a1);
        tv2.startAnimation(a3);
        illustration1.startAnimation(a2);
        buttonsHolder.startAnimation(a6);
        completionRate.startAnimation(a4);

        a4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                completionRate.setVisibility(View.VISIBLE);
                //completionRate.clearAnimation();
                startCountAnimation((int) score, a5);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startCountAnimation(final int percentage, final Animation animation1) {
        final Animation a2 = animationFactory.generateFadeInAnimation(600, 150,
                new AccelerateDecelerateInterpolator(), true);
        final Animation a3 = animationFactory.generateFadeInAnimation(600, 250,
                new AccelerateDecelerateInterpolator(), true);
        ValueAnimator animator = ValueAnimator.ofInt(0, percentage);
        animator.setDuration(3000);
        animator.setStartDelay(1800);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint("SetTextI18n")
            public void onAnimationUpdate(ValueAnimator animation) {
                completionRate.setText(animation.getAnimatedValue().toString() + " %");
                if (Integer.valueOf(animation.getAnimatedValue().toString()) == percentage) {
                    lastMessage.startAnimation(animation1);
                    tv5.startAnimation(a2);
                    placement.startAnimation(a3);
                }
            }
        });
        animator.start();
    }

    private void initializeTimer() {

        if (quiz.isTimed()) {
            timerHolder.setVisibility(View.VISIBLE);
            if (quiz.isLimitedTimer()) {
                countDownTimer = new CountDownTimer(timeInMillis(), 1000) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTick(long l) {
                        timerValue.setText(millisInMinutesAndSeconds(l) + "  left");
                        millis = l;
                    }

                    @Override
                    public void onFinish() {
                        showTimeIsUpDialog();
                    }
                }.start();
            }
            else {
                countDownTimer = new CountDownTimer(36000000, 1000) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTick(long l) {
                        seconds++;
                        if (seconds == 60) {
                            minutes++;
                            seconds = 0;
                        }
                        if (minutes < 10 && seconds < 10)
                            timerValue.setText("0" + minutes + ":0" + seconds);
                        if (minutes >= 10 && seconds < 10)
                            timerValue.setText(minutes + ":0" + seconds);
                        if (minutes < 10 && seconds >= 10)
                            timerValue.setText("0" + minutes + ":" + seconds);
                        if (minutes >= 10 && seconds >= 10)
                            timerValue.setText(minutes + ":" + seconds);
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        }
        else {
            timerHolder.setVisibility(View.GONE);
        }
    }

    private void showTimeIsUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RunQuizActivity.this);
        builder.setTitle("Time's Up")
                .setMessage("Timer has reached 00:00. Unfortunately you didn't make it to the end of the quiz" +
                        ". The points that you have racked up so far will still count.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        dialogInterface.dismiss();
                        ArrayList<Answer> answers1 = new ArrayList<>();
                        for (int i = 0; i < answers.size(); i++) {
                            if (answers.get(i).isChecked()) {
                                answers1.add(answers.get(i));
                            }
                        }
                        pickedAnswers.put(questionIndex, answers1);

                        scoreArray.add(answerAdapter.getScoreForAnswer());
                        double score = 0.0;
                        countDownTimer.cancel();
                        for (int i = 0; i < scoreArray.size(); i++) {
                            score += scoreArray.get(i);
                        }
                        Log.d(TAG, "onClick: user score -> " + score);
                        Log.d(TAG, "onClick: possible score -> " + quiz.getQuestionsList().size());
                        long scorePercentage = Math.round((score / quiz.getQuestionsList().size()) * 100);

                        postLeaderBoardEntry(scorePercentage);

                        quizLayout.setVisibility(View.GONE);
                        scoreLayout.setVisibility(View.VISIBLE);
                        if (quiz.isTimed()) {
                            if (quiz.isLimitedTimer())
                                placement.setText(formatPlacement(findLeaderBoardPlace(millis, scorePercentage, leaderBoardEntries)));
                            else
                                placement.setText(formatPlacement(findLeaderBoardPlace((minutes * 60 + seconds) * 1000, scorePercentage, leaderBoardEntries)));
                        }
                        else
                            placement.setText(formatPlacement(findLeaderBoardPlace(-1, scorePercentage, leaderBoardEntries)));

                        animateScoreScreen(scorePercentage);
                        Log.d(TAG, "onClick: score percentage -> " + scorePercentage);
                    }
                });

        builder.create();
        builder.show();
    }

    private int timeInMillis() {
        String time = quiz.getTimerValue();
        int minutes = Integer.parseInt(time.split(":")[0]);
        int seconds = Integer.parseInt(time.split(":")[1]);


        return seconds * 1000 + minutes * 60 * 1000;
    }

    private String millisInMinutesAndSeconds(long millis) {
        long seconds = millis / 1000;
        long minutesLeft = seconds / 60;
        long secondsLeft;

        while (seconds >= 60) {
            seconds -= 60;
        }

        secondsLeft = seconds;

        return minutesLeft + ":" + secondsLeft;
    }

    private boolean hasMultipleChoice() {
        for (Question q: quiz.getQuestionsList()) {
            if (q.getQuestionType() == 1)
                return true;
        }
        return false;
    }

    private void showMultipleChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Notice")
                .setMessage("There are multiple choice questions in this quiz. Please beware that not" +
                        " all answers are correct. You will be awarded a portion of the total score for a particular question" +
                        " if you don't mark all the correct answers")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        body.setVisibility(View.GONE);
                        back.setVisibility(View.VISIBLE);
                        quizLayout.setVisibility(View.VISIBLE);
                        questionIndex++;
                        switchQuestion();
                        animateQuestionViewsIn(false);
                        dialogInterface.dismiss();
                    }
                });

        builder.create();
        builder.show();
    }

    private void bindViews() {
        title = findViewById(R.id.tv_quiz_name);
        description = findViewById(R.id.tv_quiz_description);
        createdBy = findViewById(R.id.tv_created_by);
        numQuestions = findViewById(R.id.tv_num_questions);
        timer = findViewById(R.id.tv_timer);
        questionText = findViewById(R.id.tv_question);
        remainingQuestions = findViewById(R.id.tv_remaining_questions);
        cancelQuiz = findViewById(R.id.tv_cancel_quiz);
        timerValue = findViewById(R.id.tv_timer_value);
        tv1 = findViewById(R.id.tv_top);
        tv2 = findViewById(R.id.tv_3);
        completionRate = findViewById(R.id.tv_completion_rate);
        lastMessage = findViewById(R.id.tv_final_message);
        tv5 = findViewById(R.id.tv_5);
        placement = findViewById(R.id.tv_leader_board_placement);
        startQuiz = findViewById(R.id.rl_start_quiz);
        body = findViewById(R.id.rl_quiz_welcome);
        quizLayout = findViewById(R.id.rl_quiz_layout);
        timerHolder = findViewById(R.id.rl_timer_holder);
        scoreLayout = findViewById(R.id.rl_score_layout);
        difficulty = findViewById(R.id.iv_difficulty);
        back = findViewById(R.id.iv_back);
        illustration1 = findViewById(R.id.iv_illustration_1);
        questionSV = findViewById(R.id.sv_question);
        answersRV = findViewById(R.id.rv_answers);
        nextQuestion = findViewById(R.id.btn_next_question);
        finish = findViewById(R.id.btn_finish);
        seeResults = findViewById(R.id.btn_see_results);
        buttonsHolder = findViewById(R.id.ll_buttons_holder);
        tagGroup = findViewById(R.id.tg_categories);
    }

    @SuppressLint("SetTextI18n")
    private void switchQuestion() {
        clearAnswersList();
        remainingQuestions.setText((questionIndex + 1) + " of " + quiz.getQuestionsList().size());
        initAnswerList(questionIndex);
        Collections.shuffle(answers);
        answerAdapter.notifyDataSetChanged();
        answerAdapter.notifyDataSetChanged();
        questionText.setText(questions.get(questionIndex).getQuestionText());
        if (questions.get(questionIndex).getQuestionType() == 1)
            answerAdapter.setMultipleChoice(true);
        else
            answerAdapter.setMultipleChoice(false);
    }

    private void clearAnswersList() {
        for (int i = answers.size() - 1; i >= 0; i--) {
            answers.remove(i);
            answerAdapter.notifyItemRemoved(answers.size() - 1);
        }
    }

    private void initAnswerList(int questionIndex) {
        Question question = questions.get(questionIndex);
        for (int i = 0; i < question.getCorrectAnswers().size(); i++) {
            Answer answer = new Answer();
            answer.setCorrectAnswer(true);
            answer.setText(question.getCorrectAnswers().get(i));
            answers.add(answer);
            answerAdapter.notifyItemInserted(answers.size() - 1);
        }

        for (int i = 0; i < question.getIncorrectAnswers().size(); i++) {
            Answer answer = new Answer();
            answer.setCorrectAnswer(false);
            answer.setText(question.getIncorrectAnswers().get(i));
            answers.add(answer);
            answerAdapter.notifyItemInserted(answers.size() - 1);
        }
    }

    private int findLeaderBoardPlace(long time, double percentage, List<LeaderBoardEntry> entries) {
        LeaderBoardEntry userEntry = new LeaderBoardEntry();
        userEntry.setCompletionTimeInMillis(time);
        userEntry.setCompletionTime(millisInMinutesAndSeconds(time));
        userEntry.setQuizHasTimer(quiz.isTimed());
        userEntry.setReferencedQuizId(quiz.getIdToReference());
        userEntry.setReferencedUserFullName(currentUser.getFirstName() + " " + currentUser.getLastName());
        userEntry.setReferencedUserId(currentUser.getId());
        userEntry.setCompletionPercentage(percentage);
        Log.d(TAG, "findLeaderBoardPlace: percentage -> " + percentage);
        Log.d(TAG, "findLeaderBoardPlace: time -> " + time);

        Log.d(TAG, "findLeaderBoardPlace: quiz is timed -> " + quiz.isTimed());
        if (entries.isEmpty())
            return 1;
        else {
            if (quiz.isTimed()) {
                for (int i = 0; i < entries.size(); i++) {
                    LeaderBoardEntry entry = entries.get(i);

                    if (percentage > entry.getCompletionPercentage()) {
                        Log.d(TAG, "findLeaderBoardPlace: placement -> " + (i + 1));
                        entries.add(i, userEntry);
                        return i + 1;
                    }

                    if (percentage == entry.getCompletionPercentage()) {
                        if (time <= entry.getCompletionTimeInMillis()) {
                            Log.d(TAG, "findLeaderBoardPlace: placement -> " + (i + 1));
                            entries.add(i, userEntry);
                            return i + 1;
                        }
                        else {
                            for (int j = i; j < entries.size(); j++) {
                                if (percentage != entries.get(j).getCompletionPercentage()) {
                                    Log.d(TAG, "findLeaderBoardPlace: placement -> " + (j + 1));
                                    entries.add(j, userEntry);
                                    return j + 1;
                                }
                                else {
                                    if (time <= entries.get(j).getCompletionTimeInMillis()) {
                                        Log.d(TAG, "findLeaderBoardPlace: placement -> " + (j + 1));
                                        entries.add(j, userEntry);
                                        return j + 1;
                                    }

                                }
                            }
                        }
                    }

                    if (percentage < entry.getCompletionPercentage()) {
                        for (int j = i; j < entries.size(); j++) {
                            if (percentage == entries.get(j).getCompletionPercentage()) {
                                i = j;
                                break;
                            }
                            else {
                                if (percentage > entries.get(j).getCompletionPercentage()) {
                                    Log.d(TAG, "findLeaderBoardPlace: placement -> " + (j + 1));
                                    entries.add(j, userEntry);
                                    return j + 1;
                                }
                                else if (j == entries.size() - 1) {
                                    Log.d(TAG, "findLeaderBoardPlace: placement -> " + (j + 1));
                                    entries.add(j, userEntry);
                                    return j + 1;
                                }
                            }
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < entries.size(); i++) {
                    LeaderBoardEntry entry = entries.get(i);
                    if (percentage >= entry.getCompletionPercentage()) {
                        Log.d(TAG, "findLeaderBoardPlace: first if -> " + true);
                        entries.add(i, userEntry);
                        Log.d(TAG, "findLeaderBoardPlace: placement -> " + (i + 1));
                        return i + 1;
                    }
                }
            }
        }

        entries.add(userEntry);
        return entries.size();
    }
}
