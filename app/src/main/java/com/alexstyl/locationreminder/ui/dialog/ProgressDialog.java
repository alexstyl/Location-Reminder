package com.alexstyl.locationreminder.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.alexstyl.locationreminder.ui.BaseDialog;

/**
 * <p>Created by alexstyl on 28/02/15.</p>
 */
public class ProgressDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new android.app.ProgressDialog(getActivity());
    }
}
