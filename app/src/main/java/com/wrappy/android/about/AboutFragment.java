package com.wrappy.android.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.common.SubFragment;

import javax.inject.Inject;

public class AboutFragment extends BaseFragment{

    @Inject
    AboutNavigationManager mAboutNavigationManager;

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
        showToolbar(true);
        showToolbarBackButton(true);
        if (!mIsShown) {
            mAboutNavigationManager.showAboutPage();
            mIsShown = true;
        }

    }
}
