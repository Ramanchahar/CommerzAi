package com.example.commerzai;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CommerzAIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
} 