package com.example.hi5an.inclass11;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;

public class UpdateContactActivity extends AppCompatActivity {

    String name, phone, email;
    User user;
    private StorageReference mStorageRef;
    String imageUrl;
    ImageView imageViewprofilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);
        if (user==null){
            Intent intent = new Intent(UpdateContactActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        imageViewprofilePicture = (ImageView) findViewById(R.id.imageViewProfilePicture);
        imageViewprofilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //invoke method to call camera.
            }
        });

        final EditText editTextName = findViewById(R.id.editTextName);
        final EditText editTextPhone = findViewById(R.id.editTextPhone);
        final EditText editTextEmail = findViewById(R.id.editTextEmail);
        imageViewprofilePicture = findViewById(R.id.imageView);
        Button createButton = findViewById(R.id.buttonCreate);
    }
}
