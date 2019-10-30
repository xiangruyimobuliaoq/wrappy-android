package com.wrappy.android.home.profile;


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

public class EditProfileFragment extends BaseFragment {
    private static final String KEY_EXTRA_NAME = "name";
    private static final String KEY_EXTRA_USER_ID = "user_id";
    private static final String KEY_EXTRA_PHONE_NUMBER = "phone_number";
    private static final String KEY_EXTRA_COUNTRY_CODE = "country_code";
    private static final String KEY_EXTRA_EMAIL = "email";
    private static final String KEY_EXTRA_PROFILE_URL = "profile_url";
    private static final String KEY_EXTRA_BANNER_URL = "banner_url";

    @Inject
    EditProfileNavigationManager mEditProfileNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    EditProfileViewModel mEditProfileViewModel;

    private Toolbar mToolbar;
    private boolean mIsShown;

    public static EditProfileFragment create(String name,
                                             String userId,
                                             String countryCode,
                                             String phoneNumber,
                                             String email,
                                             String profileUrl,
                                             String bannerUrl) {
        EditProfileFragment f = new EditProfileFragment();
        Bundle b = new Bundle();
        b.putString(KEY_EXTRA_NAME, name);
        b.putString(KEY_EXTRA_USER_ID, userId);
        b.putString(KEY_EXTRA_COUNTRY_CODE, countryCode);
        b.putString(KEY_EXTRA_PHONE_NUMBER, phoneNumber);
        b.putString(KEY_EXTRA_EMAIL, email);
        b.putString(KEY_EXTRA_PROFILE_URL, profileUrl);
        b.putString(KEY_EXTRA_BANNER_URL, bannerUrl);
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
        mEditProfileViewModel = ViewModelProviders.of(this, mViewModelFactory).get(EditProfileViewModel.class);

        mEditProfileViewModel.setName(getArguments().getString(KEY_EXTRA_NAME));
        mEditProfileViewModel.setUserId(getArguments().getString(KEY_EXTRA_USER_ID));
        mEditProfileViewModel.setCountryCode(getArguments().getString(KEY_EXTRA_COUNTRY_CODE));
        mEditProfileViewModel.setPhoneNumber(getArguments().getString(KEY_EXTRA_PHONE_NUMBER));
        mEditProfileViewModel.setEmail(getArguments().getString(KEY_EXTRA_EMAIL));
        mEditProfileViewModel.setProfileUrl(getArguments().getString(KEY_EXTRA_PROFILE_URL));
        mEditProfileViewModel.setBannerUrl(getArguments().getString(KEY_EXTRA_BANNER_URL));

        setToolbar(mToolbar);
        setToolbarTitle(getResources().getString(R.string.edit_my_page));
        showToolbarBackButton(true);

        if (!mIsShown) {
            mEditProfileNavigationManager.showEditProfileInfoPage();
            mIsShown = true;
        }
    }

}
