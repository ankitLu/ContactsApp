package com.example.hi5an.inclass11;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class CreateContactActivity extends AppCompatActivity {

    String name, phone, email;
    User user;
    private StorageReference mStorageRef;
    String imageUrl;
    ImageView imageViewprofilePicture;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private int PICK_IMAGE_REQUEST = 1;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);
        user = MainActivity.validateSession(this);
        if (user==null){
            Intent intent = new Intent(CreateContactActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        final EditText editTextName = findViewById(R.id.editTextName);
        final EditText editTextPhone = findViewById(R.id.editTextPhone);
        final EditText editTextEmail = findViewById(R.id.editTextEmail);
        imageViewprofilePicture = (ImageView) findViewById(R.id.imageViewProfilePicture);
        imageViewprofilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateContactActivity.this);
                alertDialog.setTitle(R.string.confirm);
                alertDialog.setMessage("Do you want to use camera or upload from gallery?");
                alertDialog.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                });
                alertDialog.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dispatchTakePictureIntent();
                        Intent intent = new Intent();
                        // Show only images, no videos or anything else
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // Always show the chooser (if there are multiple options available)
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                });
                alertDialog.show();
            }
        });
        Button createButton = findViewById(R.id.buttonCreate);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFields())
                    return;
                Contact contact = new Contact();
                contact.setName(editTextName.getText().toString());
                contact.setPhone(editTextPhone.getText().toString());
                contact.setEmail(editTextEmail.getText().toString());
                String uniquekey = UUID.randomUUID().toString();
                uploadImage(imageViewprofilePicture, uniquekey, contact);
            }
        });
    }


    private boolean validateFields(){
        //imageViewPicture = (ImageView) findViewById(R.id.imageViewPic);
        name = ((EditText)findViewById(R.id.editTextName)).getText().toString().trim();
        phone = ((EditText)findViewById(R.id.editTextPhone)).getText().toString().trim();
        email = ((EditText)findViewById(R.id.editTextEmail)).getText().toString().trim();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        if (photo == null) {
            Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.equals("") || phone.equals("") || email.equals("") ){
            Toast.makeText(getBaseContext(), "Please complete missing fields!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void uploadImage(ImageView imageView, final String uniquekey, final Contact contact){
        // Get the data from an ImageView as bytes
        //StorageReference mountainImagesRef = mStorageRef.child(name+".jpg");
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        String path = "images/"+name+"_"+uniquekey+".jpeg";
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference storageReference =  mStorageRef.child(path);
        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(CreateContactActivity.this, "failed to upload image: "+exception
                        .getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageUrl = downloadUrl.toString();
                contact.setContactImage(imageUrl);
                contact.setContactKey(uniquekey);
                DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                String id = user.getUserKey();
                firebaseDatabase.child(id).child(MainActivity.CONTACT_DETAILS).child(uniquekey).setValue(contact);
                Toast.makeText(getBaseContext(), "contact saved successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateContactActivity.this, ContactsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try{
                Bundle extras = data.getExtras();
                photo = (Bitmap) extras.get("data");
                imageViewprofilePicture.setImageBitmap(photo);
            }catch (Exception ex){
                ex.printStackTrace();
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                        .show();
                Log.e("Camera", ex.toString());
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageViewprofilePicture.setImageBitmap(photo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
