package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.Firebase;

/**
 * Created by ziyad on 3/11/16.
 */
public class helpers {
    private static Firebase krsRef;
    private static String routeString;

    public static final String DEVICE_CONFIG = "DeviceConfig";
    public static final String portName = "BT:KRS Bread A";
    public static final String portSettings = "portable;escpos";


    public static void setFirebase() {
        krsRef = new Firebase("https://krs-bread-app.firebaseio.com/");
        krsRef.keepSynced(true);
    }

    public static Firebase getFirebase() {return krsRef;}

    public static String setRouteString(Context context, String newValue) {
        SharedPreferences settings = context.getSharedPreferences(DEVICE_CONFIG, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("route", newValue);
        // Commit the edits!
        editor.commit();
        return newValue;
    }

    public static String getRouteString(Context context) {
        SharedPreferences settings = context.getSharedPreferences(DEVICE_CONFIG, 0);
        String routeString = settings.getString("route", "");
        return routeString;
    }
}
