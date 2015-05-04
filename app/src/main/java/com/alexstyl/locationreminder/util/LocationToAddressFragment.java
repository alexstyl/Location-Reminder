package com.alexstyl.locationreminder.util;

import android.os.AsyncTask;
import android.os.Bundle;

import com.alexstyl.locationreminder.ui.BaseFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Worker Fragment that
 * <p>Created by alexstyl on 28/02/15.</p>
 */
public class LocationToAddressFragment extends BaseFragment {

    public interface OnLocationConverteredListener {

        /**
         * Called when
         *
         * @param address
         * @param requested
         */
        public void onLocationConverted(String address, LatLng requested);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private class LocationToAddressTask extends AsyncTask<LatLng, Void, String> {
        @Override
        protected String doInBackground(LatLng... params) {
            return null;
        }
    }

}
