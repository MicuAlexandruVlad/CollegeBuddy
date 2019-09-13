package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {
    private List<LeaderBoardEntry> entries;
    private Context context;
    private ColorUtils colorUtils;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView placement, percentage, name, time;
        private CardView percentageCard;

        public ViewHolder(View view) {
            super(view);
            placement = view.findViewById(R.id.tv_place);
            percentage = view.findViewById(R.id.tv_percentage);
            name = view.findViewById(R.id.tv_user_full_name);
            time = view.findViewById(R.id.tv_time);
            percentageCard = view.findViewById(R.id.cv_percentage);
        }
    }

    public LeaderBoardAdapter(List<LeaderBoardEntry> entries) {
        this.entries = entries;
        this.colorUtils = new ColorUtils();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.leader_board_list_item, viewGroup, false);

        context = viewGroup.getContext();

        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LeaderBoardAdapter.ViewHolder viewHolder, int i) {
        final LeaderBoardEntry entry = entries.get(i);

        viewHolder.name.setText(entry.getReferencedUserFullName());
        viewHolder.percentage.setText(entry.getCompletionPercentage() + "%");
        viewHolder.placement.setText((i + 1) + "");
        viewHolder.time.setText(entry.getCompletionTime());
        viewHolder.percentageCard
                .setCardBackgroundColor(Color.parseColor(colorUtils.getRandomColor()));
    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }
}