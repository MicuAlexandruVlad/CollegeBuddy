package com.example.micua.licenseapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.ViewHolder> {
    private List<Answer> answers;
    private Context context;

    // 0 - true or false
    // 1 - multiple choice
    // 2 - single choice
    private int questionType;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView answerText;
        private RelativeLayout body;

        public ViewHolder(View view) {
            super(view);
            answerText = view.findViewById(R.id.tv_answer);
            body = view.findViewById(R.id.rl_answer_body);
        }
    }

    public AnswersAdapter(List<Answer> answers) {
        this.answers = answers;
    }

    @NonNull
    @Override
    public AnswersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.answer_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new AnswersAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Answer answer = answers.get(i);

        viewHolder.answerText.setText(answer.getText());

        if (!answer.isCorrectAnswer()) {
            viewHolder.answerText.setTextColor(Color.parseColor("#000000"));
            viewHolder.body.setBackground(ContextCompat.
                    getDrawable(context, R.drawable.btn_layout_round));
        }
        else {
            viewHolder.answerText.setTextColor(Color.parseColor("#000000"));
            viewHolder.body.setBackground(ContextCompat.
                    getDrawable(context, R.drawable.layout_round_correct_answer));
        }

        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddAnswerDialog dialog = new AddAnswerDialog(context);

                dialog.setEdit(true);
                dialog.setAnswerText(answer.getText());

                if (answer.isCorrectAnswer()) {
                    dialog.setCorrectAnswer(true);
                }
                else {
                    dialog.setCorrectAnswer(false);
                }

                dialog.create();
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (dialog.isAddOrEditPressed()) {
                            answer.setText(dialog.getAnswerText());
                            if (questionType != 0) {
                                answer.setCorrectAnswer(dialog.isCorrectAnswer());
                                notifyItemChanged(i);
                            }
                            else {
                                if (dialog.isCorrectAnswer()) {
                                    if (i == 0) {
                                        if (answers.get(1).isCorrectAnswer())
                                            Toast.makeText(context, "Only one correct answer allowed", Toast.LENGTH_SHORT).show();
                                        else {
                                            answer.setCorrectAnswer(dialog.isCorrectAnswer());
                                            notifyItemChanged(i);
                                        }
                                    }
                                    else {
                                        if (answers.get(0).isCorrectAnswer())
                                            Toast.makeText(context, "Only one correct answer allowed", Toast.LENGTH_SHORT).show();
                                        else {
                                            answer.setCorrectAnswer(dialog.isCorrectAnswer());
                                            notifyItemChanged(i);
                                        }
                                    }
                                }
                                else {
                                    answer.setCorrectAnswer(false);
                                    notifyItemChanged(i);
                                }
                            }

                        }

                        if (dialog.isDeletePressed()) {
                            answers.remove(i);
                            notifyItemRemoved(i);
                        }
                    }
                });
            }
        });
    }

    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }
}
