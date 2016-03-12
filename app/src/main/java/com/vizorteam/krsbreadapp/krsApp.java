package com.vizorteam.krsbreadapp;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by ziyad on 3/11/16.
 */
public class krsApp extends Application {
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Initialize firebase
        initFirebase();
    }

    protected void initFirebase() {
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
