package com.wrappy.android.forgot;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.common.AbstractNavigationManager;
import com.wrappy.android.R;

public class ForgotPasswordNavigationManager extends AbstractNavigationManager {

    public ForgotPasswordNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }


    public void showForgotPasswordQuestionPage() {
        showPage(new ForgotPasswordQuestionFragment(), false);
    }

    public void showForgotPasswordChangePasswordPage(String secretKey) {
        showPage(ForgotPasswordChangePasswordFragment.create(secretKey), true);
    }
}
