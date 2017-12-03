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

    // Select part
    private JSONArray result;
    private TextView textView1;
    private Button buttonGet;
    boolean log = false;

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
        setContentView(R.layout.activity_main);


        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        welcomeMessage = (TextView) findViewById(R.id.welcMessage);
        checkLogin();
        setWelcomeMessage();

        textView1 = (TextView)findViewById(R.id.textView1);
        buttonGet = (Button)findViewById(R.id.getContentButton);
        buttonGet.setOnClickListener(getDataClicked);

        loginActivityButton = (Button) findViewById(R.id.loginActivityButton);
        loginActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        registration = (Button) findViewById(R.id.registration);
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, registration.class);
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

    View.OnClickListener getDataClicked = new View.OnClickListener(){

        @Override
        public void onClick(View v){
            getData();
        }
    };

    private void getData(){
        if(log) Log.d("debug", "HERE 2");
        StringRequest stringRequest = new StringRequest("https://attendance-system-server-js5898.c9users.io/select_data.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j;

                        if(log) Log.d("debug", "HERE 3: Response: " + response);
                        try {
                            if(log) Log.d("debug", "HERE 4 - TRY?");
                            j = new JSONObject(response);
                            result = j.getJSONArray(com.jan.dbtest.JSONSupportClass.JSON_ARRAY);
                            Log.d("ServerResponse", result.toString());
                            parseJSON(result);
                        } catch (JSONException e) {
                            if(log) Log.d("debug", "HERE 4 - Exception?");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(log) Log.d("debug", "On Error Listener: " + error);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void parseJSON(JSONArray j){
        if(log) Log.d("debug", "Here, being called");
        textView1.setText("");
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.JSONSupportClass.TAG_ID));
                if(log)  Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.JSONSupportClass.TAG_ID));

                textView1.append(
                        json.getString(JSONSupportClass.TAG_ID) + "\n" +
                        json.getString(JSONSupportClass.TAG_NAME) + "\n" +
                        json.getString(JSONSupportClass.TAG_EMAIL) + "\n" +
                        json.getString(JSONSupportClass.TAG_WEBSITE) + "\n" +
                        json.getString(JSONSupportClass.TAG_REGDATE) + "\n------------------\n");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}