package com.wrappy.android.forgot;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.server.account.body.request.SendRecoveryEmailBody;

public class ForgotPasswordQuestionFragment extends SubFragment implements OnClickListener, TextWatcher {
    private static final int MAX_QUESTIONS = 3;

    @Inject
    ForgotPasswordNavigationManager mForgotPasswordNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ForgotPasswordViewModel mForgotPasswordViewModel;

    private TextView[] mTextViewQuestion = new TextView[3];
    private EditText[] mEditTextAnswer = new EditText[3];
    private Button mButtonNext;

    private TextView mTextViewForgotAnswer;

    private int mQuestionSize;
    private boolean mShowError;

    StringBuilder mLockedTimeSb = new StringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_forgot_password_page, container, false);

        for (int x = 0; x < MAX_QUESTIONS; x++) {
            mEditTextAnswer[x] = view.findViewById(getResources().getIdentifier("forgot_edittext_answer" + (x + 1), "id", getContext().getPackageName()));
            mEditTextAnswer[x].addTextChangedListener(this);

            mTextViewQuestion[x] = view.findViewById(getResources().getIdentifier("forgot_textview_question" + (x + 1), "id", getContext().getPackageName()));
        }
        mButtonNext = view.findViewById(R.id.forgot_button_next);
        mTextViewForgotAnswer = view.findViewById(R.id.forgot_textview_forgot_answer_label);

        InputUtils.enableView(mButtonNext, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mForgotPasswordViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ForgotPasswordViewModel.class);

        List<SecurityQuestionAnswerBody> questionList = mForgotPasswordViewModel.getQuestionList();
        mQuestionSize = Math.min(MAX_QUESTIONS, questionList.size());
        for (int x = 0; x < mQuestionSize; x++) {
            mTextViewQuestion[x].setVisibility(View.VISIBLE);
            mTextViewQuestion[x].setText(questionList.get(x).question);
            mEditTextAnswer[x].setVisibility(View.VISIBLE);
        }
        mButtonNext.setOnClickListener(this);
        mTextViewForgotAnswer.setOnClickListener(this);
    }

    private List<SecurityQuestionAnswerBody> getAnswers() {
        List<SecurityQuestionAnswerBody> qaList = new ArrayList<>();
        for (int x = 0; x < mQuestionSize; x++) {
            SecurityQuestionAnswerBody question = mForgotPasswordViewModel.getQuestionList().get(x);
            SecurityQuestionAnswerBody answer = new SecurityQuestionAnswerBody(
                    question.code,
                    mEditTextAnswer[x].getText().toString());
            qaList.add(answer);
        }
        return qaList;
    }

    private boolean checkIfCompleted() {
        if (mQuestionSize <= 0) {
            return false;
        }
        for (int x = 0; x < mQuestionSize; x++) {
            if (mEditTextAnswer[x].length() <= 0) {
                return false;
            }
        }
        return true;
    }

    private void toggleEditTextError(boolean showError) {
        if (mShowError == showError) {
            return;
        }

        mShowError = showError;
        for (EditText editText : mEditTextAnswer) {
                editText.setBackgroundColor(
                        getResources().getColor(
                                showError ? R.color.error_edit_text_background : android.R.color.white));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_textview_forgot_answer_label:
                mNavigationManager.showForgotAnswerPage(SendRecoveryEmailBody.TYPE_PASSWORD);
                break;
            case R.id.forgot_button_next:
                mForgotPasswordViewModel.setQuestionAnswerList(getAnswers());
                mForgotPasswordViewModel.validateSecurityQuestionAnswers().observe(this,
                        result -> {
                            showLoadingDialog(result);
                            switch (result.status) {
                                case SUCCESS:
                                    mForgotPasswordNavigationManager.showForgotPasswordChangePasswordPage(result.data);
                                    break;
                                case CLIENT_ERROR:
                                    showAlertDialog(result.message, null);
                                    break;
                                case SERVER_ERROR:
                                    checkAccountStatus(() -> toggleEditTextError(true));
                                    break;
                            }
                        }
                );
                break;
        }
    }

    /**
     * Executes the supplied runnable if the account is not locked.
     *
     * @param ifNormal runnable containing behavior if account is not locked
     */
    private void checkAccountStatus(Runnable ifNormal) {
        mForgotPasswordViewModel.getAccountStatus(mForgotPasswordViewModel.getUsername()).observe(this,
                result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            ifNormal.run();
                            break;
                        case SERVER_ERROR:
                            String message;
                            if (result.data.count < 7) {
                                message = getString(R.string.login_password_error_try_again);
                            } else {
                                message = getString(R.string.login_password_error_locked, result.message);
                            }
                            showAlertDialog(message, (dialog, which) -> {
                                dialog.dismiss();
                                mNavigationManager.showLoginPage();
                            });
                            break;
                    }
                });
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        InputUtils.enableView(mButtonNext, checkIfCompleted());
        toggleEditTextError(false);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
