package com.alexstyl.locationreminder;

import android.content.Context;

import com.alexstyl.locationreminder.entity.LocationPlace;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The Revisitor keeps a list of {@linkplain com.alexstyl.locationreminder.entity.LocationPlace} with their names and co-ords, in order to be used through the app.
 * <p>Created by alexstyl on 22/02/15.</p>
 */
final public class Revisitor {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String FILE_NAME = "visited_places.dat";
    private static final String TAG = "Revisitor";
    private static Revisitor sInstance;

    private Context mContext;
    private List<LocationPlace> mExistingLocationPlaces;

    private Revisitor(Context context) {
        this.mContext = context;
        this.mExistingLocationPlaces = new ArrayList<>();
        init();
    }

    public static Revisitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Revisitor(context);
        }
        return sInstance;
    }

    private void init() {
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<LocationPlace> read = (ArrayList<LocationPlace>) is.readObject();
            this.mExistingLocationPlaces.addAll(read);
            is.close();
            fis.close();

        } catch (IOException fx) {
            DeLog.d(TAG, "Previous stored currencies file not found");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (DEBUG && mExistingLocationPlaces.isEmpty()) {
            addPlace("University of Birmingham", 52.450817, -1.930514, "University of Birmingham\n" +
                    "Edgbaston\n" +
                    "Birmingham, West Midlands B15 2TT");
            addPlace("Tesco (Selly)", 52.4462654, -1.9310574, "Selly Oak Bristol Road Esso Express\n" +
                    "479 Bristol Road\n" +
                    "Birmingham\n" +
                    "Bournbrook, West Midlands B29 6BA");
        }

    }

    private void addPlace(String name, double latitude, double longtitude, String address) {
        addPlace(new LocationPlace(name, latitude, longtitude, address));
    }

    /**
     * Adds a new place in the Revisitor. Places with the same name or same co-ords as the given will be removed.
     */
    public void addPlace(LocationPlace newPlace) {
        int length = mExistingLocationPlaces.size();
        for (int i = length - 1; i >= 0; i--) {
            LocationPlace oldPlace = mExistingLocationPlaces.get(i);

            if (oldPlace.equals(newPlace) || oldPlace.toString().equals(newPlace.toString())) {
                LocationPlace placeRemoved = this.mExistingLocationPlaces.remove(i);
                DeLog.v(TAG, "Removed " + placeRemoved + " from cache");

            }
        }

        this.mExistingLocationPlaces.add(newPlace);
        DeLog.v(TAG, "Added " + newPlace + " to cache");
        saveToFileAsync();
        DeLog.v(TAG, "Saved cache to file");


    }

    private Runnable mSaveRunnable = new Runnable() {
        @Override
        public void run() {
            saveToFile();
        }
    };

    private void saveToFileAsync() {
        // TODO create a queue and only save the newest occurance
        new Thread(mSaveRunnable, TAG + "-saving").start();
    }

    private void saveToFile() {
        try {
            FileOutputStream fileOut = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(mExistingLocationPlaces);
            out.close();
            fileOut.close();
            DeLog.d(TAG, "Saved places to file");
        } catch (IOException e) {
            DeLog.log(TAG, "Saving places failed", e);
        }
    }

    /**
     * Returns a previously used Place, starting with the given prefix
     *
     * @param prefix
     */
    public LocationPlace getLocationfrom(String prefix) {

        for (LocationPlace place : mExistingLocationPlaces) {
            if (place.toString().startsWith(prefix)) {
                return place;
            }
        }
        return null;
    }

    public List<LocationPlace> getAllPlaces() {
        return new ArrayList<>(mExistingLocationPlaces);
    }

    public String getAddressFrom(double latitude, double longtitude) {
        for (LocationPlace place : mExistingLocationPlaces) {
            if (place.compareDistance(latitude, longtitude)) {
                return place.toString();
            }
        }
        return null;
    }
}

//52.46090064724269,-1.9242717325687406
//mLatitude = 52.46090064724269
//        mLongtitude = -1.9242717325687406

//52.4609006472427, -1.92427173256874
//latitude = 52.4609006472427
//        longtitude = -1.92427173256874