package com.wrappy.android.register;

import javax.inject.Inject;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.ui.AbstractSmsVerificationFragment;

public class RegisterSmsVerificationFragment extends AbstractSmsVerificationFragment {
    @Inject
    RegisterNavigationManager mRegisterNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    RegisterViewModel mRegisterViewModel;

    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mRegisterViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(RegisterViewModel.class);
    }

    protected String getToolbarTitle() {
        return getString(R.string.reg_smsver_toolbar_title);
    }

    protected String getFullPhoneNumber() {
        return mRegisterViewModel.getCountryCode() + mRegisterViewModel.getPhoneNumber();
    }

    protected LiveData<Resource<Boolean>> onValidateSmsCodeRequest(String smsCode) {
        return mRegisterViewModel.validateSmsCode(smsCode);
    }

    protected LiveData<Resource<Boolean>> onResendSmsCodeRequest() {
        return mRegisterViewModel.validatePhone();
    }

    protected void onPhoneNumberChanged(String countryCode, String phoneNumber) {
        mRegisterViewModel.setPhoneNumber(phoneNumber);
        mRegisterViewModel.setCountryCode(countryCode);
    }

    protected void onValidateSmsSuccess() {
        mRegisterNavigationManager.showPasswordPage();
    }
}
