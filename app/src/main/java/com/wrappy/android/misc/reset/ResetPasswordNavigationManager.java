package com.wrappy.android.misc.reset;


import android.support.v4.app.FragmentManager;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;

public class ResetPasswordNavigationManager extends NavigationManager {
    public ResetPasswordNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showResetPatternPage() {
        showPage(new ResetPasswordPatternFragment(), false);
    }

    public void showResetTextPasswordPage() {
        showPage(new ResetPasswordTextFragment(), false);

    }
}
