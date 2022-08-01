package com.example.distancetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView_distance;
    RadioGroup radioGroup_mall;
    private double D_latitude, D_longitude, distance;
    private boolean validate_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_distance = findViewById(R.id.textView_distance);
        D_latitude = 0;
        D_longitude = 0;
        distance = 0;
        validate_location = false;
        radioGroup_mall = findViewById(R.id.radio_group);

        radioGroup_mall.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = findViewById(i);

                if (radioButton.getText().equals("Spring Shopping Mall")) {
                    D_latitude = 1.5355458;
                    D_longitude = 110.3582016;
                }

                if (radioButton.getText().equals("Viva Shopping Mall")) {
                    D_latitude = 1.5265568;
                    D_longitude = 110.3696401;
                }

                if (validate_location) { // Update latitude and longitude to service class
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    intent.putExtra("D_latitude", D_latitude);
                    intent.putExtra("D_longitude", D_longitude);
                    startService(intent);
                }
            }
        });

        RequestLocation();
    }

    private void RequestLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(enabledGPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LocationService.FINE_LOCATION);
                }
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LocationService.COARSE_LOCATION);
                }
            }
            validate_location = true;
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            distance = intent.getDoubleExtra("distance", 0.0);
            String output = "Distance: Error (Select a location first)";
            if (distance != 0.0) {
                output = "Distance: " + String.format("%.2f", distance) + " KM";
            }
            textView_distance.setText(output);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.INTENT_NAME));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}