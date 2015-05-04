package com.alexstyl.locationreminder.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * <p>Created by alexstyl on 17/02/15.</p>
 */
public class BaseFragment extends Fragment {


    private BaseActivity mActivity;

    @Override
    public void onAttach(Activity activity) {

        try {
            mActivity = (BaseActivity) activity;
        } catch (ClassCastException ex) {
            throw new RuntimeException(this.getClass().getSimpleName() + " can only be attached to a BaseActivity");
        }
        super.onAttach(activity);
    }

    /**
     * Returns the parent {@linkplain BaseActivity} of this fragment.
     *
     */
    protected BaseActivity getBaseActivity() {
        return mActivity;
    }
}
