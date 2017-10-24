package com.jan.dbtest;

import android.app.ProgressDialog;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Insert part
    EditText editTextName;
    EditText editTextEmail;
    EditText editTextWebsite;

    String nameInsert;
    String emailInsert;
    String websiteInsert;

    Button buttonSubmit;

    // Select part
    private JSONArray result;
    private TextView textView1;
    private Button buttonGet;
    boolean log = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText)findViewById(R.id.editText1);
        editTextEmail = (EditText)findViewById(R.id.editText2);
        editTextWebsite = (EditText)findViewById(R.id.editText3);
        buttonSubmit = (Button)findViewById(R.id.insertContentButton);
        textView1 = (TextView)findViewById(R.id.textView1);
        buttonGet = (Button)findViewById(R.id.getContentButton);
        buttonSubmit.setOnClickListener(submitDataClicked);
        buttonGet.setOnClickListener(getDataClicked);

    }

    // Click listeners
    View.OnClickListener submitDataClicked = new View.OnClickListener(){

        @Override
        public void onClick(View v){
            GetDataFromEditText();
            SendData();
        }
    };

    View.OnClickListener getDataClicked = new View.OnClickListener(){

        @Override
        public void onClick(View v){
            getData();
        }
    };

    // Support function, just to set values of already declared Strings
    public void GetDataFromEditText(){

        nameInsert = editTextName.getText().toString();
        emailInsert = editTextEmail.getText().toString();
        websiteInsert = editTextWebsite.getText().toString();
    }

    // Implementation using Volley library
    private void SendData(){

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://android-db-js5898.c9users.io/insert_data.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        progressDialog = new ProgressDialog(MainActivity.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please Wait, We are Inserting Your Data on Server");
        progressDialog.show();

        // Creating string request with post method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {

                        // Hiding the progress dialog after all task complete
                        progressDialog.dismiss();

                        // Showing response message coming from server
                        Toast.makeText(MainActivity.this, ServerResponse, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                })

        {
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("name", nameInsert);
                params.put("email", emailInsert);
                params.put("website", websiteInsert);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    private void getData(){
        if(log) Log.d("debug", "HERE 2");
        StringRequest stringRequest = new StringRequest("https://android-db-js5898.c9users.io/select_data.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j;

                        if(log) Log.d("debug", "HERE 3: Response: " + response);
                        try {
                            if(log) Log.d("debug", "HERE 4 - TRY?");
                            j = new JSONObject(response);
                            result = j.getJSONArray(com.jan.dbtest.JSONSupportClass.JSON_ARRAY);
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