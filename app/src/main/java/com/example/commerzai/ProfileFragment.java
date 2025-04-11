package com.example.commerzai;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class ProfileFragment extends Fragment {

    private TextView userName, userEmail, authAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        authAction = view.findViewById(R.id.logout); // reuse the logout TextView

        TextView editProfile = view.findViewById(R.id.editProfile);
        editProfile.setOnClickListener(v -> showEditProfileDialog());


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Logged in - fetch user data from Firebase
            userEmail.setText(currentUser.getEmail());
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    userName.setText(username != null ? username : "User");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    userName.setText("User");
                }
            });

            authAction.setText("Logout");
            authAction.setTextColor(Color.RED);
            authAction.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                getActivity().recreate();
            });
        } else {
            // Not logged in
            userName.setText("Guest");
            userEmail.setText("");
            authAction.setText("Login");
            authAction.setTextColor(Color.BLUE);
            authAction.setOnClickListener(v -> showLoginDialog());
        }

        return view;
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_login, null);
        builder.setView(dialogView);

        EditText emailInput = dialogView.findViewById(R.id.usernameInput); // actually email now
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        TextView signUpLink = dialogView.findViewById(R.id.signUpLink);
        Button loginBtn = dialogView.findViewById(R.id.loginBtn);

        AlertDialog dialog = builder.create();

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            getActivity().recreate();
                        } else {
                            Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        signUpLink.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(getContext(), SignUpActivity.class));
        });

        dialog.show();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText newUsernameInput = dialogView.findViewById(R.id.newUsernameInput);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        Button updateBtn = dialogView.findViewById(R.id.updateBtn);

        updateBtn.setOnClickListener(v -> {
            String newUsername = newUsernameInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

            // Check if new username exists (excluding current user)
            if (!newUsername.isEmpty()) {
                userRef.orderByChild("username").equalTo(newUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean usernameTaken = false;
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    if (!userSnap.getKey().equals(user.getUid())) {
                                        usernameTaken = true;
                                        break;
                                    }
                                }

                                if (usernameTaken) {
                                    Toast.makeText(getContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                                } else {
                                    // ✅ Update username
                                    userRef.child(user.getUid()).child("username").setValue(newUsername);

                                    // ✅ Update password if needed
                                    if (!newPassword.isEmpty()) {
                                        user.updatePassword(newPassword)
                                                .addOnCompleteListener(task -> {
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Password update failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    getActivity().recreate();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Database error", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // If username is empty, skip checking and update password only
                if (!newPassword.isEmpty()) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Password update failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                getActivity().recreate();
            }
        });


        dialog.show();
    }

}

