package com.alexstyl.locationreminder.util;

/**
 * <p>Created by alexstyl on 23/02/15.</p>
 */
final public class LocationUtils {
    public static String createLocationString(double latitude, double longtitude) {
        return String.format("(%f, %f)", latitude, longtitude);
    }
}
