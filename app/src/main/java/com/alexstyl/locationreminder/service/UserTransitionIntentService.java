package com.alexstyl.locationreminder.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.Notifier;
import com.alexstyl.locationreminder.db.DBHelper;
import com.alexstyl.locationreminder.entity.StoredReminder;
import com.alexstyl.locationreminder.util.Utils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * <p>Created by alexstyl on 01/03/15.</p>
 */
public class UserTransitionIntentService extends IntentService {
    private static final String TAG = "UserTransitionService";
    public static final String EXTRA_FEATURES = App.PACKAGE + ".features";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UserTransitionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        DeLog.d(TAG, "onHandleIntent");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);


        if (event.hasError()) {
            // TODO: Handle error
            DeLog.e(TAG, "ERROR in Geofence! " + event.getErrorCode());
        } else {
            handleGeofences(event.getTriggeringLocation(), event.getTriggeringGeofences(), event.getGeofenceTransition(), intent.getExtras().getInt(EXTRA_FEATURES, -1));
            //TODO mute and unmute device
        }

    }

    private void handleGeofences(Location location, List<Geofence> geoFences, int transition, int features) {

        for (Geofence fence : geoFences) {

            StoredReminder reminder = DBHelper.getInstance(this).getReminder(Long.valueOf(fence.getRequestId()));
            boolean dndEnabled = (features & StoredReminder.FEATURE_FLAG_MUTE) == StoredReminder.FEATURE_FLAG_MUTE;
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Notifier.notifyForReminder(this, reminder);
                if (dndEnabled) {
                    Notifier.notifyDnDFor(this, reminder);
                    Utils.muteDevice(this, false);
                }
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Notifier.cancelDndfor(this, reminder.getID());

                if (dndEnabled) {
                    Utils.muteDevice(this, true);
                }
            }
            DeLog.d(TAG, "Handled reminder: " + reminder);
        }

    }
}
