package com.jan.dbtest;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";
    private EditText usernameET;
    private EditText passwordET;
    private Button loginButton;
    private Button registerButton;
    String username;
    String password;
    private Context context = this;

    private JSONArray result;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Define the objects, start onClickListener
        defineVariables();
        fillUsernamePassword();
    }

    public void defineVariables(){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();

        usernameET = (EditText) findViewById(R.id.username);
        passwordET = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = usernameET.getText().toString();
                password = passwordET.getText().toString();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putString("user_id", null);
                editor.apply();

                login();
            }
        });

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, registration.class);
                context.startActivity(intent);
            }
        });
    }

    public void login(){

        sendLoginRequest();
    }


    // Enters username and password to the EditText fields
    public void fillUsernamePassword(){

        usernameET.setText(prefs.getString("username", ""));
        passwordET.setText(prefs.getString("password", ""));
    }


    // Implementation using Volley library
    private void sendLoginRequest(){

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/loginScript.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(LoginActivity.this);
        progressDialog = new ProgressDialog(LoginActivity.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Logging in");
        progressDialog.show();

        // Creating string request with post method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {

                        // Hiding the progress dialog after all task complete
                        progressDialog.dismiss();

                        // Showing response message coming from server
                        boolean loginSuccess = false;

                        Log.d("ServerResponse", "User_ID --> " + ServerResponse);
                        try {
                            editor.putString("user_id", ServerResponse);
                            editor.apply();
                            loginSuccess = true;
                        } catch (NumberFormatException e) {
                            Toast.makeText(LoginActivity.this, ServerResponse, Toast.LENGTH_LONG).show();
                        }

                        if(loginSuccess){
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                })

        {
            // Parameters for the hash map --> The ones read by PHP script
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("username", username);
                params.put("password", password);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }


    private void parseJSON(JSONArray j){
        Log.d("debug", "Here, being called. ArrayLen: " + j.length());
        //textView1.setText("");


        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.JSONSupportClass.TAG_ID));
                 //Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.JSONSupportClass.TAG_ID));

                 //user_id = json.getString(JSONSupportClass.USER_ID);
                 String userId = json.getString(JSONSupportClass.USER_ID);
                 Log.d("ServerResponse", "UserID: " + userId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
