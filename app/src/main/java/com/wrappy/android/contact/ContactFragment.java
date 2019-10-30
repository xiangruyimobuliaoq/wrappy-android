package com.wrappy.android.contact;

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

public class ContactFragment extends BaseFragment {

    Toolbar mToolbar;

    static final String KEY_TYPE = "Type";
    static final String KEY_JID = "JID";

    public static final int TYPE_ADD = 1;
    public static final int TYPE_EDIT = 0;

    boolean mIsShown;

    @Inject
    ContactNavigationManager mContactNavigationManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_main, container, false);
        mToolbar = view.findViewById(R.id.toolbar);

        return view;
    }

    public static ContactFragment create(int type, String userJid) {
        ContactFragment contactFragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, type);
        args.putString(KEY_JID, userJid);
        contactFragment.setArguments(args);
        return contactFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        setToolbar(mToolbar);
        setToolbarTitle(getString(R.string.forgot_toolbar_title));
        showToolbarBackButton(true);
        if(!mIsShown) {
            if(getArguments().getInt(KEY_TYPE)==TYPE_EDIT) {
                mContactNavigationManager.showContactEditFriend(getArguments().getString(KEY_JID));
            } else if(getArguments().getInt(KEY_TYPE)==TYPE_ADD) {
                mContactNavigationManager.showContactAddFriend(getArguments().getString(KEY_JID));
            }
        }

    }
}
