package com.alexstyl.locationreminder.ui.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.BuildConfig;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.service.FetchAddressIntentService;
import com.alexstyl.locationreminder.ui.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An activity showing an map that the user can select a point.
 * </br>By calling this activity for result, the returning intent will provide the location selected in the {@linkplain com.alexstyl.locationreminder.ui.activity.PointSelectionActivity#EXTRA_LATITUDE} and
 * {@linkplain com.alexstyl.locationreminder.ui.activity.PointSelectionActivity#EXTRA_LONGTITUDE} for the coordinates. The result might also include the value {@linkplain com.alexstyl.locationreminder.ui.activity.PointSelectionActivity#EXTRA_ADDRESS}
 * which is a human readable representation of the address (Internet connection is required in order to populate this value, hence the reason why this value <i>might not be present</i> in the returning bundle).
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class PointSelectionActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PointSelectionActivity";

    public static final String ACTION_RESULT = App.PACKAGE + ".RESULT_ACTION";
    public static final String EXTRA_LONGTITUDE = App.PACKAGE + ".EXTRA_LONGTITUDE";
    public static final String EXTRA_ADDRESS = App.PACKAGE + ".EXTRA_ADDRESS";
    public static final String EXTRA_LATITUDE = App.PACKAGE + ".EXTRA_LATITUDE";

    private static final String KEY_HUMAN_ADDRESS = App.PACKAGE + ".human_address";
    /**
     * LocationManager location provider
     */
    private static final String PROVIDER_MAP = "point_selection_activity";


    /**
     * The human readable address for the pointed place
     * </br> Can be null after the user selects the place, since the devince might not have an internet connection
     */
    private String mHumanAddress;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private TextView mAddressView;

    private AddressResultReceiver mResultReceiver;


    class AddressResultReceiver extends ResultReceiver {

        private static final String TAG_ADDRESS = TAG + "/adress";

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (isFinishing()) {
                DeLog.w(TAG_ADDRESS, "Activity is finishing - Skipping handling of received result");
                return;
            }

            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mHumanAddress = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            } else {
                // we failed the retrieve the address for the new location... clear the current address
                mHumanAddress = null;
            }
            updateAddressLabel();

        }
    }

    /**
     * Updates the address TextView of the activity.
     * If we know the real address of the place, the TextView will display that;
     * if we don't, the coords are going to be set instead.
     */
    private void updateAddressLabel() {
        String label = mHumanAddress;
        if (TextUtils.isEmpty(mHumanAddress)) {
            LatLng latLng = mMarker.getPosition();
            label = String.format("(%f, %f)", latLng.latitude, latLng.longitude);
        }
        mAddressView.setText(label);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_select);
        findViewById(R.id.btn_select).setOnClickListener(mSelectBtnListener);
        findViewById(R.id.btn_cancel).setOnClickListener(mCancelBtnClickListener);
        mAddressView = (TextView) findViewById(R.id.address_field);


        if (savedInstanceState != null) {
            mHumanAddress = savedInstanceState.getString(KEY_HUMAN_ADDRESS);
        }
        buildGoogleApiClient();
        mResultReceiver = new AddressResultReceiver(new Handler());
    }


    View.OnClickListener mCancelBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // we didn't select any points.
            setResult(RESULT_CANCELED);
            finish();
        }
    };
    private View.OnClickListener mSelectBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // pass the selected position to the intent and finish the activity
            double lat = mMarker.getPosition().latitude;
            double lng = mMarker.getPosition().longitude;

            Intent data = new Intent(ACTION_RESULT);
            data.putExtra(EXTRA_LONGTITUDE, lng);
            data.putExtra(EXTRA_LATITUDE, lat);
            if (!TextUtils.isEmpty(mHumanAddress)) {
                data.putExtra(EXTRA_ADDRESS, mHumanAddress);
            }
            setResult(RESULT_OK, data);
            finish();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private Marker mMarker;

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarkerPosition(latLng);

            }
        });
    }

    protected void convertToAddress(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);

        Location mLastLocation = new Location(PROVIDER_MAP);
        mLastLocation.setLatitude(latLng.latitude);
        mLastLocation.setLongitude(latLng.longitude);

        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null && BuildConfig.DEBUG) {
            mLastLocation = new Location("debug");
            mLastLocation.setLatitude(52.450817);
            mLastLocation.setLongitude(-1.930514);
        }

        if (mLastLocation != null) {
            DeLog.w(TAG, "Last Location found" + mLastLocation);
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .zoom(13)
                    .bearing(90)
                    .build();

            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    2000, null);
            setMarkerPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        } else if (BuildConfig.DEBUG) {
            DeLog.w(TAG, "No Last location found");
        }
        mGoogleApiClient.disconnect();
    }


    protected void setMarkerPosition(LatLng latLng) {

        //TODO add some loading here
        mMarker.setPosition(latLng);
        convertToAddress(latLng);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO
        Log.d(TAG, "connection suspended!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO
        Log.d(TAG, "connection failed!" + result);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_HUMAN_ADDRESS, mHumanAddress);
    }
}
