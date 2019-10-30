package com.wrappy.android.welcome;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.server.ServerConstants.ServerLocale;

import javax.inject.Inject;

public class WelcomeFragment extends BaseFragment implements View.OnClickListener {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    WelcomeViewModel mWelcomeViewModel;

    private ViewPager mViewPagerIntro;

    private Button mButtonStart;

    private TextView mTextViewTerms;

    private String mTCContent;

    private float mTCContentVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_welcome_page, container, false);
        mViewPagerIntro = view.findViewById(R.id.welcome_viewpager_intro);
        mButtonStart = view.findViewById(R.id.welcome_button_start);
        mTextViewTerms = view.findViewById(R.id.welcome_textview_terms);



        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mWelcomeViewModel = ViewModelProviders.of(this, mViewModelFactory).get(WelcomeViewModel.class);

        mTextViewTerms.setOnClickListener(this);

        mViewPagerIntro.setAdapter(new IntroPagerAdapter(getChildFragmentManager()));

        mButtonStart.setOnClickListener(this);
        mButtonStart.setEnabled(false);

        checkTermsAndConditionsVersion();
    }

    private void checkTermsAndConditionsVersion() {
        mWelcomeViewModel.getLatestTCVersion().observe(this, result -> {
            showLoadingDialog(result);
            switch(result.status) {
                case SUCCESS:
                    if (!mWelcomeViewModel.isTermsAndConditionsAccepted()) {
                        loadTermsAndConditions();
                    } else {
                        mButtonStart.setEnabled(mWelcomeViewModel.isTermsAndConditionsAccepted());
                    }
                    break;
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    showRetryDialog(result.message, (dialog, which) -> {
                        dialog.dismiss();
                        checkTermsAndConditionsVersion();
                    });
                    break;
            }

        });
    }

    private void loadTermsAndConditions() {
        if (!TextUtils.isEmpty(mTCContent)) {
            showTermsAndConditionsDialog();
        } else {
            mWelcomeViewModel.getTermsAndConditions(ServerLocale.ENGLISH)
                    .observe(this, result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case SUCCESS:
                                mTCContent = result.data;
                                showTermsAndConditionsDialog();
                                break;
                            case CLIENT_ERROR:
                            case SERVER_ERROR:
                                showRetryDialog(result.message, (dialog, which) -> {
                                    dialog.dismiss();
                                    loadTermsAndConditions();
                                });
                                break;
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.welcome_button_start:
                mNavigationManager.showLoginPage();
                break;

            case R.id.welcome_textview_terms:
                loadTermsAndConditions();
                break;
        }
    }

    private void showRetryDialog(String message, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message)
                .setCancelable(false);
        if (clickListener == null) {
            clickListener = (dialog, id) -> dialog.dismiss();
        }
        builder.setPositiveButton(R.string.dialog_retry, clickListener);
        builder.show();
    }

    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder mAlertDialogToS = new AlertDialog.Builder(getContext());
        mAlertDialogToS.setMessage(mTCContent);

        mAlertDialogToS.setCustomTitle(getLayoutInflater().inflate(R.layout.frag_welcome_tc_title, null));

        if (mWelcomeViewModel.isTermsAndConditionsAccepted()) {
            mAlertDialogToS.setPositiveButton(R.string.dialog_ok, null);
        } else {
            mAlertDialogToS.setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                mWelcomeViewModel.acceptTermsAndConditions();
                mButtonStart.setEnabled(true);
            });
            mAlertDialogToS.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> mButtonStart.setEnabled(false));
        }
        mAlertDialogToS.show();
    }


    private class IntroPagerAdapter extends FragmentStatePagerAdapter {
        String[] mTitleArray, mDescArray, mImageArray;

        public IntroPagerAdapter(FragmentManager fm) {
            super(fm);
            mImageArray = getResources().getStringArray(R.array.intro_page_image);
            mTitleArray = getResources().getStringArray(R.array.intro_page_title);
            mDescArray = getResources().getStringArray(R.array.intro_page_description);
        }

        @Override
        public Fragment getItem(int position) {
            int drawableId = getResources().getIdentifier(mImageArray[position],
                    "drawable",
                    getActivity().getPackageName());
            return IntroItemFragment.create(drawableId,
                    mTitleArray[position],
                    mDescArray[position]);
        }

        @Override
        public int getCount() {
            return 9;
        }
    }

}

