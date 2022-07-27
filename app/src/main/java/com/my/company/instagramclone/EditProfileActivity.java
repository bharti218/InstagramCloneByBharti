package com.my.company.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.my.company.instagramclone.model.User;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView closeIcon;
    private CircleImageView profileImage;
    private TextView save;
    private TextView changePicture;
    private MaterialEditText fullname;
    private MaterialEditText username;
    private MaterialEditText bio;

    private FirebaseUser fUser;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private int PICK_IMAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initMembers();
        setViewFromDatabase();
    }

    private void initMembers() {
        closeIcon = findViewById(R.id.edit_profile_activity_close);
        profileImage = findViewById(R.id.edit_profile_activity_profile_image);
        save = findViewById(R.id.edit_profile_activity_save);
        changePicture = findViewById(R.id.edit_profile_activity_change_picture);
        fullname = findViewById(R.id.edit_profile_activity_fullname);
        username = findViewById(R.id.edit_profile_activity_username);
        bio = findViewById(R.id.edit_profile_activity_bio);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        changePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePicture();

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePicture();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    private void updatePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE);
    }

    private void updateProfile() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", fullname.getText().toString());
        map.put("username", username.getText().toString());
        map.put("bio", bio.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).updateChildren(map);
        Toast.makeText(this, "details updated!", Toast.LENGTH_SHORT).show();

    }

    private void setViewFromDatabase() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        fullname.setText(user.getName());
                        username.setText(user.getUsername());
                        bio.setText(user.getBio());
                        Picasso.get().load(user.getImageurl()).into(profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        imageUri = data.getData();
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//            imgAdded.setImageBitmap(bitmap);
//            Toast.makeText(this, "imageadded", Toast.LENGTH_SHORT).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImage();
                Toast.makeText(this, "imageadded", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(EditProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpeg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(fUser.getUid()).child("imageurl").setValue(url);
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}