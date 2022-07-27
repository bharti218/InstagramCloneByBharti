package com.my.company.instagramclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.my.company.instagramclone.CommentActivity;
import com.my.company.instagramclone.FollowersActivity;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.fragments.PostDetailFragment;
import com.my.company.instagramclone.fragments.ProfileFragment;
import com.my.company.instagramclone.model.Post;
import com.my.company.instagramclone.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;
    private FirebaseUser firebaseUser;


    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);

        Picasso.get().load(post.getImageurl()).into(holder.postImage);
        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImageurl().equals("default")) {
                            holder.imageProfile.setImageResource(R.drawable.profile);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                        }
                        holder.username.setText(user.getUsername());
                        holder.author.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

        isLiked(post.getPostId(), holder.like);
        numOfLikes(post.getPostId(), holder.numOfLikes);
        getComments(post.getPostId(), holder.numOfComments);
        isSaved(post.getPostId(), holder.save);
        holder.like.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.like.getTag().equals("like")) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Likes").child(post.getPostId())
                                    .child(firebaseUser.getUid()).setValue(true);

                            notifyPublisher(post.getPostId(), post.getPublisher());
                        } else {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Likes").child(post.getPostId())
                                    .child(firebaseUser.getUid()).removeValue();
                        }
                    }
                }

        );

        holder.comment.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startIntentForCommentActivity(post);
                    }
                }
        );

        holder.numOfComments.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startIntentForCommentActivity(post);
                    }
                }
        );
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves")
                            .child(firebaseUser.getUid()).child(post.getPostId()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves")
                            .child(firebaseUser.getUid()).child(post.getPostId()).removeValue();
                }
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProfileFragment(post);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProfileFragment(post);
            }
        });

        holder.author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProfileFragment(post);
            }
        });

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postId", post.getPostId()).apply();
                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
        });

        holder.numOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPublisher());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });
    }

    private void notifyPublisher (String postId, String publisherId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notification").child(firebaseUser.getUid());
        if(publisherId.equals(firebaseUser.getUid())) return;
        if (ref.child(postId)!=null) return;
        HashMap<String, Object> map = new HashMap<>();
        map.put("publisherId", publisherId);
        map.put("text", "liked you post");
        map.put("isPost", true);
        map.put("postId", postId);

        ref.push().setValue(map);
    }

    private void setProfileFragment(Post post) {
        mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                .edit().putString("profileId", post.getPublisher()).apply();
        ((FragmentActivity) mContext).getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
    }

    private void isSaved(String postId, ImageView save) {
        FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(postId).exists()) {
                            save.setImageResource(R.drawable.ic_bookmark_solid);
                            save.setTag("saved");
                        } else {
                            save.setImageResource(R.drawable.ic_bookmark_hollow);
                            save.setTag("save");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void getComments(String postId, TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        text.setText("View All " + snapshot.getChildrenCount() + " Comments");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void startIntentForCommentActivity(Post post) {
        Intent intent = new Intent(mContext, CommentActivity.class);
        intent.putExtra("postId", post.getPostId());
        intent.putExtra("authorId", post.getPublisher());
        mContext.startActivity(intent);
    }

    private void isLiked(String postId, ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(firebaseUser.getUid()).exists()) {
                            imageView.setImageResource(R.drawable.ic_heart_solid_pink);
                            imageView.setTag("liked");
                        } else {
                            imageView.setImageResource(R.drawable.ic_heart_hollow);
                            imageView.setTag("like");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void numOfLikes(String postId, TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        text.setText(snapshot.getChildrenCount() + " likes");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageProfile;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more;

        public TextView username;
        public TextView numOfLikes;
        public TextView author;
        public TextView numOfComments;
        SocialTextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.post_item_profile_image);
            postImage = itemView.findViewById(R.id.post_item_image);
            like = itemView.findViewById(R.id.post_item_like);
            comment = itemView.findViewById(R.id.post_item_comment);
            save = itemView.findViewById(R.id.post_item_save);
            more = itemView.findViewById(R.id.post_item_more);

            username = itemView.findViewById(R.id.post_item_username);
            numOfLikes = itemView.findViewById(R.id.post_item_no_of_like);
            author = itemView.findViewById(R.id.post_item_author);
            numOfComments = itemView.findViewById(R.id.post_item_no_of_comments);
            description = itemView.findViewById(R.id.post_item_description);

        }
    }
}
