package com.alexstyl.locationreminder.ui.fragment;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.alexstyl.locationreminder.BuildConfig;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.service.FetchAddressIntentService;
import com.alexstyl.locationreminder.ui.BaseFragment;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class AddressFetcherFragment extends BaseFragment {

    private EditText mAddressField;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
//            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                Toast.makeText(getActivity(), mAddressOutput, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResultReceiver = new AddressResultReceiver(new Handler());
        if(!BuildConfig.DEBUG){
            // this is a debug fragment
            throw new RuntimeException();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        this.mAddressField = (EditText) view.findViewById(R.id.address_field);
        view.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntentService();
            }
        });
        return view;
    }

    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        mLastLocation = new Location(mAddressField.getText().toString());
        mLastLocation.setLatitude(52.476660);
        mLastLocation.setLongitude(-1.898708);

        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        getActivity().startService(intent);
    }
}
