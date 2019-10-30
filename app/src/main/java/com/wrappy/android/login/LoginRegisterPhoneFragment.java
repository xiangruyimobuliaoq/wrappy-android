package com.wrappy.android.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.common.utils.InputUtils;

import javax.inject.Inject;

/**
 * Created by Dan Chua on 2019-05-06
 */
public class LoginRegisterPhoneFragment extends BaseFragment implements View.OnClickListener {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private LoginViewModel mLoginViewModel;

    private Button mButtonRegister;
    private CountryCodePicker mCountryCodePicker;
    private EditText mEditTextPhoneNumber;
    private TextView mTextViewLogin;
    private TextView mTextViewVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_reg_mobile_page, container, false);

        mEditTextPhoneNumber = view.findViewById(R.id.reg_mobile_edittext_phone);

        mCountryCodePicker = view.findViewById(R.id.reg_mobile_spinner_country);
        mCountryCodePicker.registerCarrierNumberEditText(mEditTextPhoneNumber);

        mTextViewLogin = view.findViewById(R.id.reg_mobile_textview_login);
        mTextViewLogin.setOnClickListener(this);

        mButtonRegister = view.findViewById(R.id.reg_mobile_button_register);
        mButtonRegister.setOnClickListener(this);

        mTextViewVersion = view.findViewById(R.id.reg_mobile_textview_version);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mLoginViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginViewModel.class);

        mTextViewVersion.setText(InputUtils.getAppVersionName(getContext()));

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.reg_mobile_button_register:
                validatePhoneNumber();
                break;

            case R.id.reg_mobile_textview_login:
                getFragmentManager().popBackStackImmediate();
                break;

        }
    }

    private void validatePhoneNumber() {
        String countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();
        String phoneNumber = mCountryCodePicker.getFullNumberWithPlus().replace(countryCode, "");
        if (mCountryCodePicker.isValidFullNumber()) {
            mLoginViewModel.validatePhoneAndSendSmsCode(countryCode, phoneNumber)
                    .observe(this, response -> {
                        showLoadingDialog(response);
                        switch (response.status) {
                            case SUCCESS:
                                mNavigationManager.showRegisterPage(mCountryCodePicker.getSelectedCountryCodeWithPlus(), phoneNumber);
                                break;
                            case SERVER_ERROR:
                            case CLIENT_ERROR:
                                showAlertDialog(response.message, null);
                                break;
                        }
                    });
        } else {
            showAlertDialog(getString(R.string.error_invalid_phone_number), null);
        }
    }

}
