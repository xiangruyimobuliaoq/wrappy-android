package com.wrappy.android.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public abstract class AbstractNavigationManager {

    private FragmentManager mFragmentManager;

    public AbstractNavigationManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    public void clearBackstack() {
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    protected abstract int getContainerId();

    protected void showPage(Fragment fragment, boolean withBack) {
        if (withBack) {
            getFragmentManager().beginTransaction()
                    .replace(getContainerId(), fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(getContainerId(), fragment)
                    .commitAllowingStateLoss();
        }
    }

}
