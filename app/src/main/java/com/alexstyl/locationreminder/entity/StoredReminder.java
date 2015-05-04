package com.alexstyl.locationreminder.entity;

import android.content.Context;
import android.location.Location;

/**
 * <p>Created by alexstyl on 23/02/15.</p>
 */
public class StoredReminder implements Comparable<StoredReminder> {

    /**
     * Feature flag: Mute
     * </br> When the reminder is fired. It will mute the device
     */
    public static final int FEATURE_FLAG_MUTE = 0x00000001;

    /**
     * The range (in meters) of the geofence that fires the reminder;
     */
    public static final float DEFAULT_RADIUS = 50;

    private long id;
    private LocationPlace mLocation;
    private String mNote;

    private long mDateCreated;

    public StoredReminder(long id, String note, double latitude, double longtitdude, long dateModified, String address) {
        this(id, note, null, latitude, longtitdude, dateModified, address);
    }

    public StoredReminder(long id, String note, String placeName, double latitude, double longtitdude, long dateModified, String address) {
        this.id = id;
        this.mNote = note;
        this.mLocation = new LocationPlace(placeName, latitude, longtitdude, address);
        this.mDateCreated = dateModified;
    }


    @Override
    public String toString() {
        return mNote;
    }

    /**
     * Returns the note of the Reminder
     */
    public String getNote() {
        return mNote;
    }

    public String getPrettyLocation(Context mContext) {
        return mLocation.getPrettyLocation(mContext);
    }

    public long getID() {
        return id;
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLongtitude() {
        return mLocation.getLongtitude();
    }

    public String getAddress() {
        return mLocation.getAddress();
    }

    public Location makeLocation() {
        Location mLocation = new Location("user");
        mLocation.setLatitude(getLatitude());
        mLocation.setLongitude(getLongtitude());
        return mLocation;
    }


    public long getDateModified() {
        return mDateCreated;
    }

    @Override
    public int compareTo(StoredReminder another) {
        return (int) (another.mDateCreated - mDateCreated);
    }
}
