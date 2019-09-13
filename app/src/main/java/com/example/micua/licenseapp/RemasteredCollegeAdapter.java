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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.List;

import cz.msebera.android.httpclient.Header;

// TODO: update ui button after joining college

public class RemasteredCollegeAdapter extends RecyclerView.Adapter<RemasteredCollegeAdapter.ViewHolder>{
    private List<College> colleges;
    private Context context;
    private User currentUser;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, location;
        ImageView collegePhoto;
        CardView body;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_college_name);
            description = view.findViewById(R.id.tv_college_description);
            location = view.findViewById(R.id.tv_college_location);
            collegePhoto = view.findViewById(R.id.iv_college_image);
            body = view.findViewById(R.id.cv_body);
        }
    }

    public RemasteredCollegeAdapter(List<College> colleges, User user) {
        this.colleges = colleges;
        this.currentUser = user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.remastered_college_list_item, viewGroup, false);

        context = viewGroup.getContext();

        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final College college = colleges.get(i);

        if (!college.getDescription().equals(""))
            viewHolder.description.setText(college.getDescription());
        else
            viewHolder.description.setText("No description available");
        viewHolder.name.setText(college.getName());
        viewHolder.location.setText(college.getCity() + ", " + college.getCountry());

        RequestOptions requestOptions = new RequestOptions()
                .override(650);

        Glide.with(context).asBitmap().apply(requestOptions).load(college.getPhotoUrls()).into(viewHolder.collegePhoto);

        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CollegeDetailsActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("currentCollege", college);
                ((Activity) context).startActivityForResult(intent, 1001);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colleges.size();
    }

}
