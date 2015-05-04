package com.alexstyl.locationreminder.db;

import android.provider.BaseColumns;

/**
 * <p>Created by alexstyl on 23/02/15.</p>
 */
final public class RemindersContract implements BaseColumns {

    public static final String TABLE_NAME = "reminders";

    public static final String NOTE = "note";

    public static final String LONGTITUDE = "longtitude";
    public static final String LATITUDE = "latitude";


    /**
     * The human readable address of the reminder.
     */
    public static final String ADDRESS = "address";


    public static final String FEATURES = "features";

    /**
     * The date the reminder was set or edited
     */
    public static final String DATE_MOTIFIED = "date_modified";
}
