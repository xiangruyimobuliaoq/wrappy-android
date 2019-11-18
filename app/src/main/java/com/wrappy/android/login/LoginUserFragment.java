package com.wrappy.android.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;

import com.hbb20.CountryCodePicker;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.response.AccountStatusResponse;

import javax.inject.Inject;

public class LoginUserFragment extends SubFragment implements View.OnClickListener {

    @Inject
    LoginNavigationManager mLoginNavigationManager;
    @Inject
    NavigationManager mNavigationManager;

    private EditText mEditTextUserId;
    private Button mButtonLogin;
    private TextView mTextViewVersion;
    private TextView mTextViewForgotUser;
    private TextView mTextViewErrorUser;
    private TextView mTextViewRegisterPhone;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private LoginViewModel mLoginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_login_page, container, false);

        mEditTextUserId = view.findViewById(R.id.login_edittext_userid);
        mButtonLogin = view.findViewById(R.id.login_button_login);
        mTextViewVersion = view.findViewById(R.id.login_textview_version);
        mTextViewForgotUser = view.findViewById(R.id.login_textview_forget_user);
        mTextViewErrorUser = view.findViewById(R.id.login_textview_error_userid);
        mTextViewRegisterPhone = view.findViewById(R.id.login_textview_register);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mLoginViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(LoginViewModel.class);

        showToolbar(false);

        mButtonLogin.setOnClickListener(this);

        mTextViewForgotUser.setOnClickListener(this);

        mTextViewRegisterPhone.setOnClickListener(this);

        mTextViewVersion.setText(InputUtils.getAppVersionName(getContext()));
    }

    private void checkAccountStatus(Resource<AccountStatusResponse> result) {
        if (null == result.data) {
            mTextViewErrorUser.setText(result.message);
            return;
        }
        switch (result.data.status) {
            case AccountStatusResponse.STATUS_NORMAL:
                mLoginViewModel.setUsername(result.data.username);
                mLoginNavigationManager.showPasswordPage();
                break;
            case AccountStatusResponse.STATUS_NOT_EXIST:
            case AccountStatusResponse.STATUS_LOCKED:
                mEditTextUserId.setText("");
                mEditTextUserId.setBackgroundResource(R.drawable.error_edittext_bg);
                mTextViewErrorUser.setText(result.message);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button_login:
                mTextViewErrorUser.setText("");
                String userId = mEditTextUserId.getText().toString();
                if (!TextUtils.isEmpty(userId)) {
                    mLoginViewModel.getAccountStatus(userId).observe(getViewLifecycleOwner(),
                            result -> {
                                showLoadingDialog(result);
                                switch (result.status) {
                                    case SUCCESS:
                                    case SERVER_ERROR:
                                        checkAccountStatus(result);
                                        break;
                                    case CLIENT_ERROR:
                                        showAlertDialog(result.message, null);
                                        break;
                                }
                            });
                } else {
                    mTextViewErrorUser.setText(R.string.error_invalid_user_id);
                    mEditTextUserId.setBackgroundResource(R.drawable.error_edittext_bg);
                }
                break;
            case R.id.login_textview_register:
                mLoginNavigationManager.showRegisterPhonePage();
                break;
            case R.id.login_textview_forget_user:
                mLoginNavigationManager.showForgotUsernamePage();
                break;
        }
    }


}
