package com.wrappy.android.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;

import java.util.ArrayList;

import javax.inject.Inject;

public class ChatFragment extends BaseFragment {

    private Toolbar mToolbar;
    private boolean mIsShown;

    public static final String KEY_JID = "CHAT_JID";
    public static final String KEY_NAME = "CHAT_NAME";
    public static final String KEY_TYPE = "CHAT_TYPE";
    public static final String KEY_MEMBER = "CHAT_MEMBER";

    @Inject
    ChatNavigationManager mChatNavigationManager;

    public static ChatFragment create(String chatJid, String chatName, String chatType) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, chatJid);
        args.putString(KEY_NAME, chatName);
        args.putString(KEY_TYPE, chatType);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_base, container, false);
        mToolbar = view.findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        setToolbar(mToolbar);
        setToolbarTitle("");
        showToolbarBackButton(true);
        if (!mIsShown) {
            mChatNavigationManager.showChatMessagePage(getArguments().getString(KEY_JID), getArguments().getString(KEY_NAME), getArguments().getString(KEY_TYPE));
            mIsShown = true;
        }
    }

}
