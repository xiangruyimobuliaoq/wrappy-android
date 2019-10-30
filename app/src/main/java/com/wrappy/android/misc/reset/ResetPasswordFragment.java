package com.wrappy.android.misc.reset;


import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.wrappy.android.MainActivity;
import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;

public class ResetPasswordFragment extends BaseFragment {
    public static final String KEY_EXTRA_MODE = "reset_key_extra_mode";
    public static final String KEY_EXTRA_SECRET = "reset_key_extra_secret";
    public static final String MODE_TEXT = "text_password";
    public static final String MODE_PATTERN = "pattern_password";

    @Inject
    ResetPasswordNavigationManager mResetPasswordNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ResetPasswordViewModel mViewModel;

    private Toolbar mToolbar;

    private boolean mIsShown;

    public static ResetPasswordFragment create(String mode, String secretKey) {
        ResetPasswordFragment f = new ResetPasswordFragment();
        Bundle b = new Bundle();
        b.putString(KEY_EXTRA_MODE, mode);
        b.putString(KEY_EXTRA_SECRET, secretKey);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_main, container, false);

        mToolbar = view.findViewById(R.id.toolbar);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ResetPasswordViewModel.class);

        mViewModel.logoutXMPP().observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mViewModel.logoutAccount();
                    break;
            }
        });

        mViewModel.setSecretKey(getArguments().getString(KEY_EXTRA_SECRET));

        setToolbar(mToolbar);
        showToolbar(true);
        showToolbarBackButton(true);
        setToolbarTitle("Reset Password");
        if (!mIsShown) {
            if (getArguments().getString(KEY_EXTRA_MODE).equals(MODE_PATTERN)) {
                mResetPasswordNavigationManager.showResetPatternPage();
            } else {
                mResetPasswordNavigationManager.showResetTextPasswordPage();
            }
            mIsShown = true;
        }
    }

    @Override
    public boolean onBackPressed() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_RESET_DONE);
        startActivity(intent);
        return true;
    }
}
