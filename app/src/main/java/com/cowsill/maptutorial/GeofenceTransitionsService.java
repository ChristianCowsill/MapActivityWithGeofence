package com.cowsill.maptutorial;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsService extends IntentService {

    public static final String TAG = "GeofenceTransitionSer";

    String geofenceLocationString;
    String geofenceTransitionString;
    String errorMessage;
    public static final int NOTIFICATION_CODE = 1000;


    public GeofenceTransitionsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i(TAG, "onHandleIntent: ");
        handleTransition(intent);

    }

    private Notification createNotification(String geofenceLocation, String geofenceTransitionType) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                MapTutorialApplication.getGeofenceTransitionChannelId())
                .setSmallIcon(R.drawable.ic_location_notification_icon)
                .setContentTitle(geofenceLocation + " transition event.")
                .setContentText("You just " + geofenceTransitionType + " " + geofenceLocation + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder.build();
    }

    private void handleTransition(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handle any errors by logging error code
        if (geofencingEvent.hasError()) {
            errorMessage = String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, "HandleTransition: " + errorMessage);
            return;
        }

        // Get transition type
        int geofenceTransitionCode = geofencingEvent.getGeofenceTransition();

        // Verify that the transition is one we're interested in
        if (geofenceTransitionCode == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransitionCode == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get location string of transtion
            List<Geofence> transtionEventList = geofencingEvent.getTriggeringGeofences();
            for(Geofence geofence : transtionEventList){
                geofenceLocationString = geofence.getRequestId();
            }

            // get transition type string of transition
            switch (geofenceTransitionCode) {

                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    geofenceTransitionString = "entered";
                    break;

                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    geofenceTransitionString = "exited";
                    break;

                default:
                    Log.i(TAG, "HandleTransition: No valid transition type.");

            }

            Log.i(TAG, "handleTransition: A transition occurred at " + geofenceLocationString);

            // Create custom notification using location and transition strings
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_CODE,
                    createNotification(geofenceLocationString, geofenceTransitionString));
        }
    }
}
