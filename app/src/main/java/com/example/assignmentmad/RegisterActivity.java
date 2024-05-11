package com.example.assignmentmad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etName, etDateOfBirth, etAddress;
    private Spinner spGender;
    private TextView login;
    private Button btnUploadPhoto, btnRegister;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri profilePhotoUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        login = findViewById(R.id.logintxt);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        spGender = findViewById(R.id.spGender);
        etAddress = findViewById(R.id.etAddress);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnRegister = findViewById(R.id.btnRegister);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openFileChooser();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profilePhotoUri = data.getData();
            // You can display the selected image in an ImageView if needed
        }
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String gender = spGender.getSelectedItem().toString();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(dateOfBirth) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnSuccessListener(authResult -> {
                    // User creation successful
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String userId = user.getUid();

                        // Upload profile photo to Firebase Storage
                        if (profilePhotoUri != null) {
                            StorageReference photoRef = mStorage.child("profile_photos/" + userId + ".jpg");
                            photoRef.putFile(profilePhotoUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String profilePhotoUrl = uri.toString();
                                                            saveUserToDatabase(userId, username, password, name, dateOfBirth, gender, address, profilePhotoUrl);
                                                        }
                                                    });
                                        }
                                    });
                        } else {
                            saveUserToDatabase(userId, username, password, name, dateOfBirth, gender, address, null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // User creation failed
                    Log.e("RegisterActivity", "Error creating user: " + e.getMessage());
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void saveUserToDatabase(String userId, String username, String password, String name, String dateOfBirth, String gender, String address, String profilePhotoUrl) {
        User user = new User(username, password, name, dateOfBirth, gender, address, profilePhotoUrl);
        mDatabase.child(userId).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("RegisterActivity", "User data saved successfully");
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RegisterActivity", "Error saving user data: " + e.getMessage());
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}