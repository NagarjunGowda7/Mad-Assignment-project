package com.example.assignmentmad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
public class WelcomeActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnChangePassword, btnGenerateReport;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        mAuth = FirebaseUtils.getAuthInstance();
        mDatabase = FirebaseUtils.getDatabaseReference().child("users");

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in
                    retrieveUserInformation(currentUser);
                } else {
                    // User is not signed in
                    Toast.makeText(WelcomeActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    // Redirect to login or registration activity
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, ChangePasswordActivity.class));
            }
        });

        btnGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, ReportGenerationActivity.class));
            }
        });
    }

    private void retrieveUserInformation(FirebaseUser currentUser) {
        String userId = currentUser.getUid();
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        tvWelcome.setText("Welcome, " +user.getName());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            retrieveUserInformation(currentUser);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Clean up resources, if needed
    }
}