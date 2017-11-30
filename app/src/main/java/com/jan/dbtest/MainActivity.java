package com.jan.dbtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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

public class MainActivity extends AppCompatActivity implements LocationListener {

    // Insert part
    Context context = this;
    EditText editTextName;
    EditText editTextEmail;
    EditText editTextWebsite;

    String usernameInsert;
    String passwordInsert;
    String uniqueCodeInsert;

    Button buttonSubmit;

    // Select part
    private JSONArray result;
    private TextView textView1;
    private Button buttonGet;
    private Button getCoordinates;
    boolean log = false;


    // GPS Start
    private TextView locationGPS;
    private TextView locationNetwork;
    private Boolean InformationObtained;
    private LocationManager locationManagerGPS;
    private LocationManager locationManagerNetwork;

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;

    // GPS End

    // QRScanner
    private Button QRScannerButton;
    private Button QRScanner2Button;

    // Login
    private Button loginActivityButton;

    // SharedPreferences
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        checkLogin();

        editTextName = (EditText)findViewById(R.id.editText1);
        editTextEmail = (EditText)findViewById(R.id.editText2);
        editTextWebsite = (EditText)findViewById(R.id.editText3);
        buttonSubmit = (Button)findViewById(R.id.insertContentButton);
        textView1 = (TextView)findViewById(R.id.textView1);
        buttonGet = (Button)findViewById(R.id.getContentButton);
        buttonSubmit.setOnClickListener(submitDataClicked);
        buttonGet.setOnClickListener(getDataClicked);

        // GPS START

        // GPS Permission check --> If permission not granted (Android 6.0+), ask for permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("gps", "Here - permission issue");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
            return;
        }


        locationGPS = (TextView) findViewById(R.id.locationGPS);
        locationNetwork = (TextView) findViewById(R.id.locationNetwork);
        locationManagerGPS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // GPS END

        // QR Code start
        QRScannerButton = (Button) findViewById(R.id.QRScannerButton);
        QRScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QRScanner.class);
                context.startActivity(intent);
            }
        });

        QRScanner2Button = (Button) findViewById(R.id.QRScanner2Button);
        QRScanner2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

                    startActivityForResult(intent, 0);

                } catch (Exception e) {

                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                    startActivity(marketIntent);

                }
            }
        });
        // QR Code End

        loginActivityButton = (Button) findViewById(R.id.loginActivityButton);
        loginActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });

    }

    public void checkLogin(){

        if(prefs.getInt("user_id", -1) == -1){
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {
            // TODO Part to delete
            Log.d("userData", "Username: " + prefs.getString("username", null) + " | Password: " + prefs.getString("password", null) + " | UserId: " + prefs.getInt("user_id", -1));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkLogin();
        // GPS Permission check --> If permission not granted (Android 6.0+), ask for permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("gps", "Here - permission issue");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                // TODO First install issue!
            }

            return;
        } else {
            // GPS Settings
            this.locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
            this.locationManagerNetwork.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, this);
        }
    }


    // Result from QRScanner2 --> Calls external app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                Log.d("result", "Contents: " + contents);
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {


        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            locationGPS.setText("GPS--> Lat: " + location.getLatitude() + " Long: " + location.getLongitude() + " Accur: " + location.getAccuracy());
        }

        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
            locationNetwork.setText("Net --> Lat: " + location.getLatitude() + " Long: " + location.getLongitude() + " Accur: " + location.getAccuracy());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if(location.isFromMockProvider()){
                Log.d("mock", "Location from mock provider!");
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

        usernameInsert = editTextName.getText().toString();
        passwordInsert = editTextEmail.getText().toString();
        uniqueCodeInsert = editTextWebsite.getText().toString();
    }

    // Implementation using Volley library
    private void SendData(){

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/insert_data.1.php";

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
            // Parameters for the hash map --> The ones read by PHP script
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("username", usernameInsert);

                // String hashed = BCrypt.hashpw(passwordInsert, BCrypt.gensalt());
                //Log.d("Hash", "HashedPass: " + passwordInsert);
                params.put("password", passwordInsert);
                params.put("uniqueCode", uniqueCodeInsert);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

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