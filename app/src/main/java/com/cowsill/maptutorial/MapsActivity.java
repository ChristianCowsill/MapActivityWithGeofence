package com.cowsill.maptutorial;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback {

    private GeofencingClient mGeofencingClient;
    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private ArrayList<Geofence> mGeofenceList;
    private ArrayList<MyGeofence> mMyGeofenceList;
    public static final int PERMISSION_REQUEST_CODE = 1;

    private Marker mCurrentLocation;

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            double lat = intent.getDoubleExtra("latitude", 0);
            double lon = intent.getDoubleExtra("longtitude", 0);
            int complete = intent.getIntExtra("complete", 0);

            if (complete == 1) {
                LatLng latLng = new LatLng(lat, lon);
                updateMarker(latLng);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkPermissions();

    }

    private void checkPermissions() {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    // TO DO:  Get geofence information from list and populate map with geofence markers/circles
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        startLocationService();
        new GeofenceCreationAsyncTask().execute();


    }

    private void startLocationService() {
        Intent startLocationService = new Intent(
                this,
                LocationService.class
        );
        startService(startLocationService);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        registerReceiver(receiver,
                new IntentFilter("com.cowsill.maptutorial.LocationService"));
    }

    @Override
    protected void onPause() {

        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
        super.onPause();

    }

    private void updateMarker(LatLng latLng) {

        if (mCurrentLocation == null) {
            mCurrentLocation = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Here I am")
            );
        } else {
            mCurrentLocation.setPosition(latLng);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    class GeofenceCreationAsyncTask extends AsyncTask<Void, Void, Void> {

        PendingIntent geofencePendingIntent;

        @Override
        protected void onPreExecute() {
            // initialize geofence list before starting background task
            mGeofenceList = new ArrayList<>();
            mMyGeofenceList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            createGeofenceObjects();

            // Permissions are requested in OnCreate so they will definitely have been given
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            mGeofencingClient.addGeofences(getGeofencingRequest(),
                    getGeofencePendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "onSuccess: Geofences added");
                    Toast.makeText(
                            getApplicationContext(),
                            "Geofences successfully added",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "There was a problem adding the geofences.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            placeGeofencesOnMap();
        }

        private PendingIntent getGeofencePendingIntent() {

            // The PendingIntent is basically a singleton
            if (geofencePendingIntent != null) {
                return geofencePendingIntent;
            } else {
                Intent intent = new Intent(MapsActivity.this,
                        GeofenceTransitionsService.class);
                geofencePendingIntent = PendingIntent.getService(MapsActivity.this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            return geofencePendingIntent;
        }

        private GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);

            return builder.build();
        }

        private void createGeofenceObjects() {

            // Create array of transition events
            ArrayList<Integer> transitionEvents = new ArrayList<>();
            transitionEvents.add(Geofence.GEOFENCE_TRANSITION_ENTER);
            transitionEvents.add(Geofence.GEOFENCE_TRANSITION_EXIT);

            // Initialize geofences
            MyGeofence homeFence = new MyGeofence("Home",
                    43.252294,
                    -79.828408,
                    50,
                    Geofence.NEVER_EXPIRE,
                    transitionEvents
            );

            MyGeofence workFence = new MyGeofence("Work",
                    43.238864,
                    -79.848904,
                    100,
                    Geofence.NEVER_EXPIRE,
                    transitionEvents);


            // Add geofences to master lists
            mMyGeofenceList.add(homeFence);
            mMyGeofenceList.add(workFence);

            for (MyGeofence geofence : mMyGeofenceList) {
                mGeofenceList.add(geofence.createGeofence());
            }

        }
    }

    private void placeGeofencesOnMap() {

        for (MyGeofence geofence : mMyGeofenceList) {
            updateMap(geofence);
            Log.i(TAG, "placeGeofencesOnMap: " + geofence.getRequestId());
        }
    }

    private void updateMap(MyGeofence geofence) {

        LatLng latLng = new LatLng(geofence.getLatitude(), geofence.getLongtitude());

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(geofence.getRequestId())
        );

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(geofence.getRadius())
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.parseColor("#200084d3")) //#AARRGGBB AA = transparency
                .visible(true)
        );
    }
}
