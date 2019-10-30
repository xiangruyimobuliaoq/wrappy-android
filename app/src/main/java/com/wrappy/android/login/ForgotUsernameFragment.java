package com.wrappy.android.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

import javax.inject.Inject;

public class ForgotUsernameFragment extends SubFragment implements OnClickListener, TextWatcher {

    @Inject
    LoginNavigationManager mLoginNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    LoginViewModel mLoginViewModel;

    private EditText mEditTextEmail;
    private EditText mEditTextPhone;

    private CountryCodePicker mCountryCodePickerPhone;

    private Button mButtonSend;

    private AlertDialog.Builder mADBuilder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_forgot_username_page, container, false);

        mEditTextEmail = view.findViewById(R.id.forgot_username_edittext_email);
        mEditTextEmail.addTextChangedListener(this);

        mEditTextPhone = view.findViewById(R.id.forgot_username_edittext_phone);
        mEditTextPhone.addTextChangedListener(this);

        mCountryCodePickerPhone = view.findViewById(R.id.forgot_username_countrycodepicker_phone);

        mButtonSend = view.findViewById(R.id.forgot_username_button_send);
        InputUtils.enableView(mButtonSend, false);

        mCountryCodePickerPhone.registerCarrierNumberEditText(mEditTextPhone);

        mADBuilder = new AlertDialog.Builder(getContext());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mLoginViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(LoginViewModel.class);

        showToolbar(true);
        setToolbarTitle(getString(R.string.forgot_userid_title));

        mButtonSend.setOnClickListener(this);
    }

    private boolean isFormCompleted() {
        return mEditTextEmail.length() > 0 &&
                mCountryCodePickerPhone.isValidFullNumber() && mEditTextPhone.length() > 0;
    }

    private void forgotUsername(String email, String phone) {
        if(InputUtils.isValidEmail(email)) {
            mLoginViewModel.findUserAccount(email, phone).observe(this,
                    result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case SUCCESS:
                                showAlertDialog(getString(R.string.message_has_sent_to_your_mobile_and_email), (dialog, which) -> {
                                    dialog.dismiss();
                                    mNavigationManager.showWelcomePage();
                                });
                                break;
                            case CLIENT_ERROR:
                            case SERVER_ERROR:
                                showAlertDialog(result.message, null);
                                break;
                        }
                    });
        } else {
            showAlertDialog(getString(R.string.please_enter_valid_email), null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_username_button_send:
                forgotUsername(mEditTextEmail.getText().toString(), mCountryCodePickerPhone.getFullNumberWithPlus());
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        InputUtils.enableView(mButtonSend, isFormCompleted());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
