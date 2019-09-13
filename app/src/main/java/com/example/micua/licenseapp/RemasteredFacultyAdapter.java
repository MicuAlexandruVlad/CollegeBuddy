package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

//TODO: update ui button after joining faculty

public class RemasteredFacultyAdapter extends RecyclerView.Adapter<RemasteredFacultyAdapter.ViewHolder> {
    private List<Faculty> faculties;
    private Context context;
    private User currentUser;
    private College currentCollege;
    private DBLinks dbLinks;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, referencedCollege, numMembers, numGroups, description, firstLetter;
        CardView firstLetterBackground;
        ConstraintLayout body;

        public ViewHolder(View view) {
            super(view);
            firstLetter = view.findViewById(R.id.tv_faculty_name_first_letter);
            name = view.findViewById(R.id.tv_faculty_name);
            referencedCollege = view.findViewById(R.id.tv_referenced_college);
            description = view.findViewById(R.id.tv_faculty_description);
            numGroups = view.findViewById(R.id.tv_num_groups);
            numMembers = view.findViewById(R.id.tv_num_members);
            firstLetterBackground = view.findViewById(R.id.cv_faculty_first_letter);
            body = view.findViewById(R.id.cl_body);
        }
    }

    public RemasteredFacultyAdapter(List<Faculty> faculties, User user, Context context) {
        this.faculties = faculties;
        this.currentUser = user;
        this.dbLinks = new DBLinks();
        this.context = context;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final Faculty faculty = faculties.get(i);

        viewHolder.name.setText(faculty.getFacultyName());
        if (faculty.getFacultyDescription().equals(""))
            viewHolder.description.setText("No description available");
        else
            viewHolder.description.setText(faculty.getFacultyDescription());
        viewHolder.referencedCollege.setText(faculty.getReferencedCollegeName());
        viewHolder.numMembers.setText(faculty.getNumMembers() + "");
        viewHolder.numGroups.setText(faculty.getNumGroups() + "");
        viewHolder.firstLetter.setText(faculty.getFacultyName().toCharArray()[0] + "");

        final int pos = i;

        viewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, JoinGroupActivity.class);
                intent.putExtra("currentFaculty", faculty);
                intent.putExtra("facultyPosition", pos);
                intent.putExtra("currentUser", currentUser);
                ((Activity) context).startActivityForResult(intent, 222);
            }
        });

    }



    @Override
    public int getItemCount() {
        return faculties.size();
    }
}
