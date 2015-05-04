package com.alexstyl.locationreminder.entity;

import android.content.Context;
import android.location.Location;

import com.alexstyl.locationreminder.Revisitor;

import java.io.Serializable;

/**
 * A LocationPlace consists of a the location and the name of the place.
 * </br>i.e. Starbucks, (12.12938 , 42.239192)
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class LocationPlace implements Serializable {
    private String mNameOfPlace;
    private double mLatitude;
    private double mLongtitude;
    private String mAddress;

    public LocationPlace(String name, Location location, String address) {
        this(name, location.getLatitude(), location.getLongitude(), address);
    }

    public LocationPlace(String name, double latitude, double longtitude, String address) {
        this.mNameOfPlace = name;
        this.mLatitude = latitude;
        this.mLongtitude = longtitude;
        this.mAddress = address;
    }

    @Override
    public String toString() {
        return mNameOfPlace;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongtitude() {
        return mLongtitude;
    }

    public String getAddress() {
        return mAddress;
    }

    /**
     * Compares whether this reminder is equal to the given object.
     * </br> Two LocationPlaces are considered equal only if their co-ords are equal.
     * </br> The human readable address is not compared
     *
     * @param o The object to compare
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LocationPlace)) {
            return false;
        }
        LocationPlace other = (LocationPlace) o;
        return mLatitude == other.getLatitude()
                && mLongtitude == other.getLongtitude();
    }

    @Override
    public int hashCode() {
        //TODO this might not be unique
        return (int) (mLongtitude * 100 + mLatitude);
    }

    public String getPrettyLocation(Context mContext) {
        if (mNameOfPlace == null) {
            mNameOfPlace = Revisitor.getInstance(mContext).getAddressFrom(mLatitude, mLongtitude);
        }
        if (mNameOfPlace == null) {
            return mAddress;
        }
        return mNameOfPlace;
    }

    //    place.getLatitude() == latitude &&
//            place.getLongtitude() == longtitude
    private static final double ACCURACY = 1e-8;


    /**
     * Compares the distance to the given point.
     * </br>If the distances between the two points is less that {@value #ACCURACY}, the locations are considered to be in the same place
     *
     * @return
     */
    public boolean compareDistance(double latitude, double longtitude) {
        return Math.abs(mLatitude - latitude) < ACCURACY
                && Math.abs(mLongtitude - longtitude) < ACCURACY;


    }
}
