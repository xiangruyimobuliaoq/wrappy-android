package com.wrappy.android.security;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

import javax.inject.Inject;

public class SecurityChangePasswordFragment extends SubFragment implements View.OnClickListener {

    @Inject
    SecurityNavigationManager mSecurityNavigationManagaer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;

    private EditText mEditTextNewPassword;
    private EditText mEditTextConfirmPassword;

    private Button mButtonDone;
    private TextView mTextViewErrorMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_security_change_password, container, false);

        mButtonDone = view.findViewById(R.id.sec_changepass_button_done);
        mEditTextNewPassword = view.findViewById(R.id.sec_changepass_edittext_new_pass);
        mEditTextConfirmPassword = view.findViewById(R.id.sec_changepass_edittext_confirm_pass);
        mTextViewErrorMessage = view.findViewById(R.id.sec_changepass_textview_error_password);

        InputUtils.disableWhitespaceInput(mEditTextNewPassword);
        InputUtils.disableWhitespaceInput(mEditTextConfirmPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);

        mButtonDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sec_changepass_button_done:
                checkPassword();
                break;
        }
    }

    private void checkPassword() {
        mTextViewErrorMessage.setText("");
        mEditTextNewPassword.setBackgroundColor(getResources().getColor(R.color.white));
        mEditTextConfirmPassword.setBackgroundColor(getResources().getColor(R.color.white));
        String password = mEditTextNewPassword.getText().toString();
        String confirmPassword = mEditTextConfirmPassword.getText().toString();
        if (!TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(confirmPassword)
                && password.equals(confirmPassword)) {
            mSecurityViewModel.updateUserPassword(confirmPassword).observe(this, result -> {
                showLoadingDialog(result);
                switch (result.status) {
                    case SUCCESS:
                        mSecurityViewModel.setViewPassword(confirmPassword);
                        getActivity().onBackPressed();
                        break;
                    case CLIENT_ERROR:
                        showAlertDialog(result.message, null);
                        break;
                    case SERVER_ERROR:
                        StringBuilder sb = new StringBuilder();
                        for (String errorString : result.data) {
                            sb.append(errorString);
                            sb.append("\n");
                        }
                        mTextViewErrorMessage.setText(sb.toString());
                        mEditTextNewPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                        mEditTextConfirmPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                        break;
                }
            });
        } else {
            mTextViewErrorMessage.setText(R.string.reg_password_error);
            if (TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)) {
                mEditTextNewPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                mEditTextConfirmPassword.setBackgroundResource(R.drawable.error_edittext_bg);
            } else {
                if (TextUtils.isEmpty(password)) {
                    mEditTextNewPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                } else if (TextUtils.isEmpty(confirmPassword) || !password.equals(confirmPassword)) {
                    mEditTextConfirmPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                }
            }
        }
    }
}
