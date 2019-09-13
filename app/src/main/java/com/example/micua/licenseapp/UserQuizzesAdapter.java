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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

public class UserQuizzesAdapter extends RecyclerView.Adapter<UserQuizzesAdapter.ViewHolder> {
    public static final String TAG = "UserQuizzesAdapter";

    private List<Quiz> quizzes;
    private List<QuizAsJSON> quizAsJSONList;
    private Context context;
    private User currentUser;
    private Repository repository;
    private List<Group> groups;

    public static final int EDIT_QUIZ_REQ_CODE = 192;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView quizName, createdBy, description, numQuestions, timer;
        private ImageView edit, difficulty, delete;
        private Button shareQuiz;

        public ViewHolder(View view) {
            super(view);
            quizName = view.findViewById(R.id.tv_quiz_name);
            createdBy = view.findViewById(R.id.tv_created_by);
            description = view.findViewById(R.id.tv_quiz_description);
            numQuestions = view.findViewById(R.id.tv_num_questions);
            timer = view.findViewById(R.id.tv_timer);
            edit = view.findViewById(R.id.iv_edit_quiz);
            difficulty = view.findViewById(R.id.iv_difficulty);
            delete = view.findViewById(R.id.iv_delete_quiz);
            shareQuiz = view.findViewById(R.id.btn_share_quiz);
        }
    }

    public UserQuizzesAdapter(List<Quiz> quizzes, Context context, User currentUser
            , List<QuizAsJSON> quizAsJSONList) {
        this.quizzes = quizzes;
        this.context = context;
        this.currentUser = currentUser;
        this.quizAsJSONList = quizAsJSONList;
        this.repository = new Repository(context);
    }

    @NonNull
    @Override
    public UserQuizzesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.quiz_list_item, viewGroup, false);

        return new UserQuizzesAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserQuizzesAdapter.ViewHolder viewHolder, final int i) {
        final Quiz quiz = quizzes.get(i);

        Log.d(TAG, "onBindViewHolder: quizAsJsonID -> " + quizAsJSONList.get(i).getId());

        viewHolder.quizName.setText(quiz.getName());
        viewHolder.description.setText(quiz.getDescription());
        viewHolder.numQuestions.setText(quiz.getNumQuestions() + " questions");
        viewHolder.createdBy.setText(quiz.getCreatedByName());

        if (quiz.getDifficulty() == 0)
            viewHolder.difficulty.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.difficulty_low));
        else if (quiz.getDifficulty() == 1)
            viewHolder.difficulty.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.difficulty_medium));
        else
            viewHolder.difficulty.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.difficulty_high));

        if (!quiz.isTimed())
            viewHolder.timer.setText("No timer");
        else if (quiz.isLimitedTimer())
            viewHolder.timer.setText(quiz.getTimerValue());
        else
            viewHolder.timer.setText("No limit");



        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, QuizWizardActivity.class);
                intent.putExtra("quiz_data", quiz);
                intent.putExtra("isEdit", true);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("quiz_index", i);
                intent.putExtra("quiz_id", quizAsJSONList.get(i).getId());
                ((Activity) context).startActivityForResult(intent, EDIT_QUIZ_REQ_CODE);
            }
        });

        viewHolder.shareQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: change this later on to share a quiz

                final ShareQuizDialog shareQuizDialog = new ShareQuizDialog(context, currentUser);
                shareQuizDialog.create();
                shareQuizDialog.setGroups(groups);
                Gson gson = new Gson();
                shareQuizDialog.setQuiz(gson.toJson(quiz));
                shareQuizDialog.show();
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: there seems to be an index problem when deleting a quiz after one has already been deleted

                final QuizAsJSON quizAsJSON = quizAsJSONList.get(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Quiz Removal")
                        .setMessage("You are about to remove " + quiz.getName()
                         + ". Are you sure you want to continue ?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        repository.removeQuiz(quizAsJSON);
                                    }
                                }).start();

                                quizAsJSONList.remove(i);
                                quizzes.remove(i);
                                notifyItemRemoved(i);

                                Log.d(TAG, "onClick: quizAsJson size -> " + quizAsJSONList.size());
                                Log.d(TAG, "onClick: quizList size -> " + quizzes.size());

                                Intent intent = new Intent("on-quiz-removed");
                                intent.putExtra("quiz_index", i);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        });
                builder.create();
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public void setQuizAsJSONList(List<QuizAsJSON> quizAsJSONList) {
        this.quizAsJSONList = quizAsJSONList;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
