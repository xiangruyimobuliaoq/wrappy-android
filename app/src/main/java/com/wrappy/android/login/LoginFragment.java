package com.wrappy.android.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;

import javax.inject.Inject;

public class LoginFragment extends BaseFragment {

    @Inject
    LoginNavigationManager mLoginNavigationManager;

    private Toolbar mToolbar;
    private boolean mIsShown;

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
        setToolbar(mToolbar);
        setToolbarTitle(getString(R.string.login_toolbar_title));
        showToolbarBackButton(true);
        if (!mIsShown) {
            mLoginNavigationManager.showLoginPage();
            mIsShown = true;
        }
    }
}
