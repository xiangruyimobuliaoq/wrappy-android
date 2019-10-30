package com.wrappy.android.login;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.common.AbstractNavigationManager;
import com.wrappy.android.R;

public class LoginNavigationManager extends AbstractNavigationManager {

    public LoginNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showLoginPage() {
        showPage(new LoginUserFragment(), false);
    }

    public void showPasswordPage() {
        showPage(new LoginPasswordFragment(), true);
    }

    public void showForgotUsernamePage() {
        showPage(new ForgotUsernameFragment(), true);
    }

    public void showPatternPage() {
        showPage(new LoginPatternFragment(), false);
    }

    public void showRegisterPhonePage() {
        showPage(new LoginRegisterPhoneFragment(), true);
    }

}
