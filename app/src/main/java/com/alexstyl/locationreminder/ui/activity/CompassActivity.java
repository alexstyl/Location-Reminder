package com.alexstyl.locationreminder.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.ui.BaseActivity;
import com.alexstyl.locationreminder.ui.fragment.CompassFragment;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class CompassActivity extends BaseActivity {


    public static final String EXTRA_REMINDER_ID = App.PACKAGE + ".REMINDER_ID";
    private static final String TAG = "CompassActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        Bundle extras = getIntent().getExtras();
        if (!extras.containsKey(EXTRA_REMINDER_ID)) {
            DeLog.w(TAG, "Cannot show activity without reminder ID");
            finish();
            return;
        }

        if (savedInstanceState == null) {
            CompassFragment frag = CompassFragment.newInstance(extras.getLong(EXTRA_REMINDER_ID));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.compass_holder, frag).commit();

        }

    }

    /**
     * Starts the CompassActivity that will point to a reminder of the given id!
     *
     * @param context    The context to use
     * @param reminderID The id of the reminder to point at
     */
    public static void startActivity(Context context, long reminderID) {
        Intent i = new Intent(context, CompassActivity.class);
        i.putExtra(EXTRA_REMINDER_ID, reminderID);
        context.startActivity(i);

    }
}
