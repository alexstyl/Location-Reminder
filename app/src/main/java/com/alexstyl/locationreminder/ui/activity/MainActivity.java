package com.alexstyl.locationreminder.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alexstyl.locationreminder.R;
import com.alexstyl.locationreminder.ui.AboutDialog;
import com.alexstyl.locationreminder.ui.BaseActivity;


public class MainActivity extends BaseActivity {

    private static final String FMTAG_ABOUT = "alexstyl:about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.add_reminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewReminderActivity.startActivity(MainActivity.this);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            AboutDialog dialog = new AboutDialog();
            dialog.show(getSupportFragmentManager(), FMTAG_ABOUT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
