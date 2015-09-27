package com.example.raghav.myola;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import android.os.AsyncTask;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import org.json.*;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.*;
public class MainActivity extends Activity {
    //ListView listView
    String[] blogTitles;
    Button goBtn;
    EditText peopleCount;
    final Double SEDAN_CAPACITY = 4.0;
    final Double MINI_CAPACITY = 4.0;
    final Double PRIME_CAPACITY = 6.0;
    Double minEstimate =0.0;
    Double maxEstimate =0.0;
    ArrayList<CarDetails> allBookedCars;
    Double loopLimit = 0.0 ;
    int countOfBookedCars = 0;
    String Bookingmessage = "";
    String sedanrideEstimateUrl = "http://sandbox-t.olacabs.com/v1/products?pickup_lat=12.950072&pickup_lng=77.642684&drop_lat=13.039308&drop_lng=77.599994&category=sedan";
    String sedanbookingUrl = "http://sandbox-t.olacabs.com/v1/bookings/create?pickup_lat=12.950072&pickup_lng=77.642684&pickup_mode=NOW&category=sedan";
    String sedanAvailabilty = "http://sandbox-t.olacabs.com/v1/products?pickup_lat=12.94887&pickup_lng=77.643684&category=sedan";
    //private ArrayAdapter arrayAdapter;
    String access_token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allBookedCars = new ArrayList<>();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("currentuser", 0); // 0 - for private mode
        access_token = pref.getString("access_token", null);
        Toast.makeText(getApplicationContext(),"ACCESS-TOKEN" + access_token,Toast.LENGTH_LONG).show();

