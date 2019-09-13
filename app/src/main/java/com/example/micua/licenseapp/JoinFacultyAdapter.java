package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class JoinFacultyAdapter extends RecyclerView.Adapter<JoinFacultyAdapter.ViewHolder> {
    private List<Faculty> faculties;
    private Context context;
    private User currentUser;
    private DBLinks dbLinks;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView firstLetter, name, belongingCollege, description, numMembers, numGroups;
        private ConstraintLayout body;

        public ViewHolder(View view) {
            super(view);

            firstLetter = view.findViewById(R.id.tv_faculty_name_first_letter);
            name = view.findViewById(R.id.tv_faculty_name);
            belongingCollege = view.findViewById(R.id.tv_referenced_college);
            description = view.findViewById(R.id.tv_faculty_description);
            numMembers = view.findViewById(R.id.tv_num_members);
            numGroups = view.findViewById(R.id.tv_num_groups);
            body = view.findViewById(R.id.cl_body);
        }
    }

    public JoinFacultyAdapter(List<Faculty> faculties, User user) {
        this.faculties = faculties;
        this.currentUser = user;
        this.dbLinks = new DBLinks();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.faculty_remastered_list_item, viewGroup, false);

        context = viewGroup.getContext();

        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull JoinFacultyAdapter.ViewHolder viewHolder, int i) {
        final Faculty faculty = faculties.get(i);

        viewHolder.name.setText(faculty.getFacultyName());
        if (!faculty.getFacultyDescription().equals(""))
            viewHolder.description.setText(faculty.getFacultyDescription());
        else
            viewHolder.description.setText("No description available");
        viewHolder.firstLetter.setText(faculty.getFacultyName().toCharArray()[0] + "");
        viewHolder.numGroups.setText(faculty.getNumGroups() + "");
        viewHolder.numMembers.setText(faculty.getNumMembers() + "");
        viewHolder.belongingCollege.setText(faculty.getReferencedCollegeName());

        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Join Faculty")
                        .setMessage("You are about to join " + faculty.getFacultyName()
                                + " . Do you want to continue ?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent intent = new Intent("faculty-joined-adapter");
                                intent.putExtra("joinedFaculty", faculty);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                currentUser.setJoinedFacultyName(currentUser.getJoinedFacultyName()
                                        + "_" + faculty.getFacultyName());
                                String url = dbLinks.getBaseLink() + "join-faculty?facultyId="
                                        + faculty.getId() + "&userId=" + currentUser.getId()
                                        + "&joinedFaculty=true&joinedFacultyName="
                                        + currentUser.getJoinedFacultyName();
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
                                            boolean userUpdated, facultyUpdated;
                                            userUpdated = object.getBoolean("userUpdated");
                                            facultyUpdated = object.getBoolean("facultyUpdated");
                                            if (facultyUpdated && userUpdated) {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                                builder.setTitle("Success")
                                                        .setMessage("You have successfully joined "
                                                                + faculty.getFacultyName()
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
                                                        builder.create();
                                                        builder.show();
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
        return this.faculties.size();
    }
}