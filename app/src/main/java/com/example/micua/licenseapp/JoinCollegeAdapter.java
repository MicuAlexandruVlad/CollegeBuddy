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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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

public class JoinCollegeAdapter extends RecyclerView.Adapter<JoinCollegeAdapter.ViewHolder> {
    private List<College> colleges;
    private Context context;
    private User currentUser;
    private DBLinks dbLinks;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, location, description;
        private ImageView collegeImage;
        private CardView body;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_college_name);
            location = view.findViewById(R.id.tv_college_location);
            description = view.findViewById(R.id.tv_college_description);
            collegeImage = view.findViewById(R.id.iv_college_image);
            body = view.findViewById(R.id.cv_body);
        }
    }

    public JoinCollegeAdapter(List<College> colleges, User user) {
        this.colleges = colleges;
        this.currentUser = user;
        this.dbLinks = new DBLinks();
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
    public void onBindViewHolder(@NonNull JoinCollegeAdapter.ViewHolder viewHolder, int i) {
        final College college = colleges.get(i);

        viewHolder.name.setText(college.getName());
        if (!college.getDescription().equals(""))
            viewHolder.description.setText(college.getDescription());
        else
            viewHolder.description.setText("No description available");
        viewHolder.location.setText(college.getCity() + ", " + college.getCountry());

        RequestOptions requestOptions = new RequestOptions()
                .override(650);
        Glide.with(context).asBitmap().apply(requestOptions).load(college.getPhotoUrls()).into(viewHolder.collegeImage);

        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("College Join")
                        .setMessage("You are about to join " + college.getName()
                         + ". Are you sure you want to continue ?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent intent = new Intent("college-joined-adapter");
                                intent.putExtra("joinedCollege", college);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                currentUser.setJoinedCollegeName(college.getName() + "_" +
                                        currentUser.getJoinedCollegeName());
                                String url = dbLinks.getBaseLink() + "join-college?collegeId="
                                        + college.getId() + "&joinedCollege=true&joinedCollegeName="
                                        + currentUser.getJoinedCollegeName() + "&userId=" + currentUser.getId();
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
                                            boolean userUpdated, collegeUpdated;
                                            userUpdated = object.getBoolean("userUpdated");
                                            collegeUpdated = object.getBoolean("collegeUpdated");
                                            if (userUpdated && collegeUpdated) {
                                                final AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                                builder1.setTitle("Success")
                                                        .setMessage("You have successfully joined " +
                                                                college.getName() + ".")
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
        return this.colleges.size();
    }
}