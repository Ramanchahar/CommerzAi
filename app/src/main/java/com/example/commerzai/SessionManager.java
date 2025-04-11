package com.example.commerzai;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {

    /**
     * Check if user is currently logged in.
     */
    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Get current user ID.
     */
    public static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Get current user's email.
     */
    public static String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Log the user out and optionally navigate to a specific activity.
     */
    public static void logout(Context context, Class<?> nextActivity) {
        FirebaseAuth.getInstance().signOut();
        if (nextActivity != null) {
            Intent intent = new Intent(context, nextActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}
