package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

// TODO: update ui button after joining college

public class CollegeAdapter extends RecyclerView.Adapter<CollegeAdapter.ViewHolder>{
    private List<College> colleges;
    private Context context;
    private User currentUser;
    private DBLinks dbLinks;
    private RecyclerView recyclerView;
    private String[] collegesAll;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView collegePhoto, joinIcon;
        private TextView collegeName, location, numMembers, numGroups;
        private RelativeLayout joinCollege;
        private TextView joinTv;

        public ViewHolder(View view) {
            super(view);
            collegeName = view.findViewById(R.id.tv_college_name);
            joinIcon = view.findViewById(R.id.iv_join);
            joinTv = view.findViewById(R.id.tv_join);
            collegePhoto = view.findViewById(R.id.iv_college_photo);
            location = view.findViewById(R.id.tv_location);
            numGroups = view.findViewById(R.id.tv_num_groups);
            numMembers = view.findViewById(R.id.tv_num_members);
            joinCollege = view.findViewById(R.id.rl_join_college);
        }
    }

    public CollegeAdapter(List<College> colleges, User user, RecyclerView recyclerView, String[] collegesAll) {
        this.colleges = colleges;
        this.currentUser = user;
        this.dbLinks = new DBLinks();
        this.recyclerView = recyclerView;
        this.collegesAll = collegesAll;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.colleges_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        College college = colleges.get(i);

        viewHolder.collegeName.setText(college.getName());
        viewHolder.numMembers.setText(college.getNumMembers() + "");
        viewHolder.location.setText(college.getCity() + ", " + college.getCountry());
        viewHolder.numGroups.setText(college.getNumFaculties() + "");

        RequestOptions requestOptions = new RequestOptions()
                .override(700);

        Glide.with(context).asBitmap().apply(requestOptions).load(college.getPhotoUrls()).into(viewHolder.collegePhoto);

        if (currentUser.isJoinedCollege())
            if (currentUser.getJoinedCollegeName().contains(college.getId())) {
                viewHolder.joinTv.setText("Open");
                viewHolder.joinIcon.setVisibility(View.GONE);
            }

        final int pos = i;

        viewHolder.joinCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String url = dbLinks.getBaseLink() + "update-college";
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Join College")
                        .setMessage("You are about to join " + colleges.get(pos).getName() + ". Are " +
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
                                currentUser.setJoinedCollege(true);
                                currentUser.setJoinedCollegeName(currentUser.getJoinedCollegeName() +
                                        "_" + colleges.get(pos).getName());
                                colleges.get(pos).setNumMembers(colleges.get(pos).getNumMembers() + 1);
                                viewHolder.joinTv.setText("Open");
                                viewHolder.joinIcon.setVisibility(View.GONE);
                                viewHolder.numMembers.setText("" + colleges.get(pos).getNumMembers());
                                // TODO: get data for this college, after user joins, from database to update in app view
                                AsyncHttpClient client = new AsyncHttpClient();
                                RequestParams params = new RequestParams();
                                params.put("collegeName", colleges.get(pos).getName());
                                params.put("collegeNumMembers", colleges.get(pos).getNumMembers());
                                params.put("collegeNumFaculties", colleges.get(pos).getNumFaculties());
                                client.post(url, params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Log.d("Find", "numMembers of " + colleges.get(pos).getName() +
                                                " has been updated");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });

                                AsyncHttpClient client1 = new AsyncHttpClient();
                                RequestParams params1 = new RequestParams();
                                params1.put("email", currentUser.getEmail());
                                params1.put("joinedCollege", currentUser.isJoinedCollege());
                                params1.put("joinedCollegeName", currentUser.getJoinedCollegeName());
                                String url1 = dbLinks.getBaseLink() + "update-user";
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

                                Log.d("Find", "User joined: " + colleges.get(pos).getName());
                            }
                        });

                if (viewHolder.joinTv.getText().toString().equalsIgnoreCase("open")) {
                    Intent intent = new Intent(context, FacultyActivity.class);
                    intent.putExtra("currentCollege", colleges.get(pos))
                            .putExtra("currentUser", currentUser)
                            .putExtra("collegesAll", collegesAll);
                    context.startActivity(intent);
                }
                else
                    builder.create().show();
            }
        });



    }

    @Override
    public int getItemCount() {
        return colleges.size();
    }

}
