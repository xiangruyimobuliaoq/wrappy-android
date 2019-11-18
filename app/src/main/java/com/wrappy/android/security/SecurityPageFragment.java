package com.wrappy.android.security;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
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
import com.wrappy.android.db.entity.Contact;

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
    private Button mButtonLockAccount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_security_page, container, false);

        mButtonChangeQuestion = view.findViewById(R.id.settings_button_change_secret_question);
        mButtonChangePassword = view.findViewById(R.id.settings_button_change_password);
        mButtonChangePattern = view.findViewById(R.id.settings_button_change_pattern);
        mButtonBlockedUsers = view.findViewById(R.id.settings_button_blocked_users);
        mButtonLockAccount = view.findViewById(R.id.settings_button_lock_account);

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
        mButtonLockAccount.setOnClickListener(this);
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
            case R.id.settings_button_lock_account:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("Delete Account");
                alertDialogBuilder.setMessage("You will lose all messages, lose all contacts, and can not recover data anymore. And you can not re-use this username anymore. Please make sure if you really want to delete the account.");
                alertDialogBuilder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSecurityViewModel.logoutXMPP().observe(getViewLifecycleOwner(), result -> {
                            switch (result.status) {
                                case SUCCESS:
                                    dialog.dismiss();
                                    Log.e("123", "lockAccount执行前");
                                    mSecurityViewModel.lockAccount().observe(getViewLifecycleOwner(), result1 -> {
                                        switch (result1.status) {
                                            case SUCCESS:
                                                mSecurityViewModel.logoutAccount();
                                                break;
                                        }
                                    });
                                    break;
                                case LOADING:
                                    break;
                                case CLIENT_ERROR:
                                    break;
                                case SERVER_ERROR:
                                    break;
                            }
                        });
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.dialog_no, null);
                alertDialogBuilder.show();
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
