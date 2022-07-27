package com.my.company.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.company.instagramclone.adapter.UserAdapter;
import com.my.company.instagramclone.model.User;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    private String id;
    private String title;
    private List<String> idList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Toolbar toolbar = findViewById(R.id.follower_activity_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.follower_activity_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(this, mUsers, false);
        idList = new ArrayList<>();

        switch (title) {
            case "followers":
                getFollowers();
                break;

            case "followings":
                getFollowings();
                break;

            case "likes":
                getLikes();
                break;
        }
    }

    private void getLikes() {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snap : snapshot.getChildren())
                    idList.add(snap.getKey());

                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mUsers.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);

                            for (String id : idList) {
                                if (user.getId().equals(id))
                                    mUsers.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getFollowings() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(id).child("following").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idList.clear();
                        for (DataSnapshot snap : snapshot.getChildren())
                            idList.add(snap.getKey());

                        showUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getFollowers() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(id).child("followers").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idList.clear();
                        for (DataSnapshot snap : snapshot.getChildren())
                            idList.add(snap.getKey());

                        showUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }
}