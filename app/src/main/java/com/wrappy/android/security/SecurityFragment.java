package com.wrappy.android.security;

import java.util.ArrayList;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

import javax.inject.Inject;

public class SecurityFragment extends BaseFragment {
    public static final String KEY_EXTRA_PATTERN = "pattern";
    public static final String KEY_EXTRA_PATTERN_FLAG = "pattern_flag";

    @Inject
    SecurityNavigationManager mSecurityNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;

    private Toolbar mToolbar;
    private boolean mIsShown;

    public static SecurityFragment create(String patternPassword,
                                          boolean patternPasswordFlag) {
        SecurityFragment f = new SecurityFragment();
        Bundle b = new Bundle();
        b.putString(KEY_EXTRA_PATTERN, patternPassword);
        b.putBoolean(KEY_EXTRA_PATTERN_FLAG, patternPasswordFlag);
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
        mSecurityViewModel = ViewModelProviders.of(this, mViewModelFactory).get(SecurityViewModel.class);

        mSecurityViewModel.setViewPatternPassword(getArguments().getString(KEY_EXTRA_PATTERN));
        mSecurityViewModel.setViewPatternPasswordFlag(getArguments().getBoolean(KEY_EXTRA_PATTERN_FLAG));

        setToolbar(mToolbar);
        setToolbarTitle(getString(R.string.security));
        showToolbarBackButton(true);
        if (!mIsShown) {
            mSecurityNavigationManager.showValidatePasswordPage();
            mIsShown = true;
        }
    }
}
