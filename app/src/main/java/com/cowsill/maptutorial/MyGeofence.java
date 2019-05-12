package com.cowsill.maptutorial;

import com.google.android.gms.location.Geofence;
import java.util.ArrayList;


public class MyGeofence implements Geofence {

    private String requestId;
    private double latitude;
    private double longtitude;
    private float radius;
    private long expirationDuration;

    public MyGeofence(String requestId, double latitude, double longtitude, float radius,
                      long expirationDuration,
                      ArrayList<Integer> transitionTypes){
        this.requestId = requestId;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.radius = radius;
        this.expirationDuration = expirationDuration;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public void setExpirationDuration(long expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    @Override
    public String getRequestId() {
        return null;
    }

    public Geofence createGeofence(){

        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId(requestId)
                .setExpirationDuration(expirationDuration)
                .setCircularRegion(latitude,
                        longtitude,
                        radius
                        )
                .setExpirationDuration(expirationDuration)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        return builder.build();
    }
}
