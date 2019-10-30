package com.wrappy.android.common.ui;


import java.util.List;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

public abstract class AbstractResetPasswordFragment extends SubFragment implements OnClickListener {
    private EditText mEditTextNewPassword;
    private EditText mEditTextConfirmPassword;
    private TextView mTextViewPasswordError;

    private Button mButtonReset;

    protected abstract void onActivityCreatedInternal();

    protected abstract void onResetPasswordSuccess();

    protected abstract LiveData<Resource<List<String>>> onValidatePasswordRequest(String newPassword);

    protected abstract LiveData<Resource<Boolean>> onResetPasswordRequest(String newPassword);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_forgot_change_password_page, container, false);

        mEditTextNewPassword = view.findViewById(R.id.change_password_edittext_new_pass);
        mEditTextConfirmPassword = view.findViewById(R.id.change_password_edittext_confirm_pass);
        mTextViewPasswordError = view.findViewById(R.id.change_password_textview_guide_pass);
        mButtonReset = view.findViewById(R.id.change_password_button_reset);

        InputUtils.disableWhitespaceInput(mEditTextNewPassword);
        InputUtils.disableWhitespaceInput(mEditTextConfirmPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onActivityCreatedInternal();

        mButtonReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password_button_reset:
                mEditTextNewPassword.setBackgroundColor(getResources().getColor(R.color.white));
                mEditTextConfirmPassword.setBackgroundColor(getResources().getColor(R.color.white));
                String password = mEditTextNewPassword.getText().toString();
                String confirmPassword = mEditTextConfirmPassword.getText().toString();
                if (!TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(confirmPassword)
                        && password.equals(confirmPassword)) {
                    validatePasswordFormat();
                } else {
                    mTextViewPasswordError.setText(R.string.reg_password_error);
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
                break;
        }
    }

    private void validatePasswordFormat() {
        String newPassword = mEditTextNewPassword.getText().toString();
        onValidatePasswordRequest(newPassword)
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            resetPasswordRequest(newPassword);
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
                            mTextViewPasswordError.setText(sb.toString());
                            mEditTextNewPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                            mEditTextConfirmPassword.setBackgroundResource(R.drawable.error_edittext_bg);
                            break;
                    }
                });
    }

    private void resetPasswordRequest(String newPassword) {
        onResetPasswordRequest(newPassword).observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    onResetPasswordSuccess();
                    break;
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    showAlertDialog(result.message, null);
                    break;
            }
        });
    }
}
