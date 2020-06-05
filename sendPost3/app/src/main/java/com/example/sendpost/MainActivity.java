package com.example.sendpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Looper;
import android.Manifest;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



public class MainActivity extends AppCompatActivity {

    private TextView mTextViewResult;
    private TextView textView;
    private TextView textView2;

    EditText username;
    EditText password;
    Button enterBTN;
    Button serverIPBtn;

    String nameStr;
    String passStr;

    String lat;
    String lng;


    int PERMISSION_ID = 44;  //dont' konw
    FusedLocationProviderClient mFusedLocationClient; //fuse is a service providern
    TextView latTextView, lonTextView;  //you can put two vars under one thing




    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private void serverIPBtn(){
        Intent intent = new Intent(this, Next.class);
        startActivity(intent);
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){  //THis is the moneys
        if (checkPermissions()) {  //Always required to get location service
            if (isLocationEnabled()) {  //didn't turn on location yesterday
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData(); //gets new location if there is none
                                } else {
                                    latTextView.setText(location.getLatitude()+""); //Here is the golden ticket
                                    lonTextView.setText(location.getLongitude()+""); //Here is the golden ticket
                                    lat = location.getLatitude()+"";
                                    lng = location.getLongitude()+"";
                                    Log.d("none", location.getLongitude()+"");

                                    //-----------------------------------------------------------
                                    //API POST
                                    OkHttpClient client = new OkHttpClient();
                                    MediaType MEDIA_TYPE = MediaType.parse("application/json");
                                    String url = "http://178.128.178.72:56733/incomingCoords";


                                    JSONObject postdata = new JSONObject();
                                    try {
                                        //put("name", "12344321234124");
                                        //postdata.put("password", "4123l4kj132k4j");
                                        postdata.put("lat", location.getLatitude());
                                        postdata.put("lng", location.getLongitude());
                                        Log.d("none", "long from json" + lat);


                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                    RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());


                                    Request request = new Request.Builder()
                                            .url(url)
                                            .post(body)
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .build();

                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            if (response.isSuccessful()) {

                                                final String myResponse = response.body().string();

                                                MainActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //mTextViewResult.setText(myResponse);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    //end API POST
                                    //-------------------------------------------------

                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();  //ask for permission from dumb users
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();  //new location
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //sets proority
        mLocationRequest.setInterval(0);  //sets internval
        mLocationRequest.setFastestInterval(0); //Not sure how this is different
        mLocationRequest.setNumUpdates(1); //Not sure

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  //Sets me as the client
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,  //Gets location updates
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();  //Get lasts location
            latTextView.setText(mLastLocation.getLatitude()+"");  //Sets to print in layout
            lonTextView.setText(mLastLocation.getLongitude()+"");
            Log.d("none", mLastLocation.getLatitude()+"");
            lat = mLastLocation.getLatitude()+"";
            lng = mLastLocation.getLongitude()+"";

        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;  //Looks that permissions are granted for this
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},  //Asks for permissions
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {  //Checks that location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {  //Checks permissions
            getLastLocation();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        enterBTN = (Button) findViewById(R.id.enterBTN);
        username = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        serverIPBtn = (Button) findViewById(R.id.serverIP);

        enterBTN.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                nameStr = username.getText().toString();
                passStr = password.getText().toString();
                showToast(nameStr);


            }

        });
        serverIPBtn.setOnClickListener(new View.OnClickListener(){
                                           @Override
                                           public void onClick(View v){
                                               serverIPBtn();


                                           }
                                       }
        );


        latTextView = findViewById(R.id.latTextView);
        lonTextView = findViewById(R.id.lonTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  //Prob location from fused

        getLastLocation();


        //mTextViewResult = findViewById(R.id.text_view_result);




    }

}
