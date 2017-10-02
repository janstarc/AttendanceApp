package com.jan.dbtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // Insert part
    EditText editTextName;
    EditText editTextEmail;
    EditText editTextWebsite;

    String GetName;
    String GetEmail;
    String GetWebsite;

    Button buttonSubmit;

    String DataParseUrl = "https://android-db-js5898.c9users.io/insert_data.php";

    // Select part
    private JSONArray result;
    private TextView textView1;
    private Button buttonGet;


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

        // Submit data
        buttonSubmit.setOnClickListener(submitDataClicked);

        // Get data
        buttonGet.setOnClickListener(getDataClicked);

    }

    // Click listeners
    View.OnClickListener submitDataClicked = new View.OnClickListener(){

        @Override
        public void onClick(View v){
            GetDataFromEditText();
            SendDataToServer(GetName, GetEmail, GetWebsite);
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

        GetName = editTextName.getText().toString();
        GetEmail = editTextEmail.getText().toString();
        GetWebsite = editTextWebsite.getText().toString();
    }


    // Sending things to the DB
    public void SendDataToServer(final String name, final String email, final String website){
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String QuickName = name;
                String QuickEmail = email;
                String QuickWebsite = website;

                List<NameValuePair> nameValuePairs = new ArrayList<>();

                nameValuePairs.add(new BasicNameValuePair("name", QuickName));
                nameValuePairs.add(new BasicNameValuePair("email", QuickEmail));
                nameValuePairs.add(new BasicNameValuePair("website", QuickWebsite));

                try {
                    // TODO
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(DataParseUrl);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();

                } catch (ClientProtocolException e) {
                    Log.d("debug", "Client protocol exception");
                    Toast.makeText(MainActivity.this, "Error submiting data (CPE) - please try later", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error submiting data (IOE) - please try later", Toast.LENGTH_LONG).show();
                }
                return "Data Submit Successfully";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                Toast.makeText(MainActivity.this, "Data Submit Successfully", Toast.LENGTH_LONG).show();

            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(name, email, website);
    }

    private void getData(){
        Log.d("debug", "HERE 2");
        StringRequest stringRequest = new StringRequest("https://android-db-js5898.c9users.io/select_data.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        Log.d("debug", "HERE 3: Response: " + response);
                        try {
                            Log.d("debug", "HERE 4 - TRY?");
                            j = new JSONObject(response);
                            result = j.getJSONArray(com.jan.dbtest.Config.JSON_ARRAY);
                            parseJSON(result);
                        } catch (JSONException e) {
                            Log.d("debug", "HERE 4 - Exception?");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("debug", "On Error Listener: " + error);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void parseJSON(JSONArray j){
        Log.d("debug", "Here, being called");
        textView1.setText("");
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.Config.TAG_ID));
                Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.Config.TAG_ID));

                textView1.append(
                        json.getString(Config.TAG_ID) + "\n" +
                                json.getString(Config.TAG_NAME) + "\n" +
                                json.getString(Config.TAG_EMAIL) + "\n" +
                                json.getString(Config.TAG_WEBSITE) + "\n" +
                                json.getString(Config.TAG_REGDATE) + "\n------------------\n");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}