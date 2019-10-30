package com.wrappy.android.security;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

import javax.inject.Inject;

public class SecurityPageFragment extends SubFragment implements OnClickListener, OnCheckedChangeListener {

    @Inject
    SecurityNavigationManager mSecurityNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;

    private Button mButtonChangeQuestion;
    private Button mButtonChangePassword;
    private Button mButtonBlockedUsers;

    private SwitchCompat mSwitchPattern;
    private TextView mButtonChangePattern;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_security_page, container, false);

        mButtonChangeQuestion = view.findViewById(R.id.settings_button_change_secret_question);
        mButtonChangePassword = view.findViewById(R.id.settings_button_change_password);
        mButtonChangePattern = view.findViewById(R.id.settings_button_change_pattern);
        mButtonBlockedUsers = view.findViewById(R.id.settings_button_blocked_users);

        mSwitchPattern = view.findViewById(R.id.settings_switch_pattern);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);

        setToolbarTitle(getString(R.string.security));
        showToolbar(true);

        mButtonChangeQuestion.setOnClickListener(this);
        mButtonChangePassword.setOnClickListener(this);
        mButtonBlockedUsers.setOnClickListener(this);

        mButtonChangePattern.setOnClickListener(this);

        InputUtils.enableView(mButtonChangePattern, mSecurityViewModel.getViewPatternPasswordFlag());
        setButtonChangePatternText(mSecurityViewModel.getViewPatternPasswordFlag());
    }

    @Override
    public void onResume() {
        super.onResume();
        setPatternSwitchChecked(mSecurityViewModel.getViewPatternPasswordFlag());
    }

    private void setPatternSwitchChecked(boolean isChecked) {
        mSwitchPattern.setOnCheckedChangeListener(null);
        mSwitchPattern.setChecked(isChecked);
        mSwitchPattern.setOnCheckedChangeListener(this);
    }

    private void setButtonChangePatternText(boolean patternEnabled) {
        if (patternEnabled) {
            mButtonChangePattern.setText(R.string.link_pattern_register);
        } else {
            mButtonChangePattern.setText(R.string.link_pattern_register_disabled);
        }
    }

    private void setPatternPasswordFlag(boolean flag) {
        mSecurityViewModel.setPatternPasswordFlag(flag).observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mSecurityViewModel.setViewPatternPasswordFlag(flag);
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    boolean patternEnabled = mSecurityViewModel.getViewPatternPasswordFlag();
                    setPatternSwitchChecked(patternEnabled);
                    InputUtils.enableView(mButtonChangePattern, patternEnabled);
                    setButtonChangePatternText(patternEnabled);
                    break;
            }
        });
    }

    private void requestAllSecurityQuestions() {
        mSecurityViewModel.requestAllQuestionList().observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    requestUserSecurityQuestionsWithAnswer();
                    break;
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    showAlertDialog(result.message, null);
                    break;
            }
        });
    }

    private void requestUserSecurityQuestionsWithAnswer() {
        mSecurityViewModel.requestUserQuestionWithAnswerList().observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mSecurityNavigationManager.showChooseQuestionPage();
                    break;
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    showAlertDialog(result.message, null);
                    break;
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.settings_button_change_secret_question:
                requestAllSecurityQuestions();
                break;
            case R.id.settings_button_change_password:
                mSecurityNavigationManager.showChangePasswordPage();
                break;
            case R.id.settings_button_blocked_users:
                mSecurityNavigationManager.showBlockedUsersPage();
                break;
            case R.id.settings_button_change_pattern:
                mSecurityNavigationManager.showCreatePatternPage();
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (TextUtils.isEmpty(mSecurityViewModel.getViewPatternPassword())) {
                mSecurityNavigationManager.showCreatePatternPage();
            } else {
                setPatternPasswordFlag(true);
            }
        } else {
            setPatternPasswordFlag(false);
        }
    }
}
