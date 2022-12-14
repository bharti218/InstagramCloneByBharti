package com.my.company.instagramclone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.adapter.TagAdapter;
import com.my.company.instagramclone.adapter.UserAdapter;
import com.my.company.instagramclone.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private SocialAutoCompleteTextView searchBar;
    private List<User> mUsers;
    private UserAdapter userAdapter;

    private RecyclerView tagRecylerView;
    private List<String> mHashTags;
    private List<String> mHashTagCount;
    private TagAdapter tagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar = view.findViewById(R.id.search_bar);
        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(userAdapter);

        tagRecylerView = view.findViewById(R.id.recycler_view_tags);
        tagRecylerView.setHasFixedSize(true);
        tagRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mHashTags = new ArrayList<>();
        mHashTagCount = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext(), mHashTags, mHashTagCount);
        tagRecylerView.setAdapter(tagAdapter);

        readUsers();
        readTags();
        searchBar.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        searchUser(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        filter(editable.toString());
                    }
                }
        );
        return view;
    }

    private void readUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (TextUtils.isEmpty(searchBar.getText().toString())) {
                            mUsers.clear();
                            for (DataSnapshot snapShot : snapshot.getChildren()) {
                                User user = snapShot.getValue(User.class);
                                mUsers.add(user);
                                Log.i("FORLOOP", "Inside For Loop");
                                Log.i("MUSERS", String.valueOf(mUsers.size()));
                                Log.i("USER", user.getBio() + user.getId() + user.getEmail() + user.getUsername() + user.getImageurl());
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void searchUser(String searchText) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff");
        query.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mUsers.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            mUsers.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void readTags() {
        FirebaseDatabase.getInstance().getReference().child("hashtags").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mHashTags.clear();
                        mHashTagCount.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            mHashTags.add(s.getKey());
                            mHashTagCount.add(s.getChildrenCount() + "");
                        }
                        tagAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void filter(String text) {
        List<String> searchTags = new ArrayList<>();
        List<String> searchTagsCount = new ArrayList<>();

        for (String s : mHashTags) {

            if(s.toLowerCase().contains(text.toLowerCase())){
                searchTags.add(s);
                searchTagsCount.add(mHashTagCount.get(mHashTags.indexOf(s)));
            }
        }
        tagAdapter.filter(searchTags, searchTagsCount);
    }
}