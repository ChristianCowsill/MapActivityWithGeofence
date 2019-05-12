package com.cowsill.maptutorial;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class GeofenceTransitionsService extends IntentService {

    public static final String TAG = "GeofenceTransitionService";

    String geofenceLocation;
    String geofenceTransitionType;

    public GeofenceTransitionsService() {
        super(TAG);
    }

    // TO DO:  Collect geofence event data from intent and produce custom notification
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    private Notification createNotification(String geofenceLocation, String geofenceTransitionType){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                MapTutorialApplication.getLocationChannelID())
                .setSmallIcon(R.drawable.ic_location_notification_icon)
                .setContentTitle("Welcome to " + geofenceLocation)
                .setContentText("You just " + geofenceTransitionType + " " + geofenceLocation + ".")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
}
