package com.wrappy.android.home.profile;


import android.support.v4.app.FragmentManager;
import com.wrappy.android.R;
import com.wrappy.android.common.AbstractNavigationManager;

public class EditProfileNavigationManager extends AbstractNavigationManager {
    public EditProfileNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showEditProfileInfoPage() {
        showPage(new EditProfileInfoFragment(), false);
    }

    public void showEditProfileSmsVerificationPage(String countryCode, String phoneNumber) {
        showPage(EditProfileSmsVerificationFragment.create(countryCode, phoneNumber), true);
    }
}
