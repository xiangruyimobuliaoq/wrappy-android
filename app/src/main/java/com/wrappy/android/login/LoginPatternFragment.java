package com.wrappy.android.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itsxtt.patternlock.PatternLockView;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.server.account.body.request.SendRecoveryEmailBody;
import com.wrappy.android.xmpp.XMPPRepository;

import java.util.ArrayList;

import javax.inject.Inject;

public class LoginPatternFragment extends SubFragment implements OnClickListener {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    LoginViewModel mLoginViewModel;

    private PatternLockView mPatternLockViewPattern;

    private TextView mTextViewForgotPattern;
    private TextView mTextViewPatternError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_login_pattern_page, container, false);

        mPatternLockViewPattern = view.findViewById(R.id.login_patternlockview_pattern);
        mTextViewForgotPattern = view.findViewById(R.id.login_pattern_forgot_text);
        mTextViewPatternError = view.findViewById(R.id.login_pattern_error_text);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mLoginViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(LoginViewModel.class);

        showToolbarBackButton(false);

        mTextViewForgotPattern.setOnClickListener(this);

        mPatternLockViewPattern.setOnPatternListener(new PatternLockView.OnPatternListener() {
            @Override
            public void onStarted() {
                mTextViewPatternError.setText("");
            }

            @Override
            public void onProgress(ArrayList<Integer> arrayList) {

            }

            @Override
            public boolean onComplete(ArrayList<Integer> arrayList) {
                if (arrayList.size() < 4) {
                    mTextViewPatternError.setText(R.string.pattern_error_incorrect);
                    return false;
                }

                String pattern = "";
                for (Integer password : arrayList) {
                    pattern += String.valueOf(password);
                }
                validatePatternPassword(pattern);
                return true;
            }
        });

    }

    private void validatePatternPassword(String patternPassword) {
        mLoginViewModel.validatePatternPassword(patternPassword).observe(LoginPatternFragment.this,
                result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            mLoginViewModel.finishPendingLogin();
                            mLoginViewModel.getConnectionStatus().observe(this, connectionStatus -> {
                                showLoadingDialog(connectionStatus);
                                switch (connectionStatus.status) {
                                    // TODO: LOADING SCREEN
                                    case SUCCESS:
                                        if(connectionStatus.data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                                            mNavigationManager.showHomePage(false);
                                        }
                                        break;
                                    case LOADING:
                                        break;
                                    case CLIENT_ERROR:
                                        if(connectionStatus.data!=XMPPRepository.ConnectionStatus.RECONNECTING) {
                                            showAlertDialog(connectionStatus.message, null);
                                        }
                                        break;
                                    case SERVER_ERROR:
                                        showAlertDialog(connectionStatus.message,null);
                                        break;
                                }
                            });
                            mLoginViewModel.loginXMPP();
                            break;
                        case SERVER_ERROR:
                            mTextViewPatternError.setText(R.string.pattern_error_incorrect);
                            break;
                        case CLIENT_ERROR:
                            mTextViewPatternError.setText(result.message);
                            break;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_pattern_forgot_text:
                mNavigationManager.showForgotAnswerPage(SendRecoveryEmailBody.TYPE_PATTERN);
                break;
        }
    }
}
