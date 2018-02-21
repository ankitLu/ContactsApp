package com.example.hi5an.inclass11;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    DatabaseReference firebaseDatabase;
    String username, firstName, lastName, password, confirmPass, mCurrentPhotoPath;
    ImageView imageViewPicture;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    FirebaseAuth firebaseAuth;
    private int PICK_IMAGE_REQUEST = 1;
    private StorageReference mStorageRef;
    String imageUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        findViewById(R.id.buttonSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                if (!validateFields()){
                    return;
                }
                firebaseAuth.createUserWithEmailAndPassword(username,password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    String id = task.getResult().getUser().getUid();
                                    User user = new User();
                                    // set user object
                                    user.setFirstName(firstName);
                                    user.setLastName(lastName);
                                    user.setUsername(username);
                                    user.setUserKey(id);
                                    uploadImage(imageViewPicture,  "_" +firstName,user );
                                    Toast.makeText(SignUpActivity.this, "Uploading....", Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(getBaseContext(), "Failed to create user: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });
        imageViewPicture = (ImageView) findViewById(R.id.imageViewPic);
        imageViewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dispatchTakePictureIntent();
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }



    private boolean validateFields(){
        imageViewPicture = (ImageView) findViewById(R.id.imageViewPic);
        username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString().trim();
        firstName = ((EditText)findViewById(R.id.editTextFirstName)).getText().toString().trim();
        lastName = ((EditText)findViewById(R.id.editTextLastName)).getText().toString().trim();
        password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString().trim();
        confirmPass = ((EditText)findViewById(R.id.editTextPasswordConfirm)).getText().toString().trim();
        if(username.equals("") || firstName.equals("") || lastName.equals("") || password.equals("")){
            Toast.makeText(getBaseContext(), "Please complete missing fields!", Toast.LENGTH_SHORT).show();
            return false;
        }  if (!(password.length() >= 8)){
            Toast.makeText(getBaseContext(), "Password should be minimum 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        } if (!password.equalsIgnoreCase(confirmPass)) {
            Toast.makeText(getBaseContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewPicture.setImageBitmap(imageBitmap);
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                imageViewPicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }



    public void uploadImage(ImageView imageView, String name, final User user){
        // Get the data from an ImageView as bytes
        //StorageReference mountainImagesRef = mStorageRef.child(name+".jpg");
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        String path = "images/"+name+"_"+user.getUserKey()+".jpeg";
        StorageReference storageReference =  mStorageRef.child(path);
        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(SignUpActivity.this, "failed to upload image: "+exception
                        .getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageUrl = downloadUrl.toString();
                user.setImagePath(imageUrl);
                // save user object
                firebaseDatabase.child(user.getUserKey()).child(MainActivity.USER_DETAILS).setValue(user);
                Toast.makeText(getBaseContext(), "user created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
