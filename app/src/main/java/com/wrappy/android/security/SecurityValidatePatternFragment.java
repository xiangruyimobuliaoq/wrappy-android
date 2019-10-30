package com.wrappy.android.security;


import java.util.ArrayList;
import javax.inject.Inject;

import android.support.v7.app.AlertDialog;
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
import com.itsxtt.patternlock.PatternLockView.OnPatternListener;
import com.wrappy.android.MainActivityViewModel;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;

public class SecurityValidatePatternFragment extends SubFragment implements OnClickListener {

    @Inject
    SecurityNavigationManager mSecurityNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;
    MainActivityViewModel mActivityViewModel;

    private PatternLockView mPatternLockViewPattern;

    private TextView mTextViewForgotPattern;
    private TextView mTextViewPatternError;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_login_pattern_page, container, false);

        mPatternLockViewPattern = view.findViewById(R.id.login_patternlockview_pattern);
        mTextViewForgotPattern = view.findViewById(R.id.login_pattern_forgot_text);
        mTextViewPatternError = view.findViewById(R.id.login_pattern_error_text);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);
        mActivityViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MainActivityViewModel.class);

        mTextViewForgotPattern.setOnClickListener(this);
        mPatternLockViewPattern.setOnPatternListener(new OnPatternListener() {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_pattern_forgot_text:
                showLogoutConfirmation();
                break;
        }
    }

    private void validatePatternPassword(String patternPassword) {
        mSecurityViewModel.validatePatternPassword(patternPassword).observe(this,
                result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            mSecurityNavigationManager.showSecurityPage();
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
}
