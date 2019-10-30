package com.wrappy.android.misc.reset;


import java.util.List;
import javax.inject.Inject;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.ui.AbstractResetPasswordFragment;
import com.wrappy.android.server.ServerConstants.ServerLocale;

public class ResetPasswordTextFragment extends AbstractResetPasswordFragment {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ResetPasswordViewModel mResetPasswordViewModel;

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mResetPasswordViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ResetPasswordViewModel.class);
    }

    @Override
    protected void onResetPasswordSuccess() {
        getActivity().onBackPressed();
    }

    @Override
    protected LiveData<Resource<List<String>>> onValidatePasswordRequest(String newPassword) {
        return mResetPasswordViewModel.validatePasswordFormat(newPassword, ServerLocale.ENGLISH);
    }

    @Override
    protected LiveData<Resource<Boolean>> onResetPasswordRequest(String newPassword) {
        return mResetPasswordViewModel.resetUserPassword(mResetPasswordViewModel.getSecretKey(), newPassword, null);
    }
}
