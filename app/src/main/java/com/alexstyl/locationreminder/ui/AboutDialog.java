package com.alexstyl.locationreminder.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.alexstyl.locationreminder.R;


/**
 * <p>Created by alexstyl on 15/02/15.</p>
 */
public class AboutDialog extends BaseDialog {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        StringBuilder str = new StringBuilder();
        str.append(getString(R.string.about_message_by))
                .append("\n\n")
                .append(getString(R.string.about_message_contact));

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.app_name)
                .setMessage(str.toString())
                .setNegativeButton(R.string.close, null)
                .create();
        return dialog;
    }
}
