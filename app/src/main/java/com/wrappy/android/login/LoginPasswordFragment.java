package com.wrappy.android.login;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;

public class LoginPasswordFragment extends SubFragment implements View.OnClickListener {

    @Inject
    LoginNavigationManager mLoginNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    private TextInputEditText mEditTextPassword;
    private TextView mTextViewErrorPassword;
    private TextView mTextViewForgotPassword;
    private TextView mTextViewTimeLock;
    private Button mButtonProceed;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private LoginViewModel mLoginViewModel;

    private StringBuilder mLockedTimeSb;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private long mRemainingTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_login_password_page, container, false);

        mEditTextPassword = view.findViewById(R.id.password_edittext_password);
        mTextViewErrorPassword = view.findViewById(R.id.password_textview_error_password);
        mButtonProceed = view.findViewById(R.id.password_button_proceed);
        mTextViewForgotPassword = view.findViewById(R.id.password_textview_forgot_password);
        mTextViewTimeLock = view.findViewById(R.id.password_textview_time_label);

        InputUtils.disableWhitespaceInput(mEditTextPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mLoginViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(LoginViewModel.class);

        showToolbar(true);
        setToolbarTitle(getString(R.string.login_toolbar_title));

        mButtonProceed.setOnClickListener(this);
        mTextViewForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.password_button_proceed:
                checkPassword();
                break;

            case R.id.password_textview_forgot_password:
                getSecurityQuestions();
                break;
        }
    }

    /**
     * Executes the supplied runnable if the account is not locked.
     *
     * @param ifNormal runnable containing behavior if account is not locked
     */
    private void checkAccountStatus(Runnable ifNormal) {
        mLoginViewModel.getAccountStatus(mLoginViewModel.getUsername()).observe(this,
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
                                //mTextViewTimeLock.setText( "in " + InputUtils.formatLockedTime(mLockedTimeSb, result.data.lockLeftSeconds));
                                mRemainingTime = result.data.lockLeftSeconds;
                                countLockTime();
                            } else {
                                message = getString(R.string.login_password_error_locked, result.message);
                            }
                            mTextViewErrorPassword.setText(message);
                            break;
                    }
                });
    }

    private void getSecurityQuestions() {
        checkAccountStatus(() ->
                mLoginViewModel.getUserSecurityQuestions().observe(this,
                        result -> {
                            showLoadingDialog(result);
                            switch (result.status) {
                                case SUCCESS:
                                    mNavigationManager.showForgotPasswordPage(mLoginViewModel.getUsername(), result.data);
                                    break;
                                case CLIENT_ERROR:
                                case SERVER_ERROR:
                                    showAlertDialog(result.message, null);
                                    break;
                            }
                        })
        );
    }

    private void checkPassword() {
        if (TextUtils.isEmpty(mEditTextPassword.getText().toString())) {
            mTextViewErrorPassword.setText(getString(R.string.error_incorrect_password));
        } else {
            mTextViewErrorPassword.setText("");
            mTextViewTimeLock.setText("");
            mLoginViewModel.setPassword(mEditTextPassword.getText().toString());
            mLoginViewModel.login().observe(this, result -> {
                showLoadingDialog(result);
                switch (result.status) {
                    case SUCCESS:
                        if (result.data.extendedInfo.patternPasswordFlag) {
                            mLoginNavigationManager.showPatternPage();
                        } else {
                            mLoginViewModel.getConnectionStatus().observe(this, connectionStatus -> {

                                showLoadingDialog(connectionStatus);
                                switch (connectionStatus.status) {
                                    case SUCCESS:
                                        if(connectionStatus.data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                                            mNavigationManager.showHomePage(false);
                                        }
                                        break;
                                    case LOADING:
                                        break;
                                    case CLIENT_ERROR:
                                        if(connectionStatus.data!= XMPPRepository.ConnectionStatus.RECONNECTING) {
                                            showAlertDialog(connectionStatus.message, null);
                                        }
                                        break;
                                    case SERVER_ERROR:
                                        showAlertDialog(connectionStatus.message,null);
                                        break;
                                }
                            });
                            mLoginViewModel.loginXMPP();

                        }
                        break;
                    case SERVER_ERROR:
                    case CLIENT_ERROR:
                        checkAccountStatus(() -> mTextViewErrorPassword.setText(R.string.error_incorrect_password));
                        break;

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }

    private void countLockTime() {
        mHandler.removeCallbacks(mRunnable);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mTextViewTimeLock.setText( "in " + InputUtils.formatLockedTime(mLockedTimeSb, mRemainingTime));
                if(mRemainingTime==0) {
                    mTextViewTimeLock.setText("");
                    mTextViewErrorPassword.setText("");
                    mHandler.removeCallbacks(mRunnable);
                } else {
                    mHandler.postDelayed(mRunnable, 1000L);
                    mRemainingTime--;
                }
            }
        };
        mHandler.postDelayed(mRunnable, 0L);
    }


}
