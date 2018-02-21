package com.example.hi5an.inclass11;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    User user;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        user = MainActivity.validateSession(this);
        if (user == null) {
            Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        ImageView imageView = findViewById(R.id.imageButtonAddUser);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), CreateContactActivity.class);
                startActivity(intent);
            }
        });

        ImageView editProfile = findViewById(R.id.imageEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), UpdateContactActivity.class);
                intent.putExtra("NAME", account.getDisplayName());
                intent.putExtra("Email", account.getEmail());
                intent.putExtra("Photo", account.getPhotoUrl());
                startActivity(intent);
            }
        });
        readDatabase();
    }


    public void readDatabase() {
        final ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(user.getUserKey()).child(MainActivity.CONTACT_DETAILS).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Contact person = child.getValue(Contact.class);
                            if (person != null)
                                contactArrayList.add(person);
                        }
                        if (!contactArrayList.isEmpty())
                            loadShowFriendsRecyclerView(contactArrayList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public void loadShowFriendsRecyclerView(ArrayList<Contact> contactArrayList) {
        mRecyclerView = (RecyclerView) findViewById(R.id.showContactsRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new ContactAdapter(contactArrayList, user);
        mRecyclerView.setAdapter(mAdapter);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if(account!=null){
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
                mGoogleSignInClient.revokeAccess();
            } else if (firebaseAuth.getCurrentUser()!=null) {
                firebaseAuth.signOut();
            } Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
            startActivity(intent);
        }catch (Exception e) {
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"Logout").setIcon(R.drawable.logout)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        getMenuInflater().inflate(R.menu.menu_dummy,menu);
        return super.onCreateOptionsMenu(menu);
    }

}
