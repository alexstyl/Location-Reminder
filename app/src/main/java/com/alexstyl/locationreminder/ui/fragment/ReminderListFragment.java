package com.alexstyl.locationreminder.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.Notifier;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.db.DBHelper;
import com.alexstyl.locationreminder.entity.StoredReminder;
import com.alexstyl.locationreminder.ui.BaseFragment;
import com.alexstyl.locationreminder.ui.activity.CompassActivity;
import com.alexstyl.locationreminder.ui.activity.NewReminderActivity;
import com.alexstyl.locationreminder.ui.fragment.adapter.ReminderListAdapter;
import com.alexstyl.locationreminder.ui.loader.ReminderListLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by alexstyl on 17/02/15.</p>
 */
public class ReminderListFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<ArrayList<StoredReminder>>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int ID_REMINDERS = 100;
    private static final int ID_REMINDER_DELETE = 302;
    private static final int ID_REMINDER_EDIT = 303;
    private static final String FM_TAG_DELETE_CONFIRM = "alexstyl:delete_reminder";
    private static final String TAG = "ReminderListFragment";

    private View mEmptyView;
    private RecyclerView mRecycler;
    private ReminderListAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_reminder_list, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_order_by_distnace:
                onOrderToggled(item.isChecked());
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Called whenever the sort of the list needs to be changed
     *
     * @param sortByDistance True when the user asked the list to be sorted by distance
     */
    private void onOrderToggled(boolean sortByDistance) {
        AppConfigs.setListOrderedByDistance(getActivity(), sortByDistance);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);
        mRecycler = (RecyclerView) view.findViewById(R.id.recyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(layoutManager);
        mEmptyView = view.findViewById(android.R.id.empty);
        mEmptyView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ReminderListAdapter(getActivity());
        mAdapter.setOnCardClickListener(new ReminderListAdapter.OnCardClickListener() {
            @Override
            public void onCardClicked(View view, int position) {
                StoredReminder locationPlace = mAdapter.getReminder(position);
                CompassActivity.startActivity(getActivity(), locationPlace.getID());
            }

            @Override
            public void onCardOptionsClicked(View view, int position) {
                final StoredReminder reminder = mAdapter.getReminder(position);
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.getMenu().add(0, ID_REMINDER_EDIT, 0, R.string.edit);
                popup.getMenu().add(0, ID_REMINDER_DELETE, 0, R.string.reminder_delete);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == ID_REMINDER_DELETE) {
                            //TODO dialog for comfirmation
                            onReminderDelete(reminder.getID());
                        } else if (id == ID_REMINDER_EDIT) {
                            NewReminderActivity.startActivity(getActivity(), reminder.getID());
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        mRecycler.setAdapter(mAdapter);
        getLoaderManager().initLoader(ID_REMINDERS, null, this);
    }

    GoogleApiClient mGoogleApiClient;

    private void onReminderDelete(long id) {

        mDeletedReminderID = id;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            deleteReminder(id);
        }

    }

    private void deleteReminder(long id) {
        List<String> ids = new ArrayList<>();
        ids.add(String.valueOf(id));
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, ids);

        mGoogleApiClient.disconnect();
        DeLog.d(TAG, "deleting reminder " + id);
        new DeleteReminderTask().execute(id);
    }

    private class DeleteReminderTask extends AsyncTask<Long, Void, Boolean> {

        private long deletedID;

        @Override
        protected Boolean doInBackground(Long... params) {
            long id = params[0];
            deletedID = id;
            return DBHelper.getInstance(getActivity()).deleteReminder(id);

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(getActivity(), R.string.reminder_deleted, Toast.LENGTH_SHORT).show();
                // if the reminder is deleted also delete the notification of it
                Notifier.cancelForReminder(getActivity(), deletedID);
                App.broadcastOnReminderCreated(getActivity());
            }
        }
    }


    @Override
    public Loader<ArrayList<StoredReminder>> onCreateLoader(int id, Bundle args) {
        if (id == ID_REMINDERS) {
            return new ReminderListLoader(getActivity());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<StoredReminder>> loader, ArrayList<StoredReminder> data) {
        if (loader.getId() == ID_REMINDERS) {
            mAdapter.setData(data);
        }
        setEmptyViewVisibility(data == null || data.isEmpty());
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<StoredReminder>> loader) {
        if (loader.getId() == ID_REMINDERS) {
            mAdapter.setData(null);
        }
        setEmptyViewVisibility(true);

    }

    public void setEmptyViewVisibility(boolean show) {
        int vis;
        if (show) {
            vis = View.VISIBLE;
        } else {
            vis = View.GONE;
        }
        this.mEmptyView.setVisibility(vis);
    }

    private long mDeletedReminderID = -1;

    /////////
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "connected!!");
        deleteReminder(mDeletedReminderID);
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
