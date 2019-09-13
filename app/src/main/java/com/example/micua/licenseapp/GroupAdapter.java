package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Group> groups;
    private Context context;
    private User currentUser;
    private Faculty currentFaculty;
    private DBLinks dbLinks;
    //private boolean isFromMain;

    //TODO: rework this shit

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView groupName, numMembers, groupDescription, joinTv, timestamp;
        private RelativeLayout joinGroup, groupMembers;
        private ImageView joinIcon;
        private CardView holder;

        public ViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.tv_group_name);
            joinGroup = view.findViewById(R.id.rl_join_group);
            numMembers = view.findViewById(R.id.tv_num_members1);
            groupDescription = view.findViewById(R.id.tv_group_description);
            joinTv = view.findViewById(R.id.tv_join);
            joinIcon = view.findViewById(R.id.iv_join);
            holder = view.findViewById(R.id.cv_holder);
            timestamp = view.findViewById(R.id.tv_timestamp);
            groupMembers = view.findViewById(R.id.rl_group_members);
        }
    }

    public GroupAdapter(List<Group> groups, User user, Faculty faculty) {
        this.groups = groups;
        this.currentUser = user;
        this.currentFaculty = faculty;
        this.dbLinks = new DBLinks();
        //this.isFromMain = isFromMain;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.groups_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.ViewHolder viewHolder, int i) {
        final Group group = groups.get(i);

        viewHolder.groupName.setText(group.getGroupName());

        viewHolder.joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Join Group")
                        .setMessage("You are about to join the " + group.getGroupName() + " group. " +
                                "Are you sure ?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int temporalNumMembers = group.getNumMembers() + 1;
                                String url = dbLinks.getBaseLink() + "user-joined-group?"
                                        + "email=" + currentUser.getEmail()
                                        + "&joinedGroupsAccessTokens="
                                        + currentUser.getJoinedGroupsAccessTokens() + group.getAccessToken() + "!@!"
                                        + "&numMembers=" + temporalNumMembers
                                        + "&groupName=" + group.getGroupName()
                                        + "&joinedGroup=true&profileSetupComplete=true";

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url(url).build();
                                Call call = client.newCall(request);

                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        String json = response.body().string();

                                        try {
                                            JSONObject object = new JSONObject(json);
                                            final boolean userUpdated = object.getBoolean("userUpdated");
                                            final boolean groupUpdated = object.getBoolean("groupUpdated");

                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (userUpdated && groupUpdated) {
                                                        currentUser.setJoinedGroup(true);
                                                        currentUser.setProfileSetupComplete(true);
                                                        currentUser.setJoinedGroupsAccessTokens(currentUser.getJoinedGroupsAccessTokens()
                                                                + group.getAccessToken() + "!@!");
                                                        showSetupCompleteDialog(group);
                                                    }
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                builder.create().show();
            }
        });
    }

    private void showSetupCompleteDialog(final Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Congratulations")
                .setMessage("You have completed the initial setup. You can now fully experience" +
                        " <<AppName>>.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("currentGroup", (Serializable) group);
                        intent.putExtra("currentUser", currentUser);
                        intent.putExtra("setupJustCompleted", true);
                        intent.putExtra("currentFaculty", currentFaculty);
                        context.startActivity(intent);
                    }
                });
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return this.groups.size();
    }
}

