package com.wrappy.android.security;


import javax.inject.Inject;

import android.support.v7.app.AlertDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.wrappy.android.MainActivityViewModel;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

public class SecurityValidatePasswordFragment extends SubFragment implements OnClickListener {

    @Inject
    SecurityNavigationManager mSecurityNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;
    MainActivityViewModel mActivityViewModel;

    private TextInputEditText mEditTextPassword;
    private TextView mTextViewErrorPassword;
    private TextView mTextViewForgotPassword;
    private Button mButtonProceed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_login_password_page, container, false);

        mEditTextPassword = view.findViewById(R.id.password_edittext_password);
        mTextViewErrorPassword = view.findViewById(R.id.password_textview_error_password);
        mButtonProceed = view.findViewById(R.id.password_button_proceed);
        mTextViewForgotPassword = view.findViewById(R.id.password_textview_forgot_password);

        InputUtils.disableWhitespaceInput(mEditTextPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);
        mActivityViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MainActivityViewModel.class);

        mButtonProceed.setOnClickListener(this);
        mTextViewForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.password_button_proceed:
                checkPassword(mEditTextPassword.getText().toString());
                break;
            case R.id.password_textview_forgot_password:
                showLogoutConfirmation();
                break;
        }
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.security_forgot_password_logout_message);
        builder.setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
            dialog.dismiss();
            mActivityViewModel.setForgotLogout(true);
            mSecurityViewModel.logoutAccount();
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
            dialog.dismiss();
            getActivity().onBackPressed();
        });
        builder.show();
    }

    private void checkPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            mTextViewErrorPassword.setText(getString(R.string.error_incorrect_password));
        } else {
            mSecurityViewModel.validateTextPassword(password)
                    .observe(this, result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case SUCCESS:
                                mSecurityViewModel.setViewPassword(password);
                                if (mSecurityViewModel.getViewPatternPasswordFlag()) {
                                    mSecurityNavigationManager.showValidatePatternPage();
                                } else {
                                    mSecurityNavigationManager.showSecurityPage();
                                }
                                break;
                            case CLIENT_ERROR:
                                showAlertDialog(result.message, null);
                                break;
                            case SERVER_ERROR:
                                mTextViewErrorPassword.setText(getString(R.string.error_incorrect_password));
                                break;
                        }
                    });
        }
    }
}
