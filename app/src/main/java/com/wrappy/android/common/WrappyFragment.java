package com.wrappy.android.common;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

public class WrappyFragment extends Fragment {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getActivity().getWindow();

        if (window != null &&
                (getFragmentSoftInputMode() & window.getAttributes().softInputMode) == 0) {
            // Change mode only if soft input requested mode is different from current
            window.setSoftInputMode(getFragmentSoftInputMode());
        }
    }

    /**
     * Provides the window softInputMode that the activity
     * should be in when this fragment is displayed.
     * <p>
     * The softInputMode flag returned by this method is set during Fragment.onActivityCreated()
     * <p>
     * Is set to WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN by default.
     *
     * @return one of soft input modes from Window.LayoutParams
     */
    protected int getFragmentSoftInputMode() {
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
    }
}
