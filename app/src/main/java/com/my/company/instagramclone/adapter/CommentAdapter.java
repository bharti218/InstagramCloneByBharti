package com.my.company.instagramclone.adapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.company.instagramclone.HomeActivity;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.model.Comment;
import com.my.company.instagramclone.model.User;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> mComments;
    private FirebaseUser fUser;
    private String postId;

    public CommentAdapter(Context context, List<Comment> mComments, String postId) {
        this.context = context;
        this.mComments = mComments;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.commment_item, parent, false);

        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = mComments.get(position);
        holder.comment.setText(comment.getComment());
        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        holder.username.setText(user.getUsername());
                        if (user.getImageurl().equals("default")) {
                            holder.imageProfile.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.imageProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, HomeActivity.class);
                        intent.putExtra("publisherId", comment.getPublisher());
                        context.startActivity(intent);
                    }
                }
        );

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(comment.getPublisher().endsWith(fUser.getUid())){
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle("Do you want to delete this comment?");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "no", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alertDialog.dismiss();
                                }
                            });
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).child(comment.getId()).removeValue().addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                                                        alertDialog.dismiss();
                                                    }
                                                }
                                            }
                                    );
                                }
                            });
                            alertDialog.show();

                        }
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imageProfile;
        public TextView username;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.comment_item_image_profile);
            username = itemView.findViewById(R.id.comment_item_username);
            comment = itemView.findViewById(R.id.comment_item_comment);
        }
    }
}
