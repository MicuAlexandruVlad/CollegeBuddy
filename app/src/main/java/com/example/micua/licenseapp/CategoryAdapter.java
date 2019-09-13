package com.example.micua.licenseapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<String> categories;
    private Context context;
    private ColorUtils colorUtils;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryName;
        private CardView categoryBackground;

        public ViewHolder(View view) {
            super(view);
            this.categoryName = view.findViewById(R.id.tv_category_name);
            this.categoryBackground = view.findViewById(R.id.cv_category_color);
        }
    }

    public CategoryAdapter(List<String> categories) {
        this.categories = categories;
        this.colorUtils = new ColorUtils();
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.category_list_item, viewGroup, false);

        context = viewGroup.getContext();

        return new CategoryAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final String category = categories.get(i);

        viewHolder.categoryName.setText(category);
        viewHolder.categoryBackground.setCardBackgroundColor(
                Color.parseColor(colorUtils.getRandomColor()));

        viewHolder.categoryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddQuizCategoryDialog addQuizCategoryDialog =
                        new AddQuizCategoryDialog(context);
                addQuizCategoryDialog.setCategoryName(category);
                addQuizCategoryDialog.setEdit(true);
                addQuizCategoryDialog.create();
                addQuizCategoryDialog.show();

                addQuizCategoryDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (addQuizCategoryDialog.isAddOrEditPressed())
                            viewHolder.categoryName.setText(addQuizCategoryDialog.getCategoryName());
                        else {
                            categories.remove(i);
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
