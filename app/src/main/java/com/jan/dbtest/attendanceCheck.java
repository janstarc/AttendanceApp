package com.jan.dbtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class attendanceCheck extends AppCompatActivity implements Serializable {

    private Context context = this;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_FILE = "MyPrefsFile";
    //private TextView scrollViewText;
    private JSONArray result;
    private Spinner coursesSpinner;
    private Button loadLessons;
    private TextView attendanceReport;
    private View rectangle;


    private ListView myList;
    private ListAdapter adapter;
    ArrayList<ListItem> items = new ArrayList<>();

    private ArrayList<String> courseIdList = new ArrayList<>();
    private ArrayList<String> courseNameList = new ArrayList<>();

    private ArrayList<String> allLessonsId = new ArrayList<>();
    private ArrayList<String> allLessonsTitle = new ArrayList<>();
    private ArrayList<String> allLessonsDescription = new ArrayList<>();

    private ArrayList<String> attendLessonsId = new ArrayList<>();
    private ArrayList<String> attendLessonsTitle = new ArrayList<>();
    private ArrayList<String> attendLessonsDescription = new ArrayList<>();
    private int requiredAttendance = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_check);

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();

        //scrollViewText = (TextView) findViewById(R.id.scrollViewText);
        myList = (ListView) findViewById(R.id.listView);
        myList.setVisibility(View.INVISIBLE);
        attendanceReport = (TextView) findViewById(R.id.attendanceReport);
        attendanceReport.setVisibility(View.INVISIBLE);
        rectangle = (View) findViewById(R.id.myRectangleView);
        rectangle.setVisibility(View.INVISIBLE);

        coursesSpinner = (Spinner) findViewById(R.id.coursesSpinner);
        loadLessons = (Button) findViewById(R.id.loadLessonsButton);
        loadLessons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(courseIdList.size() > 0) {

                    myList.setVisibility(View.VISIBLE);
                    attendanceReport.setVisibility(View.VISIBLE);
                    rectangle.setVisibility(View.VISIBLE);

                    String selectedCourseName = coursesSpinner.getSelectedItem().toString();
                    Log.d("index1", "Selected Course Name: " + selectedCourseName);
                    int selectedCourseIndex = courseNameList.indexOf(selectedCourseName);
                    Log.d("index1", "Index of course: " + selectedCourseIndex);
                    String selectedCourseId = courseIdList.get(selectedCourseIndex);
                    Log.d("index1", "Selected Course ID: " + selectedCourseId);

                    // Functions called in DAISY-CHAIN: getAllLessons(selectedCourseId, userId) --> getAttendedLessons(selectedCourseId, userId) --> updateListView()
                    getAllLessons(selectedCourseId, prefs.getString("user_id", null));
                }
            }
        });

        //getAllCourses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        courseIdList = new ArrayList<>();
        courseNameList = new ArrayList<>();
        getAllCourses();
    }

    public void createDropdownMenu(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, courseNameList);    // Create an ArrayAdapter using the string array and a default spinner layout
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        coursesSpinner.setAdapter(adapter);                // Apply the adapter to the spinner
    }


    private void printResults(String arrayListName, ArrayList<String> list){

        Log.d("queryResult", "--- " + arrayListName + "---");
        for(int i = 0; i < list.size(); i++) {
            Log.d("queryResult", list.get(i).toString());
        }

        Log.d("queryResult", "-----------END-----------");
    }

    /**
     * 1.) Reads from DB --> Updates ArrayLists
     * 2.) Adds items from all ArrayLists to ArrayList<ListItem> items --> The one that is passed to the adapter
     * 3.) Updates values - Drinks sum and units sum
     * 4.) Updates ListView
     */
    public void updateListView() throws ParseException {

        items = new ArrayList<>();
        Log.d("lID", "LessonId size: " + allLessonsId.size());

        // Copies data from other ArrayLists to the items ArrayList --> Passed to adapter
        if(allLessonsId.size() > 0){
            for(int i = 0; i < allLessonsId.size(); i++){
                addListItem(i);
            }

        } else {
            items.add(new ListItem("No drinks on the list", 0, "", false));
        }

        // Pass values to adapter
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
    }

    public void addListItem(int i) throws ParseException {


        String pictureName = null;
        boolean attended = false;

        if(attendLessonsId.contains(allLessonsId.get(i))){
            pictureName = "ok_icon";
            attended = true;
        } else {
            pictureName = "cross_icon";
        }

        int resourceId = this.getResources().getIdentifier(pictureName, "drawable", this.getPackageName());

        // New ListItem creation and putting it to items list
        ListItem item = new ListItem(i + ". " + allLessonsTitle.get(i), resourceId, "Desc: " + allLessonsDescription.get(i), attended);
        items.add(item);
    }



    /**
     *  QUERY part START
     */
    private void getAttendedLessons(final String courseId, final String userId){

        attendLessonsId = new ArrayList<>();
        attendLessonsTitle = new ArrayList<>();
        attendLessonsDescription = new ArrayList<>();

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/getAttendedCourseLessons.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(attendanceCheck.this);
        progressDialog = new ProgressDialog(attendanceCheck.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please wait, loading lessons");
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

                        // Looks like we have good response --> Parse!
                        JSONObject j;
                        try {
                            j = new JSONObject(ServerResponse);
                            result = j.getJSONArray(com.jan.dbtest.JSONSupportClass.JSON_ARRAY);
                            parseJSON_getAttendedLessons(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("jsonError", "JSON Error");
                        }

                        // TODO THREAD HANDLING
                        try {
                            updateListView();

                            double attendedLessons = attendLessonsId.size();
                            double allLessons = allLessonsId.size();
                            double percent = (attendedLessons/allLessons)*100;
                            int percentInt = (int) Math.floor(percent);

                            String textOut = "";

                            if(requiredAttendance == 0 || percentInt >= requiredAttendance){
                                textOut = "<font color=#757575><big>You attended </font>" +
                                        "<font color=#4684C4><b><big>" + (int) attendedLessons + "</b></big></font><font color=#757575> out of </font><font color=#4684C4><b><big> "
                                        + (int) allLessons + " </b></big></font>lessons<font color=#4684C4><b><big> (" + percentInt + "%)</b></big></font><br>" +
                                        "<font color=#757575>(Required attendance: " + requiredAttendance + "%)</font>";
                            } else {
                                textOut = "<font color=#757575><big>You attended </font>" +
                                        "<font color=#C64747><b><big>" + (int) attendedLessons + "</b></big></font><font color=#757575> out of </font><font color=#C64747><b><big> "
                                        + (int) allLessons + " </b></big></font>lessons<font color=#C64747><b><big> (" + percentInt + "%)</b></big></font><br>" +
                                        "<font color=#757575>(Required attendance: " + requiredAttendance + "%)</font>";
                            }



                            //attendanceReport.setText("You attended " + (int) attendedLessons + " out of " + (int) allLessons + " lessons (" + percentInt + "%) | Req: " + requiredAttendance + "%");
                            attendanceReport.setText(Html.fromHtml(textOut));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(attendanceCheck.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                params.put("user_id", userId);
                params.put("course_id", courseId);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);

    }

    private void parseJSON_getAttendedLessons(JSONArray j){
        Log.d("debug", "Here, being called. jLength: " + j.length());
        //scrollViewText.setText("");
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.JSONSupportClass.TAG_ID));
                //Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.JSONSupportClass.TAG_ID));

                //courseIdList.add(json.getString("course_id"));
                //courseNameList.add(json.getString("course_name"));

                attendLessonsId.add(json.getString("lesson_id"));
                attendLessonsTitle.add(json.getString("lesson_title"));
                attendLessonsDescription.add(json.getString("lesson_description"));
                requiredAttendance = json.getInt("required_attendance");
                /*
                scrollViewText.append(
                        json.getString("course_id") + "\n" +
                                json.getString("course_name") + "\n" +
                                "\n------------------\n");
                */

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        printResults("attendLessonID", attendLessonsId);
        printResults("attendLessonsTitle", attendLessonsTitle);
        printResults("attendLessonsDescription", attendLessonsDescription);
        Log.d("queryResult", "Req attendance: " + requiredAttendance);

    }

    private void getAllLessons(final String courseId, final String userId){

        allLessonsId = new ArrayList<>();
        allLessonsTitle = new ArrayList<>();
        allLessonsDescription = new ArrayList<>();

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/getAllCourseLessons.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(attendanceCheck.this);
        progressDialog = new ProgressDialog(attendanceCheck.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please wait, loading lessons");
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

                        // Looks like we have good response --> Parse!
                        JSONObject j;
                        try {
                            j = new JSONObject(ServerResponse);
                            result = j.getJSONArray(com.jan.dbtest.JSONSupportClass.JSON_ARRAY);
                            parseJSON_getAllLessons(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("jsonError", "JSON Error");
                        }

                        // TODO TRY TO AVOID THREAD HANDLING
                        getAttendedLessons(courseId, userId);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(attendanceCheck.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                params.put("course_id", courseId);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    private void parseJSON_getAllLessons(JSONArray j){
        Log.d("debug", "Here, being called. jLength: " + j.length());
        //scrollViewText.setText("");
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.JSONSupportClass.TAG_ID));
                //Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.JSONSupportClass.TAG_ID));

                //courseIdList.add(json.getString("course_id"));
                //courseNameList.add(json.getString("course_name"));

                allLessonsId.add(json.getString("lesson_id"));
                allLessonsTitle.add(json.getString("lesson_title"));
                allLessonsDescription.add(json.getString("lesson_description"));

                /*
                scrollViewText.append(
                        json.getString("course_id") + "\n" +
                                json.getString("course_name") + "\n" +
                                "\n------------------\n");
                */

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        printResults("ALL-LessonID", allLessonsId);
        printResults("ALL-LessonTitle", allLessonsTitle);
        printResults("ALL-LessonDescription", allLessonsDescription);

    }

    // Implementation using Volley library
    private void getAllCourses(){

        courseIdList = new ArrayList<>();
        courseNameList = new ArrayList<>();

        // Creating Volley RequestQueue
        RequestQueue requestQueue;

        // Creating Progress dialog
        final ProgressDialog progressDialog;

        // Storing server url into String variable.
        String HttpUrl = "https://attendance-system-server-js5898.c9users.io/AndroidScripts/getAllCourses.php";

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(attendanceCheck.this);
        progressDialog = new ProgressDialog(attendanceCheck.this);

        // Showing progress dialog at user registration time
        progressDialog.setMessage("Please wait, loading courses");
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

                        // Looks like we have good response --> Parse!
                        JSONObject j;
                        try {
                            j = new JSONObject(ServerResponse);
                            result = j.getJSONArray(com.jan.dbtest.JSONSupportClass.JSON_ARRAY);
                            parseJSON_getAllCourses(result);
                            createDropdownMenu();
                            if (courseIdList.size() == 0){
                                attendanceReport.setText("You haven't added any course yet. Register to courses!");
                            } else {
                                attendanceReport.setText("");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("jsonError", "JSON Error");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Toast.makeText(attendanceCheck.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                String userId = prefs.getString("user_id", null);
                Log.d("responseMessage", "userID: " + userId);
                params.put("user_id", userId);

                return params;
            }

        };

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    private void parseJSON_getAllCourses(JSONArray j){
        Log.d("debug", "Here, being called. jLength: " + j.length());
        //scrollViewText.setText("");
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                //students.add(json.getString(com.jan.dbselect.JSONSupportClass.TAG_ID));
                //Log.d("debug", "Vrstica: " + json.getString(com.jan.dbtest.JSONSupportClass.TAG_ID));

                courseIdList.add(json.getString("course_id"));
                courseNameList.add(json.getString("course_name"));

                /*
                scrollViewText.append(
                        json.getString("course_id") + "\n" +
                        json.getString("course_name") + "\n" +
                        "\n------------------\n");
                */
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        printResults("CourseID", courseIdList);
        printResults("CourseName", courseNameList);
    }

    /**
     *  QUERY part END
     */

}
