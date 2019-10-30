package com.wrappy.android.about;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.R;
import com.wrappy.android.common.AbstractNavigationManager;

public class AboutNavigationManager extends AbstractNavigationManager {


    public AboutNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showAboutPage(){showPage(new AboutPageFragment(), false);}

    public void showWebPage(String url, String title) {
        showPage(AboutWebFragment.create(url, title), true);
    }
}
