package com.my.company.instagramclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.my.company.instagramclone.HomeActivity;
import com.my.company.instagramclone.MainActivity;
import com.my.company.instagramclone.R;
import com.my.company.instagramclone.fragments.ProfileFragment;
import com.my.company.instagramclone.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;
    private boolean isFragment;

    private FirebaseUser firebaseUser;


    public UserAdapter(Context mContext, List<User> mUser, boolean isFragment) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = mUser.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getName());

        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.profile).into(holder.profileImage);
        isFollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid()))
            holder.btnFollow.setVisibility(View.GONE);

        holder.btnFollow.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.btnFollow.getText().toString().equals("Follow")) {

                            FirebaseDatabase.getInstance().getReference().child("Follow").child(
                                    (firebaseUser.getUid())).child("following").child(user.getId()).setValue(true);

                            FirebaseDatabase.getInstance().getReference().child("Follow")
                                    .child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);

                            sendNotification(user.getId());
                        } else {

                            FirebaseDatabase.getInstance().getReference().child("Follow").child(
                                    (firebaseUser.getUid())).child("following").child(user.getId()).removeValue();

                            FirebaseDatabase.getInstance().getReference().child("Follow")
                                    .child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
                        }
                    }
                }
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFragment){
                    mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }else{
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    intent.putExtra("publisherId", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void sendNotification(String id) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("publisherId", firebaseUser.getUid());
        map.put("text", "started following you");
        map.put("isPost", false);
        map.put("postId", "");

        FirebaseDatabase.getInstance().getReference().child("Notification").child(id).push().setValue(map);
    }

    private void isFollowed(String id, Button btnFollow) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists())
                    btnFollow.setText("Following");
                else
                    btnFollow.setText("Follow");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage;
        public TextView username;
        public TextView fullname;
        public Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.user_item_username);
            fullname = itemView.findViewById(R.id.user_item_full_name);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
