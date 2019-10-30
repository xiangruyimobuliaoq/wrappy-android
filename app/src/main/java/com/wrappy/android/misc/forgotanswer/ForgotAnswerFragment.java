package com.wrappy.android.misc.forgotanswer;


import javax.inject.Inject;

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

public class ForgotAnswerFragment extends BaseFragment {
    private static final String KEY_RECOVERY_TYPE = "recovery_type";

    @Inject
    ForgotAnswerNavigationManager mForgotAnswerNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ForgotAnswerViewModel mViewModel;

    private Toolbar mToolbar;

    private boolean mIsShown;

    public static ForgotAnswerFragment create(String type) {
        ForgotAnswerFragment f = new ForgotAnswerFragment();
        Bundle b = new Bundle();
        b.putString(KEY_RECOVERY_TYPE, type);
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
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ForgotAnswerViewModel.class);

        setToolbar(mToolbar);
        showToolbar(true);
        showToolbarBackButton(true);
        setToolbarTitle("Email Recovery");

        mViewModel.setRecoveryType(getArguments().getString(KEY_RECOVERY_TYPE));

        if (!mIsShown) {
            mForgotAnswerNavigationManager.showEmailInputPage();
            mIsShown = true;
        }
    }
}
