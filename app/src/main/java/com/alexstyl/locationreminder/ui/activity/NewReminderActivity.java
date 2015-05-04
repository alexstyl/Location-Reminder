package com.alexstyl.locationreminder.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.alexstyl.locationreminder.App;
import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.ui.BaseActivity;
import com.alexstyl.locationreminder.ui.fragment.NewReminderFragment;

/**
 * <p>Created by alexstyl on 22/02/15.</p>
 */
public class NewReminderActivity extends BaseActivity {


    public static final String EXTRA_REMINDER_ID = App.PACKAGE + ".reminder_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reminder);
        setTitle(R.string.new_reminder);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // if the activity is displayed fullscreen instead of a dialog,
            // fix its home button listener
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_action_content_clear);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_REMINDER_ID)) {
            long _id = extras.getLong(EXTRA_REMINDER_ID, -1l);
            NewReminderFragment reminderFragment =
                    (NewReminderFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_new_reminder);

            reminderFragment.setExistingReminder(_id);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // leave the activity without saving anything
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Starts a new New Reminder Activity
     *
     * @param context The context to use
     */
    public static void startActivity(Context context) {
        startActivity(context, -1l);
    }

    /**
     * Starts a new New Reminder Activity
     *
     * @param context    The context to use
     * @param reminderID The id of an existing reminder in order to edit
     */
    public static void startActivity(Context context, long reminderID) {
        Intent i = new Intent(context, NewReminderActivity.class);
        if (reminderID != -1l) {
            i.putExtra(EXTRA_REMINDER_ID, reminderID);

        }
        context.startActivity(i);
    }
}
