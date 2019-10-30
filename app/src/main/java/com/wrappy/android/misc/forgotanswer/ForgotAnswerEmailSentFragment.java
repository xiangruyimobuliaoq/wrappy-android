package com.wrappy.android.misc.forgotanswer;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;


public class ForgotAnswerEmailSentFragment extends SubFragment {

    @Inject
    NavigationManager mNavigationManager;

    private Button mBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_forgot_answer_email_sent, container, false);
        setToolbarTitle("");
        mBackButton = view.findViewById(R.id.forgot_answer_button_back_top);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mBackButton.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
    }

    @Override
    public boolean onBackPressed() {
        mNavigationManager.showWelcomePage();
        return true;
    }
}
