package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ShareQuizDialogAdapter extends RecyclerView.Adapter<ShareQuizDialogAdapter.ViewHolder> {
    private List<Group> groups;
    private Context context;
    private boolean groupSelected = false;
    private int selectedGroupPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView firstLetter, groupName;
        private RelativeLayout body;

        public ViewHolder(View view) {
            super(view);
            firstLetter = view.findViewById(R.id.tv_group_name_first_letter);
            groupName = view.findViewById(R.id.tv_group_name);
            body = view.findViewById(R.id.rl_body);
        }
    }

    public ShareQuizDialogAdapter(Context context, List<Group> groups) {
        this.groups = groups;
        this.context = context;
    }

    @NonNull
    @Override
    public ShareQuizDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_share_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ShareQuizDialogAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Group group = groups.get(i);

        viewHolder.firstLetter.setText(group.getGroupName().toCharArray()[0] + "");
        viewHolder.groupName.setText(group.getGroupName());
        if (!group.isGroupSelected()) {
            viewHolder.body.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_layout_round));
            viewHolder.groupName.setTextColor(Color.BLACK);
        }
        else {
            viewHolder.body.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_round_primary));
            viewHolder.groupName.setTextColor(Color.WHITE);
        }
        final int pos = i;


        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupSelected = true;
                if (!group.isGroupSelected()) {
                    viewHolder.body.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_round_primary));
                    viewHolder.groupName.setTextColor(Color.WHITE);
                    Log.d(ShareQuizDialog.TAG, "onClick: clicked item -> " + pos);
                    selectedGroupPosition = pos;
                }
                else {
                    viewHolder.body.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_layout_round));
                    viewHolder.groupName.setTextColor(Color.BLACK);
                    selectedGroupPosition = -1;
                    groupSelected = false;
                }
                group.setGroupSelected(!group.isGroupSelected());
                if (group.isGroupSelected())
                    setRemainingGroupsUnselected(pos);
                notifyDataSetChanged();
            }
        });
    }

    private void resetGroupSelections() {
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setGroupSelected(false);
        }
    }

    private void setRemainingGroupsUnselected(int pos) {
        for (int i = 0; i < groups.size(); i++) {
            if (i != pos) {
                groups.get(i).setGroupSelected(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public boolean isGroupSelected() {
        return groupSelected;
    }

    public int getSelectedGroupPosition() {
        return selectedGroupPosition;
    }
}
