package com.wrappy.android.register;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.common.AbstractNavigationManager;
import com.wrappy.android.R;

public class RegisterNavigationManager extends AbstractNavigationManager {

    public RegisterNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showSmsVerificationPage() {
        showPage(new RegisterSmsVerificationFragment(), false);
    }

    public void showQuestionPage() {
        showPage(new RegisterQuestionFragment(), true);
    }

    public void showProfilePage() {
        showPage(new RegisterProfileFragment(), true);
    }

    public void showPasswordPage() {
        showPage(new RegisterPasswordFragment(), false);
    }
}
