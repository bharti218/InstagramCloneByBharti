package com.my.company.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private ImageView close;
    private ImageView imgAdded;
    private TextView post;
    SocialAutoCompleteTextView description;
    private Uri imageUri;
    private String imageUrl;
    private int PICK_IMAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initMembers();
    }

    private void initMembers() {
        close = findViewById(R.id.close);
        imgAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(PostActivity.this, HomeActivity.class));
                        finish();
                    }
                });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUpload();
            }
        });

//        CropImage.activity().start(PostActivity.this);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE);
    }

    private void imageUpload() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference("posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            StorageTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                        throw task.getException();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(
                    new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
                            String postId = ref.push().getKey();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("postId", postId);
                            map.put("imageurl", imageUrl);
                            map.put("description", description.getText().toString());
                            map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            ref.child(postId).setValue(map);

                            DatabaseReference hashtagRef = FirebaseDatabase.getInstance().getReference().child("hashtags");
                            List<String> hashtags = description.getHashtags();
                            if (!hashtags.isEmpty()) {
                                for (String tag : hashtags) {
                                    map.clear();
                                    map.put("tag", tag.toLowerCase());
                                    map.put("postid", postId);

                                    hashtagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                                }
                            }
                            progressDialog.dismiss();
                            startActivity(new Intent(PostActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
            ).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        } else {
            Toast.makeText(this, "Please select an image to proceed", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            imageUri = result.getUri();
//            imgAdded.setImageURI(imageUri);
//        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgAdded.setImageBitmap(bitmap);
                Toast.makeText(this, "imageadded", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayAdapter<Hashtag> hashtagArrayAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("hashtags").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            hashtagArrayAdapter.add(new Hashtag(snap.getKey(), (int) snap.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
        description.setHashtagAdapter(hashtagArrayAdapter);
    }

}