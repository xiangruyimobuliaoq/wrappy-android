package com.wrappy.android.misc.forgotanswer;

import javax.inject.Inject;

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
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;


public class ForgotAnswerEmailInputFragment extends SubFragment {

    @Inject
    ForgotAnswerNavigationManager mForgotAnswerNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ForgotAnswerViewModel mViewModel;

    private EditText mEditTextEmail;
    private TextView mTextViewError;
    private Button mButtonSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_forgot_answer, container, false);
        setToolbarTitle("");
        mEditTextEmail = view.findViewById(R.id.forgot_answer_edittext_email);
        mTextViewError = view.findViewById(R.id.forgot_answer_textview_error);
        mButtonSubmit = view.findViewById(R.id.forgot_answer_button_submit);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ForgotAnswerViewModel.class);

        mButtonSubmit.setOnClickListener(v -> {
            if (mEditTextEmail.length() > 0) {
                sendEmail();
            } else {
                mTextViewError.setText(R.string.forgot_answer_text_error);
            }
        });
    }

    private void sendEmail() {
        String email = mEditTextEmail.getText().toString();
        if (InputUtils.isValidEmail(email)) {
            mViewModel.sendRecoveryEmail(
                    mEditTextEmail.getText().toString(),
                    mViewModel.getRecoveryType()).observe(this, result -> {
                showLoadingDialog(result);
                switch (result.status) {
                    case SUCCESS:
                        mForgotAnswerNavigationManager.showEmailSentPage();
                        break;
                    case SERVER_ERROR:
                        mTextViewError.setText(result.message);
                        break;
                    case CLIENT_ERROR:
                        showAlertDialog(result.message, null);
                        break;
                }
            });
        } else {
            showAlertDialog(getString(R.string.please_enter_valid_email), null);
        }
    }
}
