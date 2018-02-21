package com.example.hi5an.inclass11;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.example.hi5an.inclass11.SignUpActivity.REQUEST_IMAGE_CAPTURE;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 101;
    static final String USER_DETAILS = "UserDetails";
    String username, password;
    static final String USERNAME_KEY = "username";
    static final String CONTACT_DETAILS = "contactDetails";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){
            goToHomeScreen(account.getDisplayName());
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
       findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = ((EditText) findViewById(R.id.editTextUsername)).getText().toString().trim();
                password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString().trim();
                if (username.equals("") || password.equals("")){
                    Toast.makeText(getBaseContext(), "Please complete sign in info", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(username,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getBaseContext(), "Welcome user", Toast.LENGTH_SHORT).show();
                                    goToHomeScreen(username);
                                } else {
                                    Toast.makeText(getBaseContext(), "Login failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //updateUI(account);
        findViewById(R.id.textViewSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    public void goToHomeScreen(String username){
        Intent intent = new Intent(this,ContactsActivity.class);
        intent.putExtra(USERNAME_KEY, username);
        startActivity(intent);
    }

    public static User validateSession(Context context) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            User user = new User();
            user.setUsername(account.getEmail());
            user.setUserKey(account.getId());
            user.setFirstName(account.getGivenName());
            user.setLastName(account.getFamilyName());
            user.setStatus("google");
            user.setDateOfBirth("");
            return user;
        } else if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            // get user details from firebase
            User user = new User();
            if (user==null || user.getUsername() == null || user.getUsername().equals("")){
                user.setUsername(firebaseAuth.getCurrentUser().getEmail());
                user.setUserKey(firebaseAuth.getCurrentUser().getUid());
                user.setFirstName(firebaseAuth.getCurrentUser().getDisplayName());
                user.setLastName("");
                user.setUserKey(firebaseAuth.getCurrentUser().getUid());
            }
            return user;
        } else{
            Toast.makeText(context, "Invalid session, please login!", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            goToHomeScreen(account.getDisplayName().toString());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

}
