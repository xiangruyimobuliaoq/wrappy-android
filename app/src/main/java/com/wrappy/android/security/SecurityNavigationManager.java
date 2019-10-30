package com.wrappy.android.security;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.R;
import com.wrappy.android.common.AbstractNavigationManager;

public class SecurityNavigationManager extends AbstractNavigationManager {

    public SecurityNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showValidatePasswordPage() {
        showPage(new SecurityValidatePasswordFragment(), false);
    }

    public void showValidatePatternPage() {
        showPage(new SecurityValidatePatternFragment(), false);
    }

    public void showSecurityPage(){
        showPage(new SecurityPageFragment(), false);
    }

    public void showChangePasswordPage(){showPage(new SecurityChangePasswordFragment(), true);}

    public void showChooseQuestionPage(){showPage(new SecurityChooseQuestionFragment(), true);}

    public void showBlockedUsersPage(){showPage(new SecurityBlockedUsersFragment(), true);}

    public void showCreatePatternPage() {
        showPage(new SecurityPatternFragment(), true);
    }
}
