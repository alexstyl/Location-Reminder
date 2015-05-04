package com.alexstyl.locationreminder.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.util.AddressHelper;

/**
 * <p>Created by alexstyl on 28/02/15.</p>
 */
public class FetchLocationIntentService extends IntentService {

    private static final String TAG = "FetchLocationIntentService";

    public FetchLocationIntentService() {
        super(TAG);
    }

    private ResultReceiver mReceiver;

    @Override
    protected void onHandleIntent(Intent intent) {


        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        if (mReceiver == null) {
            DeLog.w(TAG, "Bailing - no one is listening to me :(");
            return;
        }
        String address = intent.getStringExtra(Constants.EXTRA_ADDRESS);
        Location location = AddressHelper.convertToLocation(this, address);
        if (location != null) {
            deliverResultToReceiver(location.getLatitude(), location.getLongitude());
        } else {
            deliverFailure();
        }

    }

    private void deliverResultToReceiver( double lat, double lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.RESULT_LATITUDE, lat);
        bundle.putDouble(Constants.RESULT_LONGTITUDE, lng);
        mReceiver.send(Constants.SUCCESS_RESULT, bundle);
    }

    private void deliverFailure() {
        Bundle bundle = new Bundle();
        mReceiver.send(Constants.FAILURE_RESULT, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME = App.PACKAGE;
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_LATITUDE = PACKAGE_NAME + ".RESULT_LATITUDE";
        public static final String RESULT_LONGTITUDE = PACKAGE_NAME + ".RESULT_LONGTITUDE";
        public static final String EXTRA_ADDRESS = PACKAGE_NAME +
                ".EXTRA_ADDRESS";
    }
}
