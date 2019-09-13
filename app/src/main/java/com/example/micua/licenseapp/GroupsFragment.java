package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class GroupsFragment extends Fragment {
    private RecyclerView groupList;
    private FloatingActionButton addGroup;

    private String title;
    private int page;
    private DBLinks dbLinks;
    private User currentUser;
    private List<Group> joinedGroups;
    private Activity parentActivity;
    private Group group;
    private GroupFragmentAdapter adapter;
    private ViewPager pager;
    private MainPagerAdapter pagerAdapter;
    private ArrayList<String> lastMessages;

    public static final String TAG = "GroupsFragment";

    public static GroupsFragment newInstance(int page, String title, Bundle bundle) {
        GroupsFragment groupsFragment = new GroupsFragment();
        groupsFragment.setArguments(bundle);
        return groupsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        joinedGroups = (List<Group>) getArguments().getSerializable("groups");
        lastMessages = getArguments().getStringArrayList("last_messages");

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                User user = (User) intent.getSerializableExtra("user");
                currentUser.setJoinedGroupsAccessTokens(user.getJoinedGroupsAccessTokens());
                String removedToken = intent.getStringExtra("removed_token");
                for (int i = 0; i < joinedGroups.size(); i++) {
                    if (joinedGroups.get(i).getAccessToken().equals(removedToken)) {
                        joinedGroups.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }, new IntentFilter("user-left-group"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        parentActivity = Objects.requireNonNull(getActivity());
        dbLinks = new DBLinks();
        groupList = view.findViewById(R.id.rv_groups);
        addGroup = view.findViewById(R.id.fab_add_group);

        pager = Objects.requireNonNull(getActivity()).findViewById(R.id.viewpager);
        pagerAdapter = (MainPagerAdapter) pager.getAdapter();

        currentUser = (User) parentActivity.getIntent().getSerializableExtra("currentUser");

        adapter = new GroupFragmentAdapter(joinedGroups, currentUser);
        groupList.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        groupList.setAdapter(adapter);
        groupList.setNestedScrollingEnabled(false);

        for (int i = 0; i < joinedGroups.size(); i++) {
            Log.d(TAG, joinedGroups.get(i).getGroupName());
        }

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Add New Group")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setMessage("If you want to add a new group press on the faculty" +
                                " you would like the group to be part of. Afterwards press the add button.");
                builder.create();
                builder.show();
            }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = (Message) intent.getSerializableExtra("message");
                for (int i = 0; i < joinedGroups.size(); i++) {
                    if (joinedGroups.get(i).getId().equals(message.getReferencedGroupId())) {
                        joinedGroups.get(i).setLastMessage(message);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        }, new IntentFilter("new-message"));

        return view;
    }

    protected void receiveMessage(Message message) {
        if (message != null) {
            Log.d(TAG, "receiveMessage: received message -> " + message.getMessage());
            for (int i = 0; i < joinedGroups.size(); i++) {
                if (message.getReferencedGroupId().equals(joinedGroups.get(i).getId())) {
                    joinedGroups.get(i).setLastMessage(message);
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }

    protected void receiveGroups(List<Group> groups) {
        Log.d(TAG, "receiveGroup: received groups -> " + groups.size());
        joinedGroups.addAll(groups);
        adapter.notifyDataSetChanged();
    }

}
