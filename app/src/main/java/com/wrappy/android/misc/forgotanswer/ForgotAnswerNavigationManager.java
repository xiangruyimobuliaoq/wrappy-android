package com.wrappy.android.misc.forgotanswer;


import android.support.v4.app.FragmentManager;
import com.wrappy.android.R;
import com.wrappy.android.common.AbstractNavigationManager;

public class ForgotAnswerNavigationManager extends AbstractNavigationManager {
    public ForgotAnswerNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showEmailInputPage() {
        showPage(new ForgotAnswerEmailInputFragment(), false);
    }

    public void showEmailSentPage() {
        showPage(new ForgotAnswerEmailSentFragment(), false);
    }
}
