package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.List;

import cz.msebera.android.httpclient.Header;

//TODO: update ui button after joining faculty

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.ViewHolder> {
    private List<Faculty> faculties;
    private Context context;
    private User currentUser;
    private College currentCollege;
    private DBLinks dbLinks;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView joinIcon;
        private TextView facultyName, numMembers, numGroups;
        private RelativeLayout joinFaculty;
        private TextView joinTv;

        public ViewHolder(View view) {
            super(view);
            facultyName = view.findViewById(R.id.tv_faculty_name);
            joinIcon = view.findViewById(R.id.iv_join);
            joinTv = view.findViewById(R.id.tv_join);
            numGroups = view.findViewById(R.id.tv_num_groups);
            numMembers = view.findViewById(R.id.tv_num_members);
            joinFaculty = view.findViewById(R.id.rl_join_faculty);
        }
    }

    public FacultyAdapter(List<Faculty> faculties, User user, College college) {
        this.faculties = faculties;
        this.currentUser = user;
        this.currentCollege = college;
        this.dbLinks = new DBLinks();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.faculty_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Faculty faculty = faculties.get(i);

        viewHolder.facultyName.setText(faculty.getFacultyName());
        viewHolder.numMembers.setText(faculty.getNumMembers() + "");
        viewHolder.numGroups.setText(faculty.getNumGroups() + "");

        if (currentUser.isJoinedFaculty())
            if (currentUser.getJoinedFacultyName().contains(faculty.getId())) {
                viewHolder.joinTv.setText("Open");
                viewHolder.joinIcon.setVisibility(View.GONE);
            }


        final int pos = i;

        //TODO: fix update method for college with faculties number and update current faculty number of members accordingly - done (i think)?
        viewHolder.joinFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Join Faculty")
                        .setMessage("You are about to join " + faculties.get(pos).getFacultyName() + ". Are " +
                                "you sure ?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (currentUser.isJoinedFaculty())
                                    currentUser.setJoinedFacultyName(currentUser.getJoinedFacultyName() +
                                            "_" + faculties.get(pos).getFacultyName());
                                else
                                    currentUser.setJoinedFacultyName(faculties.get(pos).getFacultyName() + "_");
                                currentUser.setJoinedFaculty(true);
                                faculties.get(pos).setNumMembers(faculties.get(pos).getNumMembers() + 1);

                                // update user
                                AsyncHttpClient client1 = new AsyncHttpClient();
                                RequestParams params1 = new RequestParams();
                                params1.put("email", currentUser.getEmail());
                                params1.put("joinedFaculty", currentUser.isJoinedFaculty());
                                params1.put("joinedFacultyName", currentUser.getJoinedFacultyName());
                                String url1 = dbLinks.getBaseLink() + "update-user-faculty";
                                client1.post(url1, params1, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Log.d("Find", "User with email: " + currentUser.getEmail() +
                                                " has been updated");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });

                                // update faculty
                                AsyncHttpClient client2 = new AsyncHttpClient();
                                RequestParams params2 = new RequestParams();
                                params2.put("facultyName", faculties.get(pos).getFacultyName());
                                params2.put("facultyNumMembers", faculties.get(pos).getNumMembers());
                                String url2 = dbLinks.getBaseLink() + "update-faculty-num-members";
                                client2.post(url2, params2, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Log.d("Find", "Faculty with name: " + faculties.get(pos).getFacultyName() +
                                                " has been updated");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });

                                Intent intent = new Intent(context, FindGroupsActivity.class);
                                intent.putExtra("currentCollege", currentCollege)
                                        .putExtra("currentUser", currentUser)
                                        .putExtra("currentFaculty", faculties.get(pos));
                                context.startActivity(intent);

                                Log.d("Find", "User joined: " + faculties.get(pos).getFacultyName());
                            }
                        });

                if (viewHolder.joinTv.getText().toString().equalsIgnoreCase("open")) {
                    Intent intent = new Intent(context, FindGroupsActivity.class);
                    intent.putExtra("currentCollege", currentCollege)
                            .putExtra("currentUser", currentUser)
                            .putExtra("currentFaculty", faculties.get(pos));
                    context.startActivity(intent);
                }
                else {
                    builder.create().show();
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return faculties.size();
    }
}