        goBtn = (Button)findViewById(R.id.gobutton);
        peopleCount = (EditText)findViewById(R.id.editText);
        Spinner mySpinner=(Spinner) findViewById(R.id.spinner);
        final String text = mySpinner.getSelectedItem().toString();
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "SELECTED " + text + " "+ peopleCount.getText().toString(), Toast.LENGTH_LONG).show();
                if (text.trim().contains("Economy")) {
                    //go for sedan
                    Double x = Double.parseDouble(peopleCount.getText().toString());
                    //Toast.makeText(getApplicationContext(), "PEOPLE COUNT " + x, Toast.LENGTH_LONG).show();

                    loopLimit = Math.ceil(x/ SEDAN_CAPACITY);
                    Toast.makeText(getApplicationContext(),"Looping for sedan "+ loopLimit,Toast.LENGTH_LONG).show();
                    new AsyncHttpTask().execute(sedanAvailabilty);
                    /*
                    for (int i = 0; i < loopLimit; i++) {
                        new AsyncHttpTask().execute(sedanAvailabilty);

                        if(blogTitles.length!=0){
                            Toast.makeText(getApplicationContext(),"HURRAY GOT SEDAN",Toast.LENGTH_LONG).show();
                            countOfBookedCars++;

                        }

                    }
                    */
                } else {

                }
            }
        });

        //listView = (ListView) findViewById(R.id.listView);
        //final String url = "http://sandbox-t.olacabs.com/v1/products?pickup_lat=12.94887&pickup_lng=77.643684&category=sedan";

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }
        if(null!=inputStream){
            inputStream.close();
        }
        return result;
    }
    private void parseResult(String result) {
        try{
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("categories");
            blogTitles = new String[posts.length()];

            for(int i=0; i< posts.length();i++ ) {
                JSONObject post = posts.optJSONObject(i);
                String title = post.optString("id");
                System.out.println("CURRENT RESPONSE " + title);
                blogTitles[i] = title;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void parseCostEstimateResult(String result) {
        try{
            JSONObject response = new JSONObject(result);
            JSONArray bookingdetails = response.getJSONArray("ride_estimate");
            System.out.println("I AM PARSIN ESTIMATES");

            for(int i=0; i< bookingdetails.length();i++ ){
                JSONObject current = bookingdetails.optJSONObject(i);
                String currentcategory=current.optString("category");
                if(currentcategory.trim().contains("sedan")) {
                    Double currentmin = current.optDouble("amount_min");
                    Double currentmax = current.optDouble("amount_max");
                    System.out.println("CURRENT MIN IS "+currentmin);
                    minEstimate  = minEstimate + currentmin;
                    maxEstimate  = maxEstimate + currentmax;
                    //Toast.makeText(getApplicationContext(), "I am ESTIMATING FOR " + currentcategory, Toast.LENGTH_LONG).show();

                }
            }
        }catch (Exception e){
            System.out.println("YOUR ERROR IS " + e);
        }
    }

    private void parseBookingResult(String result) {
        try{
            JSONObject response = new JSONObject(result);
            JSONArray header = response.names();
            //Iterator<?> keys = response.keys();
            System.out.println("KEYS ARE : "+header.toString());
            /*while( keys.hasNext() ) {
                String key = (String)keys.next();
                if (key.contains("message")) {
                    Bookingmessage = "Car could not be booked due to some technical reason";
                    return;
                }
            }
            */
            Bookingmessage="SUCCESS";

                //{
                    //"crn": "2363",
                       // "driver_name": "Phonenix D343",
                       // "driver_number": "4567894343",
                       // "cab_type": "sedan",
                       // "cab_number": "KA 34  3",
                     //   "car_model": "Toyota Corolla",
                     //   "eta": 2,
                   //     "driver_lat": 12.950074,
                  //      "driver_lng": 77.641727
                //}
            System.out.println("AM BOOKING SUCCESSFULLY");



        }catch (Exception e){
            System.out.println("YOUR ERROR IS "+e);
        }
    }


    private void start(int number)
    {
        if(number == loopLimit-1)
        {
            return;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"LOOKING FOR SEDAN NUMBER "+countOfBookedCars ,Toast.LENGTH_LONG).show();

            new AsyncHttpTask().execute(sedanAvailabilty);
        }
    }
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            Toast.makeText(getApplicationContext(),"RESULT "+result,Toast.LENGTH_LONG).show();
            if(result == 200){
                new AsyncHttpTaskForEstimate().execute(sedanrideEstimateUrl);

                //arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, blogTitles);
                //listView.setAdapter(arrayAdapter);
            }else{

            }
        }
        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            int statusCode = 0;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                //urlConnection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.addRequestProperty("X-App-Token", "ca4a436b7f5c457d9ee16278f1ae6cc0");
                //urlConnection.setDoInput(true);
                //urlConnection.setDoOutput(true);
                //urlConnection.connect();
                statusCode = urlConnection.getResponseCode();
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    System.out.println("CURRENT RESPONSE "+response);
                    parseResult(response);
                    result = 1;
                }else{
                    result = 0;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return statusCode;
        }
    }



    public class AsyncHttpTaskForEstimate extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            Toast.makeText(getApplicationContext(),"estimate is  "+result,Toast.LENGTH_LONG).show();
            if(result == 200){

                new AsyncHttpTaskForCabBooking().execute(sedanbookingUrl);
                //arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, blogTitles);
                //listView.setAdapter(arrayAdapter);
            }else{

            }
        }
        @Override
        protected Integer doInBackground(String... params) {
            System.out.println("PARAMETER TO ESTIMATE API "+params[0]);
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            int statusCode = 0;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                //urlConnection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.addRequestProperty("X-App-Token", "ca4a436b7f5c457d9ee16278f1ae6cc0");
                //urlConnection.setDoInput(true);
                //urlConnection.setDoOutput(true);
                //urlConnection.connect();
                statusCode = urlConnection.getResponseCode();
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    System.out.println("ESTIMATE WALE KA CURRENT RESPONSE "+response);
                    parseCostEstimateResult(response);
                    result = 1;
                }else{
                    result = 0;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return statusCode;
        }


    }

    public class AsyncHttpTaskForCabBooking extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPostExecute(Integer result) {
            Toast.makeText(getApplicationContext(),"API Status for Booking is  "+Bookingmessage,Toast.LENGTH_LONG).show();
            if(result == 200 && Bookingmessage.equals("SUCCESS")){
                    Toast.makeText(getApplicationContext(),"HURRAY GOT SEDAN, BOOKED !!",Toast.LENGTH_LONG).show();
                    countOfBookedCars++;
                    start(countOfBookedCars);

                //arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, blogTitles);
                //listView.setAdapter(arrayAdapter);
            }else{

            }
        }
        @Override
        protected Integer doInBackground(String... params) {
            System.out.println("PARAMETER TO BOOKING API " + params[0]);
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            int statusCode = 0;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                //urlConnection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.addRequestProperty("X-App-Token", "ca4a436b7f5c457d9ee16278f1ae6cc0");
                urlConnection.addRequestProperty("Authorization","Bearer " +access_token);
                //urlConnection.setDoInput(true);
                //urlConnection.setDoOutput(true);
                //urlConnection.connect();
                statusCode = urlConnection.getResponseCode();
                System.out.println("BOOKING WALE KA STATUS "+statusCode);
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    System.out.println("BOOKING WALE KA CURRENT RESPONSE "+response);
                    parseCostEstimateResult(response);
                    result = 1;
                }else{
                    result = 0;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return statusCode;
        }


    }
}
