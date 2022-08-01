package com.example.distancetracker;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements LocationListener {
    private double M_latitude, M_longitude, D_latitude, D_longitude, distance;
    public static String INTENT_NAME = "Distance";
    public static int FINE_LOCATION = 100;
    public static int COARSE_LOCATION = 200;
    private static int MIN_TIME_INTERVAL = 5000;
    private Intent intent;
    private Handler handler;
    private Timer timer;
    private LocationManager locationManager;

    public LocationService() {
        M_latitude = 0;
        M_longitude = 0;
        D_latitude = 0;
        D_longitude = 0;
        distance = 0;
        handler = new Handler();
        timer = new Timer();
        locationManager = null;
    }

    @Override
    public void onCreate() {
        intent = new Intent(INTENT_NAME);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        D_latitude = intent.getDoubleExtra("D_latitude", 0.0);
        D_longitude = intent.getDoubleExtra("D_longitude", 0.0);

        timer = new Timer();
        timer.schedule(getTimerTask(), MIN_TIME_INTERVAL, MIN_TIME_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        M_latitude = location.getLatitude();
        M_longitude = location.getLongitude();
        CalculateDistance();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(@NonNull String provider) { }

    @Override
    public void onProviderDisabled(@NonNull String provider) { }

    private void CalculateDistance() {
        if ((M_latitude == D_latitude) && (M_longitude == D_longitude)) {
            distance = 0.0;
        }
        else {
            double theta = M_longitude - D_longitude;
            distance = Math.sin(Math.toRadians(M_latitude)) * Math.sin(Math.toRadians(D_latitude)) + Math.cos(Math.toRadians(M_latitude)) * Math.cos(Math.toRadians(D_latitude)) * Math.cos(Math.toRadians(theta));
            distance = Math.acos(distance);
            distance = Math.toDegrees(distance);
            distance = distance * 60 * 1.1515 * 1.609344;
        }

        intent.putExtra("distance", distance);
        sendBroadcast(intent);
    }

    private void RequestLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(enabledGPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL, 0, this); // Update every 5 seconds
        }
    }

    public TimerTask getTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        RequestLocation();
                    }
                });
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Unsupported Exception");
    }
}
