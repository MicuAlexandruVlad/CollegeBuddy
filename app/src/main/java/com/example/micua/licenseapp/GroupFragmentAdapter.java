package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupFragmentAdapter extends RecyclerView.Adapter<GroupFragmentAdapter.ViewHolder> {
    private List<Group> groups;
    private Context context;
    private User currentUser;
    private TimeUtils timeUtils;
    private Constants constants;

    public static final String TAG = "GroupFragmentAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName, lastMessageText, lastMessageTime, lastMessageSender
                , groupFirstLetter;
        private RelativeLayout groupBody;

        public ViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.tv_group_name);
            lastMessageText = view.findViewById(R.id.tv_last_message_text);
            lastMessageTime = view.findViewById(R.id.tv_last_message_time);
            lastMessageSender = view.findViewById(R.id.tv_last_message_sender);
            groupFirstLetter = view.findViewById(R.id.tv_group_name_first_letter);
            groupBody = view.findViewById(R.id.rl_group_body);
        }
    }

    public GroupFragmentAdapter(List<Group> groups, User user) {
        this.groups = groups;
        this.currentUser = user;
        this.timeUtils = new TimeUtils();
        this.constants = new Constants();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_fragment_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroupFragmentAdapter.ViewHolder viewHolder, int i) {
        final Group group = groups.get(i);

        viewHolder.groupName.setText(group.getGroupName());
        viewHolder.groupFirstLetter.setText(group.getGroupName().toCharArray()[0] + "");
        Log.d(TAG, "onBindViewHolder: groupId -> " + group.getId());

        Message lastMessage = group.getLastMessage();

        if (lastMessage == null)
            viewHolder.lastMessageText.setText("No messages");
        else if (!lastMessage.getReferencedUserEmail().equals(currentUser.getEmail())) {
            viewHolder.lastMessageSender.setText(lastMessage.getSentBy());
            if (lastMessage.getType() == constants.MESSAGE_TEXT) {
                viewHolder.lastMessageText.setText(lastMessage.getMessage());
            }
            if (lastMessage.getType() == constants.MESSAGE_PHOTO)
                viewHolder.lastMessageText.setText("Sent an image");
            if (lastMessage.getType() == constants.MESSAGE_QUIZ)
                viewHolder.lastMessageText.setText("Sent a quiz");
            if (lastMessage.getType() == constants.MESSAGE_FILE) {
                viewHolder.lastMessageText.setText("Sent a file");
            }
        }
        else {
            viewHolder.lastMessageSender.setText("Me");
            if (lastMessage.getType() == constants.MESSAGE_TEXT) {
                viewHolder.lastMessageText.setText(lastMessage.getMessage());
            }
            if (lastMessage.getType() == constants.MESSAGE_PHOTO)
                viewHolder.lastMessageText.setText("Sent an image");
            if (lastMessage.getType() == constants.MESSAGE_QUIZ)
                viewHolder.lastMessageText.setText("Sent a quiz");
            if (lastMessage.getType() == constants.MESSAGE_FILE) {
                viewHolder.lastMessageText.setText("Sent a file");
            }
        }

        if (lastMessage != null) {
            try {
                String time = timeUtils.computeTimeDifference(lastMessage.getTimestamp());
                if (time.equals("Error"))
                    Log.d(TAG, "onBindViewHolder: Error converting time. Most likely pattern error.");
                else
                    viewHolder.lastMessageTime.setText(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        viewHolder.groupBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("currentGroup", (Serializable) group);
                intent.putExtra("current_group_id", group.getId());
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("group_list", (Serializable) groups);
                for (int j = 0; j < groups.size(); j++) {
                    intent.putExtra("id_" + j, groups.get(j).getId());
                }
                ((Activity) context).startActivityForResult(intent, 111);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.groups.size();
    }
}

