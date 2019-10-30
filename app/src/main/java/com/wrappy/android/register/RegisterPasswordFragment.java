package com.wrappy.android.register;

import javax.inject.Inject;

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
import com.wrappy.android.server.ServerConstants.ServerLocale;

public class RegisterPasswordFragment extends SubFragment implements View.OnClickListener {

    @Inject
    RegisterNavigationManager mRegisterNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    RegisterViewModel mRegisterViewModel;

    private EditText mEditTextNewPassword;
    private EditText mEditTextConfirmPassword;
    private Button mButtonNext;

    private TextView mTextViewPasswordError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_reg_password_page, container, false);
        mButtonNext = view.findViewById(R.id.reg_password_button_next);
        mEditTextNewPassword = view.findViewById(R.id.reg_password_edittext_pass);
        mEditTextConfirmPassword = view.findViewById(R.id.reg_password_edittext_confirm_pass);
        mTextViewPasswordError = view.findViewById(R.id.reg_password_textview_error_password);

        InputUtils.disableWhitespaceInput(mEditTextNewPassword);
        InputUtils.disableWhitespaceInput(mEditTextConfirmPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mRegisterViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(RegisterViewModel.class);

        setToolbarTitle(getString(R.string.reg_toolbar_title));
        showToolbarBackButton(false);

        mButtonNext.setOnClickListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_password_button_next:
                mEditTextNewPassword.setBackgroundColor(getResources().getColor(R.color.white));
                mEditTextConfirmPassword.setBackgroundColor(getResources().getColor(R.color.white));
                String password = mEditTextNewPassword.getText().toString();
                String confirmPassword = mEditTextConfirmPassword.getText().toString();
                if (!TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(confirmPassword)
                        && password.equals(confirmPassword)) {
                    mRegisterViewModel.validatePasswordFormat(mEditTextNewPassword.getText().toString(), ServerLocale.ENGLISH)
                            .observe(this, result -> {
                                showLoadingDialog(result);
                                switch (result.status) {
                                    case SUCCESS:
                                        mRegisterViewModel.loadSecurityQuestions(ServerLocale.ENGLISH).observe(this, loadQuestionResult -> {
                                            showLoadingDialog(loadQuestionResult);
                                            switch (loadQuestionResult.status) {
                                                case SUCCESS:
                                                    mEditTextNewPassword.setText("");
                                                    mEditTextConfirmPassword.setText("");
                                                    mRegisterNavigationManager.showQuestionPage();
                                                    break;
                                                case CLIENT_ERROR:
                                                case SERVER_ERROR:
                                                    showAlertDialog(loadQuestionResult.message, null);
                                                    break;
                                            }
                                        });
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
}
