package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
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

public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> {
    private List<Group> groups;
    private Context context;
    private User currentUser;
    private Faculty currentFaculty;
    private DBLinks dbLinks;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView groupFirstLetter, numMembers, groupName, groupDescription;
        private RelativeLayout groupBody;

        public ViewHolder(View view) {
            super(view);
            groupFirstLetter = view.findViewById(R.id.tv_group_name_first_letter);
            numMembers = view.findViewById(R.id.tv_num_members);
            groupName = view.findViewById(R.id.tv_group_name);
            groupDescription = view.findViewById(R.id.tv_group_description);
            groupBody = view.findViewById(R.id.rl_group_body);
        }
    }

    public JoinGroupAdapter(List<Group> groups, User user, Faculty faculty) {
        this.groups = groups;
        this.currentUser = user;
        this.currentFaculty = faculty;
        this.dbLinks = new DBLinks();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.join_group_list_item, viewGroup, false);

        context = viewGroup.getContext();

        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull JoinGroupAdapter.ViewHolder viewHolder, int i) {
        final Group group = groups.get(i);

        viewHolder.groupDescription.setText(group.getGroupDescription());
        viewHolder.numMembers.setText(group.getNumMembers() + "");
        viewHolder.groupFirstLetter.setText(group.getGroupName().toCharArray()[0] + "");
        viewHolder.groupName.setText(group.getGroupName());
        viewHolder.groupBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Join group")
                        .setMessage("Are you sure you want to join " + group.getGroupName())
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                OkHttpClient client = new OkHttpClient();
                                // TODO: change url
                                currentUser.setJoinedGroupsAccessTokens(currentUser.getJoinedGroupsAccessTokens()
                                 + "!@!" + group.getAccessToken());
                                String url = dbLinks.getBaseLink() + "group-join-case-3?groupId=" +
                                        group.getId() + "&userId=" + currentUser.getId() +
                                        "&joinedGroup=" + currentUser.isJoinedGroup() +
                                        "&joinedGroupsAccessTokens=" + currentUser.getJoinedGroupsAccessTokens();
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
                                            boolean userUpdated, groupUpdated;
                                            userUpdated = object.getBoolean("userUpdated");
                                            groupUpdated = object.getBoolean("groupUpdated");
                                            if (userUpdated && groupUpdated) {

                                                Intent intent = new Intent("group-joined-adapter");
                                                intent.putExtra("joinedGroup", (Serializable) group);
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                                final AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                                builder1.setTitle("Success")
                                                        .setMessage("You have successfully joined " + group.getGroupName()
                                                         + ".")
                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        });
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        builder1.create();
                                                        builder1.show();
                                                    }
                                                });
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                builder.create();
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.groups.size();
    }
}