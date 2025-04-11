package com.example.commerzai;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    EditText usernameInput, passwordInput, emailInput;
    Button signupBtn;

    FirebaseAuth mAuth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        signupBtn = findViewById(R.id.signupBtn);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        signupBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                checkUsernameAndRegister(username, email, password);
            }
        });
    }

    private void checkUsernameAndRegister(String username, String email, String password) {
        userRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignUpActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(username, email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserDataToDatabase(user, username, email);
                            updateUI(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void saveUserDataToDatabase(FirebaseUser firebaseUser, String username, String email) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            HashMap<String, String> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);

            userRef.child(userId).setValue(userData)
                    .addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // You could redirect to ProfileActivity or MainActivity
            finish(); // for now, just close the signup screen
        } else {
            // Keep user on current screen, or show retry UI
        }
    }
}



