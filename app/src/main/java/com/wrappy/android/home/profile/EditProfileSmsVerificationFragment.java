package com.wrappy.android.home.profile;

import javax.inject.Inject;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.ui.AbstractSmsVerificationFragment;


public class EditProfileSmsVerificationFragment extends AbstractSmsVerificationFragment {
    public static final String KEY_EXTRA_COUNTRY_CODE = "country";
    public static final String KEY_EXTRA_PHONE_NUMBER = "phone";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    EditProfileViewModel mEditProfileViewModel;

    private String mCountryCode;
    private String mPhoneNumber;

    public static EditProfileSmsVerificationFragment create(String countryCode, String phoneNumber) {
        EditProfileSmsVerificationFragment f = new EditProfileSmsVerificationFragment();
        Bundle b = new Bundle();
        b.putString(KEY_EXTRA_COUNTRY_CODE, countryCode);
        b.putString(KEY_EXTRA_PHONE_NUMBER, phoneNumber);
        f.setArguments(b);
        return f;
    }

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mEditProfileViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(EditProfileViewModel.class);
    }

    @Override
    protected String getToolbarTitle() {
        return "SMS Verification";
    }

    @Override
    protected String getFullPhoneNumber() {
        mCountryCode = getArguments().getString(KEY_EXTRA_COUNTRY_CODE);
        mPhoneNumber = getArguments().getString(KEY_EXTRA_PHONE_NUMBER);
        return mCountryCode + mPhoneNumber;
    }

    @Override
    protected LiveData<Resource<Boolean>> onValidateSmsCodeRequest(String smsCode) {
        return mEditProfileViewModel.validateSmsCode(
                mCountryCode,
                mPhoneNumber,
                smsCode);
    }

    @Override
    protected LiveData<Resource<Boolean>> onResendSmsCodeRequest() {
        return mEditProfileViewModel.validatePhoneAndSendSmsCode(
                mCountryCode,
                mPhoneNumber);
    }

    @Override
    protected void onPhoneNumberChanged(String countryCode, String phoneNumber) {
        mCountryCode = countryCode;
        mPhoneNumber = phoneNumber;
    }

    @Override
    protected void onValidateSmsSuccess() {
        mEditProfileViewModel.setCountryCode(mCountryCode);
        mEditProfileViewModel.setPhoneNumber(mPhoneNumber);
        mEditProfileViewModel.setSmsVerified(true);
        getActivity().onBackPressed();
    }
}
