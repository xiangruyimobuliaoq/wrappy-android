package com.wrappy.android.forgot;

import java.util.List;
import javax.inject.Inject;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.ui.AbstractResetPasswordFragment;
import com.wrappy.android.server.ServerConstants.ServerLocale;

public class ForgotPasswordChangePasswordFragment extends AbstractResetPasswordFragment {
    private static final String KEY_EXTRA_SECRET = "secret";

    @Inject
    ForgotPasswordNavigationManager mForgotPasswordNavigationManager;

    @Inject
    NavigationManager mMainNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ForgotPasswordViewModel mForgotPasswordViewModel;

    private String mSecret;

    public static ForgotPasswordChangePasswordFragment create(String secretKey) {
        ForgotPasswordChangePasswordFragment f = new ForgotPasswordChangePasswordFragment();
        Bundle b = new Bundle();
        b.putString(KEY_EXTRA_SECRET, secretKey);
        f.setArguments(b);
        return f;
    }

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mForgotPasswordViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ForgotPasswordViewModel.class);

        mSecret = getArguments().getString(KEY_EXTRA_SECRET);
    }

    @Override
    protected void onResetPasswordSuccess() {
        mSecret = null;
        getArguments().putString(KEY_EXTRA_SECRET, null);
        mForgotPasswordNavigationManager.clearBackstack();
        mMainNavigationManager.getFragmentManager().popBackStack();
    }

    @Override
    protected LiveData<Resource<List<String>>> onValidatePasswordRequest(String newPassword) {
        return mForgotPasswordViewModel.validatePasswordFormat(newPassword, ServerLocale.ENGLISH);
    }

    @Override
    protected LiveData<Resource<Boolean>> onResetPasswordRequest(String newPassword) {
        return mForgotPasswordViewModel.resetUserPassword(mSecret, newPassword);
    }
}
