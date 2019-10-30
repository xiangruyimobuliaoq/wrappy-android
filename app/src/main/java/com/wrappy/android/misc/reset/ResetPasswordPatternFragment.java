package com.wrappy.android.misc.reset;


import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.common.ui.AbstractCreatePatternFragment;

public class ResetPasswordPatternFragment extends AbstractCreatePatternFragment {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ResetPasswordViewModel mResetPasswordViewModel;

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mResetPasswordViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ResetPasswordViewModel.class);
    }

    @Override
    protected void onPatternConfirmed(String pattern) {
        mResetPasswordViewModel.resetUserPassword(mResetPasswordViewModel.getSecretKey(), null, pattern)
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch(result.status) {
                        case SUCCESS:
                            getActivity().onBackPressed();
                            break;
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            showAlertDialog(result.message, null);
                            break;
                    }
                });
    }
}
