package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.Rating;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> reviews;
    private Context context;
    private int starDimen;
    private ColorUtils colorUtils;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView fullName, nameFirstLetter, rating, reviewText;
        private RatingBar ratingBar;
        private CardView firstLetterBackground;

        public ViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.tv_user_full_name);
            nameFirstLetter = view.findViewById(R.id.tv_name_first_letter);
            ratingBar = view.findViewById(R.id.rb_rating);
            rating = view.findViewById(R.id.tv_rating);
            firstLetterBackground = view.findViewById(R.id.cv_first_letter);
            reviewText = view.findViewById(R.id.tv_review);
        }
    }

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
        this.colorUtils = new ColorUtils();
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_list_item, viewGroup, false);

        context = viewGroup.getContext();


        return new ReviewAdapter.ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Review review = reviews.get(i);

        viewHolder.nameFirstLetter.setText(review.getUserFullName().toCharArray()[0] + "");
        viewHolder.fullName.setText(review.getUserFullName());
        viewHolder.rating.setText(review.getRating() + "");
        viewHolder.firstLetterBackground.setCardBackgroundColor(Color.parseColor(colorUtils.getRandomColor()));
        viewHolder.reviewText.setText(review.getReviewText());
        viewHolder.ratingBar.setEnabled(false);
        viewHolder.ratingBar.setNumStars(5);
        viewHolder.ratingBar.setMax(5);
        viewHolder.ratingBar.setStepSize(0.5f);
        viewHolder.ratingBar.setRating((float) review.getRating());
        viewHolder.ratingBar.invalidate();
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private int dpToPx(int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
