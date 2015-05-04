package com.alexstyl.locationreminder.ui.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.Revisitor;
import com.alexstyl.locationreminder.db.DBHelper;
import com.alexstyl.locationreminder.entity.LocationPlace;
import com.alexstyl.locationreminder.entity.StoredReminder;
import com.alexstyl.locationreminder.service.FetchAddressIntentService;
import com.alexstyl.locationreminder.service.FetchLocationIntentService;
import com.alexstyl.locationreminder.service.UserTransitionIntentService;
import com.alexstyl.locationreminder.ui.BaseFragment;
import com.alexstyl.locationreminder.ui.activity.PointSelectionActivity;
import com.alexstyl.locationreminder.ui.dialog.ProgressDialog;
import com.alexstyl.locationreminder.ui.fragment.adapter.PreviousPlacesAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class NewReminderFragment extends BaseFragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_MAP = 500;
    private static final String TAG = "NewReminderFragment";
    private static final String FM_PROGRESS_DIALOG = "alexstyl:progress_dialog";
    private static final int RADIUS_MAX = 500;
    EditText mNote;
    AutoCompleteTextView mPlace;
    TextView mAddress;
    private CheckBox mDnDToggle;

    ImageView mPinpoint;
    private TextView mUnsupportedActionsLabel;
    private RelativeLayout mAvailableActions;
    private PreviousPlacesAdapter mAutoCompleteAdapter;
    private SeekBar mRadius;
    private TextView mRadiusText;


    private String KEY_LAT = App.PACKAGE + ".latitude";
    private String KEY_LNG = App.PACKAGE + ".longtitude";
    private String KEY_ID = App.PACKAGE + ".id";

    private double mLatitude;
    private double mLongtitude;
    private long mExistingID = -1l;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(KEY_LAT, mLatitude);
        outState.putDouble(KEY_LNG, mLongtitude);
        outState.putLong(KEY_ID, mExistingID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAddressReceiver = new AddressResultReceiver(new Handler());

        if (savedInstanceState != null) {
            this.mLatitude = savedInstanceState.getDouble(KEY_LAT);
            this.mLongtitude = savedInstanceState.getDouble(KEY_LNG);
            this.mExistingID = savedInstanceState.getLong(KEY_ID);

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setExistingReminder(mExistingID);

        if (savedInstanceState == null) {
            this.mRadius.setProgress((int) StoredReminder.DEFAULT_RADIUS);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new_reminder, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_reminder_list, container, false);

        mNote = (EditText) view.findViewById(R.id.note);
        mPlace = (AutoCompleteTextView) view.findViewById(R.id.place_name);
        mPinpoint = (ImageView) view.findViewById(R.id.pinpoint_btn);
        mAddress = (TextView) view.findViewById(R.id.place_address);
        mDnDToggle = (CheckBox) view.findViewById(R.id.check_mute);
        mAvailableActions = (RelativeLayout) view.findViewById(R.id.available_actions);
        this.mUnsupportedActionsLabel = (TextView) view.findViewById(R.id.unsupported_actions_label);

        this.mRadius = (SeekBar) view.findViewById(R.id.radius_slider);
        this.mRadiusText = (TextView) view.findViewById(R.id.radius_text);
        this.mRadius.setMax(RADIUS_MAX);
        updateRadiusText();

        this.mRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                updateRadiusText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if (getBaseActivity().getSupportActionBar() != null) {
            // the activity is being displayed as a dialog
            view.findViewById(R.id.btn_bar).setVisibility(View.GONE);
        } else {

            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelButtonPressed();
                }
            });
            view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSaveButtonPressed();

                }
            });
        }

        mPinpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PointSelectionActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
            }
        });


        return view;
    }

    private void updateRadiusText() {
        int meters = mRadius.getProgress();
        this.mRadiusText.setText(getString(R.string.radius_in_m, meters));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            DeLog.d(TAG, "Received result: " + data);
            if (isAdded()) {
                Bundle extras = data.getExtras();

                double latitude = data.getDoubleExtra(PointSelectionActivity.EXTRA_LATITUDE, -1d);
                double longitude = data.getDoubleExtra(PointSelectionActivity.EXTRA_LONGTITUDE, -1d);
                String address = null;
                if (extras.containsKey(PointSelectionActivity.EXTRA_ADDRESS)) {
                    address = data.getStringExtra(PointSelectionActivity.EXTRA_ADDRESS);
                } else {
                    // TODO create some sort of a manager that translated location to address on the background
                    address = String.format("(%f, %f)", latitude, longitude);
                }

                this.mLongtitude = longitude;
                this.mLatitude = latitude;
                this.mAddress.setText(address);
            }
        } else {
            DeLog.w(TAG, "Ignored result for " + requestCode + "," + resultCode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onSaveButtonPressed();
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Called when the user hit the save button.
     */
    private void onSaveButtonPressed() {
        String note = mNote.getText().toString();
        String address = mAddress.getText().toString();
        boolean error = false;

        // first, check for any empty input
        if (TextUtils.isEmpty(note)) {
            mNote.setError(getString(R.string.error_no_note));
            error = true;
        }

        if (TextUtils.isEmpty(address)) {
            mAddress.setError(getString(R.string.error_no_address));
            error = true;
        }

        if (error) {
            return;
        }

        String name = mPlace.getText().toString();

        if (!TextUtils.isEmpty(name)) {
            // if the name of the place isn't left empty, save it
            Revisitor.getInstance(getActivity()).addPlace(new LocationPlace(name, mLatitude, mLongtitude, address));
            DeLog.d(TAG, String.format("Adding place: %f, %f", mLatitude, mLongtitude));
        }

        // all the data are in place. we need to check if the address is still valid
        // meaning if the address exists.
        setGeoFence();
    }


    /**
     * Starts the process of setting the geofence. Once that is done, the reminder is stored in the database
     */
    private void setGeoFence() {
        displayProgressDialog();
        setUpApi();

    }

    private void displayProgressDialog() {
        ProgressDialog dialog = (ProgressDialog) getFragmentManager().findFragmentByTag(FM_PROGRESS_DIALOG);
        if (dialog == null) {
            dialog = new ProgressDialog();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), FM_PROGRESS_DIALOG);
            DeLog.d(TAG, "Displaying Progress Dialog");
        }
    }

    private void hideProgressDialog() {
        ProgressDialog dialog = (ProgressDialog) getFragmentManager().findFragmentByTag(FM_PROGRESS_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
            DeLog.d(TAG, "Hiding Progress Dialog");
        }
    }

    /**
     * Starts a service that converts the given address to a Location object
     *
     * @param address
     */
    private void startLocationService(String address) {
        if (mLocationReceiver == null) {
            mLocationReceiver = new LocationResultReceiver(new Handler());
        }
        Intent intent = new Intent(getActivity(), FetchLocationIntentService.class);
        intent.putExtra(FetchLocationIntentService.Constants.RECEIVER, mLocationReceiver);
        intent.putExtra(FetchLocationIntentService.Constants.EXTRA_ADDRESS, address);
        getActivity().startService(intent);

    }

    /**
     * Sets the data of an existing stored remidner into the fragment, in order to allow the user to edit it
     *
     * @param id
     */
    public void setExistingReminder(long id) {
        if (id != -1) {
            StoredReminder stored = DBHelper.getInstance(getActivity()).getReminder(id);
            if (stored != null) {
                this.mNote.setText(stored.getNote());
                this.mAddress.setText(stored.getAddress());
                this.mExistingID = id;
                this.mLatitude = stored.getLatitude();
                this.mLongtitude = stored.getLongtitude();
                this.mPlace.setText(Revisitor.getInstance(getActivity()).getAddressFrom(mLatitude, mLongtitude));

                this.mAvailableActions.setVisibility(View.GONE);
                this.mUnsupportedActionsLabel.setVisibility(View.VISIBLE);

                setTitle(R.string.edit_reminder);
            }
        } else {
            this.mAvailableActions.setVisibility(View.VISIBLE);
            this.mUnsupportedActionsLabel.setVisibility(View.GONE);

            setTitle(R.string.new_reminder);
        }
    }

    /**
     * Sets the title of the parent activity
     *
     * @param resString The resource ID of the title to set
     */
    private void setTitle(int resString) {
        ActionBar ab = getBaseActivity().getSupportActionBar();
        if (ab != null) {
            ab.setTitle(resString);
        }
    }

    /**
     * AsyncTask that stores the given reminder to the database
     */
    private class SaveReminderTask extends AsyncTask<Void, Void, Integer> {

        private String name;
        private double lat;
        private double log;
        private String note;
        private String address;
        private Context mContext;
        private long _id;

        public SaveReminderTask(Context context, long id, String note, String name, double lat, double log, String address) {
            this._id = id;
            this.note = note;
            this.name = name;
            this.lat = lat;
            this.log = log;
            this.address = address;
            this.mContext = context.getApplicationContext();

        }

        private static final int ACTION_REMINDER_FAILED = -1;
        private static final int ACTION_REMINDER_CREATED = 0;
        private static final int ACTION_REMINDER_UPDATED = 1;


        @Override
        protected Integer doInBackground(Void... params) {
            int actionPerformed;
            long id = _id;
            DeLog.d(TAG, String.format("Saving to DB %f, %f", lat, log));
            if (_id != -1) {
                DBHelper.getInstance(mContext).updateReminder(_id, note, name, lat, log, address);
                actionPerformed = ACTION_REMINDER_UPDATED;
            } else {
                id = DBHelper.getInstance(mContext).saveReminder(note, name, lat, log, address);
                actionPerformed = ACTION_REMINDER_CREATED;
            }
            if (id != -1) {
                // once connected, we can setup out geofences!
                Geofence cinemaFence = new Geofence.Builder()
                        .setRequestId(String.valueOf(id))
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setCircularRegion(mLatitude, mLongtitude, mRadius.getProgress())
                        .build();

                GeofencingRequest request = new GeofencingRequest.Builder()
                        .addGeofence(cinemaFence)
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .build();

                // Create an Intent pointing to the IntentService
                Intent intent = new Intent(mContext, UserTransitionIntentService.class);
                intent.putExtra(UserTransitionIntentService.EXTRA_FEATURES, getFeaturesFlag());
                PendingIntent pi = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request, pi);
            } else {
                actionPerformed = ACTION_REMINDER_FAILED;
            }

            return actionPerformed;
        }


        private int getFeaturesFlag() {
            int flag = 0;
            if (mDnDToggle.isChecked()) {
                flag = flag | StoredReminder.FEATURE_FLAG_MUTE;
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Integer action) {

            if (action == ACTION_REMINDER_CREATED) {
                Toast.makeText(mContext, R.string.reminder_created, Toast.LENGTH_SHORT).show();
            } else if (action == ACTION_REMINDER_UPDATED) {
                Toast.makeText(mContext, R.string.reminder_updated, Toast.LENGTH_SHORT).show();
            } else if (action == ACTION_REMINDER_FAILED) {
                Toast.makeText(mContext, R.string.reminder_creation_failed, Toast.LENGTH_SHORT).show();
            }

            App.broadcastOnReminderCreated(mContext);
            mGoogleApiClient.disconnect();
            mContext = null;

        }
    }

    /**
     * Called when the user pressed the cancel button.
     * Calling this method caused the activity to exit.
     */
    private void onCancelButtonPressed() {
        // the cancel button was clicked
        // just finish the activity and leave
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        mAutoCompleteAdapter.refreshPlaces();
    }


    //// Location Stuff

    private AddressResultReceiver mAddressReceiver;
    private LocationResultReceiver mLocationReceiver;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (!isAdded()) {
                return;
            }
            String mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mPlace.setText(mAddressOutput);
            } else if (resultCode == FetchAddressIntentService.Constants.FAILURE_RESULT) {
                //

            }

        }
    }

    class LocationResultReceiver extends ResultReceiver {
        public LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (!isAdded()) {
                return;
            }
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                double latitude = resultData.getDouble(FetchLocationIntentService.Constants.RESULT_LATITUDE);
                double longtitude = resultData.getDouble(FetchLocationIntentService.Constants.RESULT_LONGTITUDE);
                DeLog.d(TAG, "Got result: " + latitude + "," + longtitude);
                mLatitude = latitude;
                mLongtitude = longtitude;
                setGeoFence();
            }

        }
    }


    private GoogleApiClient mGoogleApiClient;

    private void setUpApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAutoCompleteAdapter = new PreviousPlacesAdapter(getActivity());
        mPlace.setAdapter(mAutoCompleteAdapter);
        mPlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationPlace item = mAutoCompleteAdapter.getItem(position);
                mLongtitude = item.getLongtitude();
                mLatitude = item.getLatitude();
                mAddress.setText(item.getAddress());
            }
        });
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // do NOT disconnect the GoogleClientAPI on stop... let the asynctask close the connection for us, since we don't want to cancel it accidentally
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "connected!!");
        // we are connected to the api! don't create the reminder yet
        // save it to the database, and create a id for it first
        String note = mNote.getText().toString();
        String address = mAddress.getText().toString();
        String name = mPlace.getText().toString();
        getActivity().setResult(Activity.RESULT_OK);
        new SaveReminderTask(getActivity(), mExistingID, note, name, mLatitude, mLongtitude, address).execute();
        getActivity().finish();
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

}
