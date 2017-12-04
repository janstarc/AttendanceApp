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
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


public class registration extends AppCompatActivity {


    private Context context = this;

    private EditText firstNameET;
    private EditText lastNameET;
    private EditText emailET;
    private EditText usernameET;
    private EditText passwordET;
    private Button createNewAccount;
    private Button loginButton;

    // SharedPreferences
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        firstNameET= (EditText) findViewById(R.id.firstname);
        lastNameET = (EditText) findViewById(R.id.lastname);
        emailET = (EditText) findViewById(R.id.email);
        usernameET = (EditText) findViewById(R.id.username);
        passwordET = (EditText) findViewById(R.id.password);
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();

        createNewAccount = (Button) findViewById(R.id.createNewAccount);
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fn = firstNameET.getText().toString();
                String ln = lastNameET.getText().toString();
                String em = emailET.getText().toString();
                String un = usernameET.getText().toString();
                String pw = passwordET.getText().toString();

                if(!fn.equals("") && !ln.equals("") && !em.equals("") && !un.equals("") && !pw.equals("")){
                    createAccount(fn, ln, em, un, pw);
                } else {
                    Toast.makeText(registration.this, "All fields are required!", Toast.LENGTH_LONG).show();
                }
            }
        });

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });

    }

    public void createAccount(final String firstname, final String lastname, final String email, final String username, final String password){

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/registrationAndroid.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(registration.this);
        progressDialog = new ProgressDialog(registration.this);

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
                        Log.d("responseMessage", "Response: " + ServerResponse);


                        if(ServerResponse.equals("0")){
                            Toast.makeText(registration.this, "Your account was created successfully!", Toast.LENGTH_LONG).show();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);

                        } else if (ServerResponse.equals("1")){
                            Toast.makeText(registration.this, "Username '" + username + "' already exists. Please choose another username", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(registration.this, ServerResponse, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(registration.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        Log.d("responseMessage", "ERROR: " + volleyError.toString());
                    }
                })

        {
            // Parameters for the hash map --> The ones read by PHP script
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);

                return params;
            }
        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

}
