package com.jan.dbtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        welcomeMessage = (TextView) findViewById(R.id.welcMessage);
        checkLogin();
        //setWelcomeMessage();

        //textView1 = (TextView)findViewById(R.id.textView1);

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
}