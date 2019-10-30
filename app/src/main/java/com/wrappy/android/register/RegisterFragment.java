package com.wrappy.android.register;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;

import javax.inject.Inject;

public class RegisterFragment extends BaseFragment {
    public static final String KEY_PHONE_NUMBER = "phone";
    public static final String KEY_COUNTRY_CODE = "countryCode";

    @Inject
    RegisterNavigationManager mRegisterNavigationManager;

    private Toolbar mToolbar;
    private boolean mIsShown;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    RegisterViewModel mRegisterViewModel;

    public static RegisterFragment create(String countryCode, String phoneNumber) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle b = new Bundle();
        b.putString(KEY_PHONE_NUMBER, phoneNumber);
        b.putString(KEY_COUNTRY_CODE, countryCode);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_main, container, false);

        mToolbar = view.findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mRegisterViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RegisterViewModel.class);
        mRegisterViewModel.setPhoneNumber(getArguments().getString(KEY_PHONE_NUMBER));
        mRegisterViewModel.setCountryCode(getArguments().getString(KEY_COUNTRY_CODE));

        setToolbar(mToolbar);
        showToolbarBackButton(true);
        if (!mIsShown) {
            mRegisterNavigationManager.showSmsVerificationPage();
            mIsShown = true;
        }

    }
}
