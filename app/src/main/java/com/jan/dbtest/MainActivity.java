package com.jan.dbtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    // Insert part
    Context context = this;

    // Login
    private Button loginActivityButton;

    // SharedPreferences
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";

    private Button gotoAddToLesson;
    private Button gotoAttendanceCheck;
    private Button registerToCourse;
    private Button registration;
    private String deviceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        welcomeMessage = (TextView) findViewById(R.id.welcMessage);
        checkLogin();
        checkDeviceId();

        loginActivityButton = (Button) findViewById(R.id.loginActivityButton);
        loginActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("username", null);
                editor.putString("password", null);
                editor.putString("user_id", null);
                editor.apply();

                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });

        gotoAddToLesson =  (Button) findViewById(R.id.addToLessonActivity);
        gotoAddToLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, checkIn.class);
                context.startActivity(intent);
            }
        });

        gotoAttendanceCheck = (Button) findViewById(R.id.attendanceCheckButton);
        gotoAttendanceCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, attendanceCheck.class);
                context.startActivity(intent);
            }
        });

        registerToCourse = (Button) findViewById(R.id.registerToCourse);
        registerToCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, registerToCourse.class);
                context.startActivity(intent);
            }
        });
    }

    public void setWelcomeMessage(){
        welcomeMessage.setText("Welcome, " + prefs.getString("username", null));
    }

    public void checkLogin(){

        if(prefs.getString("user_id", null) == null){
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {
            Log.d("userData", "Username: " + prefs.getString("username", null) + " | Password: " + prefs.getString("password", null) + " | UserId: " + prefs.getString("user_id", null));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    public static String random(int length) {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        //int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < length; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static String random2(int length){

        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        //System.out.println(output);
        return output;
    }

    public void checkDeviceId(){

        // Device ID isn't assigned yet
        if(prefs.getString("deviceId", null) == null) {


            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // Permission issue
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                String id = prefs.getString("deviceId", null);
                if (id == null) {
                    deviceId = random2(15);
                    editor.putString("deviceId", deviceId);
                    editor.apply();
                }
            } else {            // No permision issue
                deviceId = tManager.getDeviceId();
                editor.putString("deviceId", deviceId);
                editor.apply();
            }
        }

        deviceId = prefs.getString("deviceId", deviceId);

        Log.d("deviceIdaaaa", "Device ID: " + deviceId);
    }
}