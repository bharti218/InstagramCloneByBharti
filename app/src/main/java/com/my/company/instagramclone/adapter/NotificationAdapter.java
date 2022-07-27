package com.my.company.instagramclone.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.fragments.PostDetailFragment;
import com.my.company.instagramclone.fragments.ProfileFragment;
import com.my.company.instagramclone.model.Notification;
import com.my.company.instagramclone.model.Post;
import com.my.company.instagramclone.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
        Log.i("mNotifications", "size =" + mNotifications.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);


        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = mNotifications.get(position);
        getUser(holder.profileImg, holder.username, notification.getPublisherId());
        if (!notification.getPostId().equals("")) {
            Log.i("notification.isPost()", "true");
            holder.postImg.setVisibility(View.VISIBLE);
            getPostImage(holder.postImg, notification.getPostId());
        } else {
            holder.postImg.setVisibility(View.GONE);
            Log.i("notification.isPost()", "false");

        }

        holder.comment.setText(notification.getText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!notification.getPostId().equals("")) {
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                            .edit().putString("postId", notification.getPostId()).apply();
                    ((FragmentActivity) mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();
                } else {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                            .edit().putString("profileId", notification.getPublisherId()).apply();
                    ((FragmentActivity) mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
            }
        });

    }

    private void getPostImage(ImageView imageView, String postId) {
        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        Picasso.get().load(post.getImageurl()).into(imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getUser(ImageView imageView, TextView textView, String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImageurl().equals("default")) {
                            imageView.setImageResource(R.drawable.profile);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(imageView);
                        }
                        textView.setText(user.getUsername());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImg;
        public ImageView postImg;
        public TextView username;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.notification_item_image_profile);
            postImg = itemView.findViewById(R.id.notification_item_post_img);
            username = itemView.findViewById(R.id.notification_item_username);
            comment = itemView.findViewById(R.id.notification_item_comment);

        }
    }
}
