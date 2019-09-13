package com.example.micua.licenseapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ReviewStarAdapter extends RecyclerView.Adapter<ReviewStarAdapter.ViewHolder> {
    private List<Double> values;
    private Context context;
    private int starDimen;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView star;

        public ViewHolder(View view) {
            super(view);
            star = view.findViewById(R.id.iv_star);
        }
    }

    public ReviewStarAdapter(List<Double> values, int starDimen) {
        this.values = values;
        this.starDimen = starDimen;
    }

    @NonNull
    @Override
    public ReviewStarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.star_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ReviewStarAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        double value = values.get(i);

        if (starDimen > 0) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.star.getLayoutParams();
            layoutParams.width = dpToPx(starDimen);
            layoutParams.height = dpToPx(starDimen);
            viewHolder.star.setLayoutParams(layoutParams);
        }

        if (value == 0.5)
            viewHolder.star.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.review_star_half));
        if (value == 1.0)
            viewHolder.star.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.review_star_full));
        if (value == 0)
            viewHolder.star.setImageDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));

    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    private int dpToPx(int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
