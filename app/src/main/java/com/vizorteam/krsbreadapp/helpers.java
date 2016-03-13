package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.client.Firebase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

    public static void writeData (Context context, String data ) {
        try {
            FileOutputStream fOut = context.openFileOutput("KRS_DATA.dat", context.MODE_PRIVATE) ;
            OutputStreamWriter osw = new OutputStreamWriter ( fOut ) ;
            osw.write ( data ) ;
            osw.flush ( ) ;
            osw.close ( ) ;
        } catch ( Exception e ) {
            e.printStackTrace ( ) ;
        }
    }

    public static String readSavedData (Context context) {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = context.openFileInput("KRS_DATA.dat") ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return datax.toString() ;
    }
}
