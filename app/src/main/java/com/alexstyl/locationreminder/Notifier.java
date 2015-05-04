package com.alexstyl.locationreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.alexstyl.locationreminder.entity.StoredReminder;
import com.alexstyl.locationreminder.ui.activity.CompassActivity;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
final public class Notifier {

    private static final String NOTIFY_TAG_DND = "notify_dnd";
    private static Notifier sInstance;

    private Notifier() {
    }

    private NotificationManager mManager;
    private Context mContext;

    private Notifier(Context context) {
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mContext = context;

    }

    public Notifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Notifier(context);
        }
        return sInstance;
    }


    public static void notifyForReminder(Context context, StoredReminder reminder) {
        // star the CompassActivity
        Intent notificationIntent = new Intent(context, CompassActivity.class);
        notificationIntent.putExtra(CompassActivity.EXTRA_REMINDER_ID, reminder.getID());
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(reminder.getPrettyLocation(context))
                .setContentText(reminder.getNote())
                .setAutoCancel(false)
                .setColor(context.getResources().getColor(R.color.primary))
                .setTicker(reminder.getNote())
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_stat_maps_location_history)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) reminder.getID(), notification);
    }

    public static void cancelForReminder(Context context, long id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel((int) id);
    }

    /**
     * Cancels the DnD notification for the reminder of the given id
     *
     * @param context The context to use
     * @param reminderID The id of the reminder to cancel the DND for
     */
    public static void cancelDndfor(Context context, long reminderID) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFY_TAG_DND, (int) reminderID);
    }

    /**
     * Generates a DnD notification to inform the user that the dnd feature has been enabled
     * @param context
     * @param reminder
     */
    public static void notifyDnDFor(Context context, StoredReminder reminder) {
        Intent notificationIntent = new Intent(context, CompassActivity.class);
        notificationIntent.putExtra(CompassActivity.EXTRA_REMINDER_ID, reminder.getID());
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getString(R.string.dnd_enabled))
                .setContentText(context.getString(R.string.near_place, reminder.getPrettyLocation(context)))
                .setAutoCancel(false)
                .setTicker(reminder.getNote())
                .setColor(context.getResources().getColor(android.R.color.holo_red_dark))
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_stat_av_volume_off)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFY_TAG_DND, (int) reminder.getID(), notification);
    }
}
