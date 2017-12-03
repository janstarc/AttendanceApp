package com.jan.dbtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class registerToCourse extends AppCompatActivity {

    private Context context = this;
    private Button register;
    private EditText courseIdEditText;
    // SharedPreferences
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";
    private JSONArray result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_to_course);

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        courseIdEditText = (EditText) findViewById(R.id.registerToCourseEditText);
        register = (Button) findViewById(R.id.registerToCourseButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String courseId = courseIdEditText.getText().toString();
                String userId = prefs.getString("user_id", null);
                if(!courseId.equals("") && userId != null){
                    registerToCourse(courseId, userId);

                }

            }
        });
    }

    private void registerToCourse(final String courseIdregister, final String userIdregister){

        Log.d("responseMessage", "CourseID: " + courseIdregister + " | UserID: " + userIdregister);

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/registerToCourse.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(registerToCourse.this);
        progressDialog = new ProgressDialog(registerToCourse.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please wait, registering you to course");
        progressDialog.show();

        // Creating string request with post method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {

                        // Hiding the progress dialog after all task complete
                        progressDialog.dismiss();

                        // Showing response message coming from server
                        //Toast.makeText(attendanceCheck.this, ServerResponse, Toast.LENGTH_LONG).show();
                        //Log.d("responseMessage", "Response: " + ServerResponse);

                        if(ServerResponse.equals("0")){
                            Toast.makeText(registerToCourse.this, "Registration successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        } else if(ServerResponse.equals("1")) {
                            Toast.makeText(registerToCourse.this, "You are already registered to this course!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(registerToCourse.this, "Course ID=" + courseIdregister + " doesn't exist!", Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(registerToCourse.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        Log.d("responseMessage", "Response: " + volleyError.toString());
                    }
                })

        {
            // Parameters for the hash map --> The ones read by PHP script
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("user_id", userIdregister);
                params.put("course_id", courseIdregister);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }



}
