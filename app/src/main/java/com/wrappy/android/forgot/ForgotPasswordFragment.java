package com.wrappy.android.forgot;

import java.util.ArrayList;
import java.util.List;
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
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

public class ForgotPasswordFragment extends BaseFragment {
    public static final String EXTRA_QUESTION_LIST = "list";
    public static final String EXTRA_USER = "user";

    @Inject
    ForgotPasswordNavigationManager mForgotPasswordNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    ForgotPasswordViewModel mForgotPasswordViewModel;

    private Toolbar mToolbar;
    private boolean mIsShown;

    public static ForgotPasswordFragment create(String username, List<SecurityQuestionAnswerBody> questionList) {
        ForgotPasswordFragment f = new ForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_USER, username);
        args.putParcelableArrayList(EXTRA_QUESTION_LIST, (ArrayList<SecurityQuestionAnswerBody>) questionList);
        f.setArguments(args);
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
        mForgotPasswordViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ForgotPasswordViewModel.class);
        mForgotPasswordViewModel.setUsername(getArguments().getString(EXTRA_USER));
        mForgotPasswordViewModel.setQuestionList(getArguments().getParcelableArrayList(EXTRA_QUESTION_LIST));

        setToolbar(mToolbar);
        setToolbarTitle(getString(R.string.forgot_toolbar_title));
        showToolbarBackButton(true);
        if (!mIsShown) {
            mForgotPasswordNavigationManager.showForgotPasswordQuestionPage();
            mIsShown = true;
        }
    }
}
