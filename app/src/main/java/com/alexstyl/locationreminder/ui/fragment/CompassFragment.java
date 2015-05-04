package com.alexstyl.locationreminder.ui.fragment;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.CompassView;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.db.DBHelper;
import com.alexstyl.locationreminder.entity.StoredReminder;
import com.alexstyl.locationreminder.ui.BaseFragment;
import com.alexstyl.locationreminder.ui.activity.CompassActivity;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class CompassFragment extends BaseFragment {

    private static final String TAG = "CompassFragment";
    private static final String KEY_DESTINATION_REACHED = App.PACKAGE + ".destination_reached";
    private static final String KEY_BEARING = App.PACKAGE + ".bearing";
    // LocationManager and LocationListener reference
    private LocationManager mLocationManager;

    private static final int DISTANCE_THRESHOLD = 2;
    private Location mCurrentPosition;
    private long MEASURE_TIME = 2;
    LocationListener mLocationListener;

    // Sensors & SensorManager
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    // Storage for sensor readings
    private float[] mGravity;
    private float[] mGeomagnetic;


    private StoredReminder mReminder;
    private Location mTargetLocation;
    private Float mBearing;
    private boolean mDestinationReached;

    private TextView mDistanceView;
    private CompassView mCompassView;
    private LinearLayout mLoadingView;

    boolean compassHidden = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long reminderID = getArguments().getLong(CompassActivity.EXTRA_REMINDER_ID);
        this.mReminder = DBHelper.getInstance(getActivity()).getReminder(reminderID);
        this.mTargetLocation = new Location("user");
        this.mTargetLocation.setLatitude(mReminder.getLatitude());
        this.mTargetLocation.setLongitude(mReminder.getLongtitude());

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_BEARING)) {
                mBearing = savedInstanceState.getFloat(KEY_BEARING);
            }
            mDestinationReached = savedInstanceState.getBoolean(KEY_DESTINATION_REACHED);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_DESTINATION_REACHED, mDestinationReached);
        if (mBearing != null) {
            outState.putFloat(KEY_BEARING, mBearing);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        mCompassView = (CompassView) view.findViewById(R.id.compass);
        TextView mTargetName = (TextView) view.findViewById(R.id.target_name);
        this.mDistanceView = (TextView) view.findViewById(R.id.distance);
        mTargetName.setText(mReminder.getPrettyLocation(getActivity()));
        this.mLoadingView = (LinearLayout) view.findViewById(R.id.loading);
        return view;
    }


    /**
     * Updates the distances text to the distance between the reminder location and the given position
     *
     * @param position The position to calculate the distance of
     * @return The distance between the two points
     */
    private float updateDistanceText(Location position) {
        float distance = position.distanceTo(mTargetLocation);
        if (distance < 500) {
            mDistanceView.setText(getString(R.string.distance, distance));
        } else {
            mDistanceView.setText(getString(R.string.distance_too_far));
        }


        mBearing = position.bearingTo(mTargetLocation);
        DeLog.d(TAG, "bearing:" + mBearing);
        return distance;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (null == (mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE))) {
            getActivity().finish();
        }
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                DeLog.d(TAG, "onLocationChanged()!");
                mCurrentPosition = location;
                float distance = updateDistanceText(location);
                if (distance < DISTANCE_THRESHOLD) {
                    onReachedToPoint();
                }
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Exit unless both sensors are available
        if (null == mAccelerometer || null == mMagnetometer) {
            Log.d(TAG, "Exiting, sensors not available");
            getActivity().finish();
        }
    }


    /**
     * Called when the user has reached their destination
     */
    private void onReachedToPoint() {
        DeLog.d(TAG, "Reached destination!");
        mDistanceView.setText(R.string.destination_reached);
        mLocationManager.removeUpdates(mLocationListener);
        DeLog.d(TAG, "Unregistered Listeners!");
        mCompassView.setVisibility(View.GONE);
        unregisterSensors(); // no need to operate the compass anymore
    }

    private static final float MIN_ACCURACY = 0.6f;
    private static final long TWO_MINUTES = 2 * DateUtils.MINUTE_IN_MILLIS;
    private static final long POLLING_FREQ = 2 * DateUtils.SECOND_IN_MILLIS;
    private static final float MIN_DISTANCE = 2;//meters


    @Override
    public void onResume() {
        super.onResume();


        if (mCurrentPosition == null) {
            // we dont have a position yet. Show the loading, hide the compass
            mCompassView.setVisibility(View.INVISIBLE);
            mLoadingView.setVisibility(View.VISIBLE);
            compassHidden = true;
            registerSensors();
        } else {
            // we do have a position
            if (mDestinationReached) {
                compassHidden = true;
                mCompassView.setVisibility(View.INVISIBLE);
            } else {
                if (mBearing != -1) {
                    mCompassView.setVisibility(View.VISIBLE);
                    compassHidden = false;
                }
                registerSensors();
            }
        }

    }

    private void registerSensors() {
        // Start listening for sensor updates
        mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Register for GPS location updates
        if (null != mLocationManager.getProvider(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, POLLING_FREQ, MIN_DISTANCE, mLocationListener);
        }

        if (null != mLocationManager .getProvider(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
                    MIN_DISTANCE, mLocationListener);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterSensors();
    }

    private void unregisterSensors() {
        // stop listening to changes if the activity is on the background
        mLocationManager.removeUpdates(mLocationListener);
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public static CompassFragment newInstance(long aLong) {
        CompassFragment fragment = new CompassFragment();
        Bundle args = new Bundle(1);
        args.putLong(CompassActivity.EXTRA_REMINDER_ID, aLong);
        fragment.setArguments(args);
        return fragment;
    }


    SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isAdded()) {
                return;
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // capture accelerometer data
                mGravity = new float[3];
                System.arraycopy(event.values, 0, mGravity, 0, 3);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // capture magnetometer data
                mGeomagnetic = new float[3];
                System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);
            }

            // before calculating the direction of the location, make sure we have the readings
            // for both sensors and our geolocation
            if (mBearing != null && mGravity != null && mGeomagnetic != null) {
                if (compassHidden) {
                    mCompassView.setVisibility(View.VISIBLE);
                    mLoadingView.setVisibility(View.GONE);
                    compassHidden = false;
                }
                float rotationMatrix[] = new float[9];

                boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic);

                if (success) {
                    // Also compensate for device orientation
                    // Not necessary if we keep the device in its natural orientation (e.g., portrait)
                    // See: http://stackoverflow.com/questions/18782829/android-sensormanager-strange-how-to-remapcoordinatesystem/20759898#20759898

                    // Get display rotation
                    Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int rotation = display.getRotation();

                    // Initialize axes for remapCoordinateSystem
                    int axisX = SensorManager.AXIS_X;
                    int axisY = SensorManager.AXIS_Y;

                    // Determine axes for remapping
                    switch (rotation) {
                        // No rotation, typically portrait
                        case Surface.ROTATION_0:
                            axisX = SensorManager.AXIS_X;
                            axisY = SensorManager.AXIS_Y;
                            break;
                        // Landscape typically
                        case Surface.ROTATION_90:
                            axisX = SensorManager.AXIS_Y;
                            axisY = SensorManager.AXIS_MINUS_X;
                            break;
                        // Portrait upside down typically
                        case Surface.ROTATION_180:
                            axisX = SensorManager.AXIS_MINUS_X;
                            axisY = SensorManager.AXIS_MINUS_Y;
                            break;
                        // Landscape upside down typically
                        case Surface.ROTATION_270:
                            axisX = SensorManager.AXIS_MINUS_Y;
                            axisY = SensorManager.AXIS_X;
                            break;
                    }

                    // Remap coordinate system
                    float rotationMatrixB[] = new float[9];
                    success = SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, rotationMatrixB);

                    if (success) {
                        float orientationMatrix[] = new float[3];

                        // Returns the device's orientation given the rotation matrix
                        // Note: if we did not account for display orientation,
                        // we would have just used rotationMatrix here
                        SensorManager.getOrientation(rotationMatrixB, orientationMatrix);

                        // Get the rotation in radians around the Z-axis (Y points to magnetic North Pole)
                        // Note: we assume the device is held flat and parallel to ground!
                        // See: http://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])
                        // See: http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])
                        float rotationInRadians = orientationMatrix[0];

                        // Convert from radians to degrees
                        double mRotationInDegrees = Math.toDegrees(rotationInRadians);

                        if (mCurrentPosition != null) {
                            GeomagneticField geoField = new GeomagneticField(
                                    (float) mCurrentPosition.getLatitude(),
                                    (float) mCurrentPosition.getLongitude(),
                                    (float) mCurrentPosition.getAltitude(),
                                    System.currentTimeMillis());
                            mRotationInDegrees += geoField.getDeclination(); // converts magnetic north into true north
                        }
                        double direction = mRotationInDegrees - mBearing;

                        mCompassView.setRotationDegrees(direction);
                        // Reset sensor event data arrays
                        mGravity = mGeomagnetic = null;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}