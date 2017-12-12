package com.jan.dbtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class checkIn extends AppCompatActivity implements LocationListener {


    private Context context = this;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";

    // Activity design
    private Button scanQRButton;
    private Button addToLessonButton;
    private ImageView loginOKimage;
    private ImageView codeOKimage;
    private ImageView locationOKimage;
    private ProgressBar progress1;
    private ProgressBar progress2;

    // GPS Start
    private LocationManager locationManagerGPS;
    private LocationManager locationManagerNetwork;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;

    private double latitude = -1;
    private double longitude = -1;
    private double accuracy = -1;

    // GPS End

    boolean QRSuccess = false;

    private boolean loginOK = false;
    private boolean codeOK = false;
    private boolean locationOK = false;
    String uniqueCode = null;

    /**
     *  onCreate and onResume START
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_in);

        defineVariables();
        setOkInvisible();
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        loginOK = checkLogin();


        // GPS onCreate START
        // GPS Permission check --> If permission not granted (Android 6.0+), ask for permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("gps", "Here - permission issue");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
            return;
        }
        // GPS onCreate END
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkLogin();
        checkLocation();
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


    /**
     *  onCreate and onResume END
     */

    /**
     *  Functions START
     */

    public void defineVariables(){
        scanQRButton = (Button) findViewById(R.id.scanQRButton);
        scanQRButton.setOnClickListener(scanQRListener);
        addToLessonButton = (Button) findViewById(R.id.addToLessonButton);
        addToLessonButton.setOnClickListener(addToLessonListener);
        loginOKimage = (ImageView) findViewById(R.id.imageView1);
        locationOKimage = (ImageView) findViewById(R.id.imageView2);
        codeOKimage = (ImageView) findViewById(R.id.imageView3);
        progress1 = (ProgressBar) findViewById(R.id.progress1);
        progress2 = (ProgressBar) findViewById(R.id.progress2);


        // GPS Objects START
        locationManagerGPS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManagerNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // GPS Objects END
    }

    public void setOkInvisible(){
        locationOKimage.setVisibility(View.INVISIBLE);
        codeOKimage.setVisibility(View.INVISIBLE);
        locationOKimage.setVisibility(View.INVISIBLE);
    }

    public boolean checkLogin(){

        if(prefs.getString("user_id", null) == null){
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {

            Log.d("userData", "Username: " + prefs.getString("username", null) + " | Password: " + prefs.getString("password", null) + " | UserId: " + prefs.getString("user_id", null));
            progress1.setVisibility(View.INVISIBLE);
            loginOKimage.setVisibility(View.VISIBLE);
            loginOK = true;
            return true;
        }

        return false;
    }

    public boolean checkQR(){
        if(uniqueCode != null && uniqueCode.length() == 6){
            scanQRButton.setVisibility(View.INVISIBLE);
            codeOKimage.setVisibility(View.VISIBLE);
            codeOK = true;
            return true;
        } else {
            //codeOKimage.setImageResource(R.drawable.error_img);
            Toast.makeText(context, "Unvaild code. Please scan the code again", Toast.LENGTH_SHORT).show();
            scanQRButton.setText("SCAN AGAIN");
            codeOK = false;
        }
        return false;
    }

    public boolean checkLocation(){
        if(longitude != -1 && latitude != -1 && accuracy != -1 && accuracy < 80){


            progress2.setVisibility(View.INVISIBLE);
            locationOKimage.setVisibility(View.VISIBLE);
            locationOK = true;
            return true;
        } else  {
            //locationOKimage.setImageResource(R.drawable.loading_img);
            // TODO
        }

        return false;
    }

    /**
     *  Functions END
     */

    /**
     *  QR Code START
     */

    View.OnClickListener scanQRListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            QRSuccess = false;
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
    };

    // When the QR code is scanned and result is returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {

                uniqueCode = data.getStringExtra("SCAN_RESULT");
                Log.d("result", "UniqueCode scanned: " + uniqueCode);
                if(checkQR()){
                    Toast.makeText(context, "UniqueCode: " + uniqueCode, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Scanned QR code does not contain lesson QR code", Toast.LENGTH_LONG).show();
                }
            }
            if(resultCode == RESULT_CANCELED){
                codeOK = false;
            }
        }
    }

    /**
     *  QR Code END
     */

    /**
     *  GPS START
     */

    @Override
    public void onLocationChanged(Location location) {


        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            //locationGPS.setText("GPS--> Lat: " + location.getLatitude() + " Long: " + location.getLongitude() + " Accur: " + location.getAccuracy());

            // First info
            if(accuracy == -1 && longitude == -1 && latitude == -1){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
                Log.d("findLocation", "First GPS location. " + latitude + " " + longitude + " " + accuracy + "m");
                checkLocation();
            }

            // When you get better information
            double accuracyNew = location.getAccuracy();            // Check if the new location is more accurate
            if(accuracy != -1 && accuracyNew < accuracy){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = accuracyNew;
                Log.d("findLocation", "Improved GPS location. " + latitude + " " + longitude + " " + accuracy + "m");
                checkLocation();
            }

        }

        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){

            if(accuracy == -1 && longitude == -1 && latitude == -1){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
                Log.d("findLocation", "First NET location. " + latitude + " " + longitude + " " + accuracy + "m");
                checkLocation();
            }

            double accuracyNew = location.getAccuracy();            // Check if the new location is more accurate
            if(accuracy != -1 && accuracyNew < accuracy){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = accuracyNew;
                Log.d("findLocation", "Improved NET location. " + latitude + " " + longitude + " " + accuracy + "m");
                checkLocation();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if(location.isFromMockProvider()){
                Log.d("findLocation", "Location from mock provider!");
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

    /**
     *  GPS END
     */

    /**
     *  AddToLesson START
     */

    View.OnClickListener addToLessonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d("addToLesson", "Here");
            if(loginOK && codeOK && locationOK){
                addToLesson();
            } else if (!loginOK){
                Toast.makeText(context, "Check your login data!", Toast.LENGTH_LONG).show();
            } else if (!codeOK) {
                Toast.makeText(context, "Scan the QR code again", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Wait for your phone to find location", Toast.LENGTH_LONG).show();
            }
        }
    };

    // Implementation using Volley library
    private void addToLesson(){

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/checkIn.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(checkIn.this);
        progressDialog = new ProgressDialog(checkIn.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please Wait, checking you in");
        progressDialog.show();

        // Creating string request with post method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {

                        // Hiding the progress dialog after all task complete
                        progressDialog.dismiss();

                        // Showing response message coming from server
                        if(ServerResponse.equals("0")){
                            Toast.makeText(checkIn.this, "You have been checked-in successfully!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        } else if (ServerResponse.equals("1")){
                            Toast.makeText(checkIn.this, "User doesn't exist!", Toast.LENGTH_LONG).show();
                        } else if (ServerResponse.equals("2")){
                            Toast.makeText(checkIn.this, "Your password is incorrect!", Toast.LENGTH_LONG).show();
                        } else if (ServerResponse.equals("3")){
                            Toast.makeText(checkIn.this, "Your location does not match the lecture location", Toast.LENGTH_LONG).show();
                        } else if (ServerResponse.equals("4")){
                            Toast.makeText(checkIn.this, "You are already checked-in to this lesson!", Toast.LENGTH_LONG).show();
                        } else if (ServerResponse.equals("5")){
                            Toast.makeText(checkIn.this, "Someone already signed in from the same device!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(checkIn.this, "Server error!" + ServerResponse, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(checkIn.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                })

        {
            // Parameters for the hash map --> The ones read by PHP script
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<>();

                // Adding All values to Params.
                params.put("username", prefs.getString("username", null));
                params.put("password", prefs.getString("password", null));
                params.put("uniqueCode", uniqueCode);
                params.put("latitude", Double.toString(latitude));
                params.put("longitude", Double.toString(longitude));
                params.put("deviceId", prefs.getString("deviceId", null));

                return params;
            }
        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    /**
     *  AddToLesson END
     */
}
