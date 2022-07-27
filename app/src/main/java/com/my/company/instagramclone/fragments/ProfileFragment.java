package com.my.company.instagramclone.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.company.instagramclone.EditProfileActivity;
import com.my.company.instagramclone.FollowersActivity;
import com.my.company.instagramclone.OptionsActivity;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.adapter.PhotoAdapter;
import com.my.company.instagramclone.model.Post;
import com.my.company.instagramclone.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerViewSaves;
    private PhotoAdapter photoAdapterSaves;
    private List<Post> mySavedPosts;

    private RecyclerView recyclerViewMyPhotos;
    private PhotoAdapter photoAdapterMyPhots;
    private List<Post> myPhotoList;

    private CircleImageView profileImg;
    private ImageView options;
    private TextView posts;
    private TextView followers;
    private TextView following;
    private TextView fullname;
    private TextView bio;
    private TextView username;

    private ImageView myPictures;
    private ImageView savedPictures;

    private FirebaseUser fUser;

    private String profileId;
    private Button editProfile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");
        if (data.equals("none")) {
            profileId = fUser.getUid();
        } else {
            profileId = data;
        }
        profileId = fUser.getUid();

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImg = view.findViewById(R.id.fragment_profile_image);
        options = view.findViewById(R.id.fragment_profile_option);
        posts = view.findViewById(R.id.fragment_profile_posts);
        followers = view.findViewById(R.id.fragment_profile_followers);
        following = view.findViewById(R.id.fragment_profile_following);
        fullname = view.findViewById(R.id.fragment_profile_fullname);
        bio = view.findViewById(R.id.fragment_container_bio);
        username = view.findViewById(R.id.fragment_profile_username);
        myPictures = view.findViewById(R.id.fragment_profile_my_pictures);
        savedPictures = view.findViewById(R.id.fragment_profile_saved_pictures);
        editProfile = view.findViewById(R.id.fragment_profile_edit_profile);

        recyclerViewMyPhotos = view.findViewById(R.id.fragment_profile_recycler_view_pics);
        recyclerViewMyPhotos.setHasFixedSize(true);
        recyclerViewMyPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList = new ArrayList<>();
        photoAdapterMyPhots = new PhotoAdapter(getContext(), myPhotoList);
        recyclerViewMyPhotos.setAdapter(photoAdapterMyPhots);

        recyclerViewSaves = view.findViewById(R.id.fragment_profile_recycler_view_saved);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mySavedPosts = new ArrayList<>();
        photoAdapterSaves = new PhotoAdapter(getContext(), mySavedPosts);
        recyclerViewSaves.setAdapter(photoAdapterSaves);

        getMyPhotos();
        userInfo();
        setFollowingAndFollowingCount();
        getPostCount();
        getSavedPosts();

        if (profileId.equals(fUser.getUid())) {
            editProfile.setText("Edit Profile");
        } else {
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String btnText = editProfile.getText().toString();
                        if (btnText.equals("Edit Profile")) {
                            //todo edit profile
                            startActivity(new Intent(getContext(), EditProfileActivity.class));
                        } else {
                            if (btnText.equals("follow")) {
                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(fUser.getUid()).child("following").child(profileId).setValue(true);

                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(profileId).child("followers").child(fUser.getUid()).setValue(true);
                            } else {
                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(fUser.getUid()).child("following").child(profileId).removeValue();

                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(profileId).child("followers").child(fUser.getUid()).removeValue();

                            }
                        }
                    }
                }
        );

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewMyPhotos.setVisibility(View.VISIBLE);
                recyclerViewSaves.setVisibility(View.GONE);
            }
        });

        savedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewMyPhotos.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        });

        recyclerViewMyPhotos.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });

        return view;
    }

    private void getSavedPosts() {
        List<String> savedIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            savedIds.add(snap.getKey());
                        }

                        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        mySavedPosts.clear();
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            Post post = snapshot1.getValue(Post.class);
                                            for (String id : savedIds) {
                                                if (post.getPostId().equals(id)) {
                                                    mySavedPosts.add(post);
                                                }
                                            }
                                        }
                                        photoAdapterSaves.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getMyPhotos() {
        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myPhotoList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Post post = snap.getValue(Post.class);

                            if (post.getPublisher().equals(profileId)) {
                                myPhotoList.add(post);
                            }
                        }
                        Collections.reverse(myPhotoList);
                        photoAdapterMyPhots.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void checkFollowingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(profileId).exists()) {
                            editProfile.setText("Following");
                        } else {
                            editProfile.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getPostCount() {
        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Post post = snap.getValue(Post.class);
                            if (post.getPublisher().equals(profileId))
                                counter++;
                        }
                        posts.setText(String.valueOf(counter));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void setFollowingAndFollowingCount() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followers.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

        ref.child("followers").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        following.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void userInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        if (user.getImageurl().toString().equals("default")) {
                            profileImg.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(profileImg);
                        }
                        username.setText(user.getUsername());
                        fullname.setText(user.getName());
                        bio.setText(user.getBio());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }


}