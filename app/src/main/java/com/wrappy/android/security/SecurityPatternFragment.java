package com.wrappy.android.security;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.common.ui.AbstractCreatePatternFragment;

public class SecurityPatternFragment extends AbstractCreatePatternFragment {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);
    }

    @Override
    protected void onPatternConfirmed(String pattern) {
        updatePatternPassword(pattern);
    }

    private void setPatternPasswordFlag() {
        mSecurityViewModel.setPatternPasswordFlag(true).observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mSecurityViewModel.setViewPatternPasswordFlag(true);
                case SERVER_ERROR:
                case CLIENT_ERROR:
                    super.reset();
                    getActivity().onBackPressed();
                    break;
            }
        });
    }

    private void updatePatternPassword(String pattern) {
        mSecurityViewModel.updatePatternPassword(pattern)
                .observe(SecurityPatternFragment.this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            mSecurityViewModel.setViewPatternPassword(pattern);
                            setPatternPasswordFlag();
                            break;
                        case SERVER_ERROR:
                        case CLIENT_ERROR:
                            setErrorText(result.message);
                            break;
                    }
                });
    }
}
