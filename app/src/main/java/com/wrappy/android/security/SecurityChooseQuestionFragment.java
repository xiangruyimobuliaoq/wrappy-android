package com.wrappy.android.security;

import java.util.List;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.R;
import com.wrappy.android.common.ui.AbstractSecurityQuestionFragment;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

public class SecurityChooseQuestionFragment extends AbstractSecurityQuestionFragment {
    @Inject
    SecurityNavigationManager mSecurityNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    SecurityViewModel mSecurityViewModel;

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);

        setToolbarTitle(getString(R.string.security_label_change_secret_question));
    }

    @Override
    protected String getNextButtonText() {
        return getString(R.string.save);
    }

    @Override
    protected List<SecurityQuestionAnswerBody> getSelectedQuestions() {
        return mSecurityViewModel.getViewUserQuestionList();
    }

    @Override
    protected List<SecurityQuestionAnswerBody> getQuestions() {
        return mSecurityViewModel.getAllQuestionList();
    }

    @Override
    protected void onSubmitQuestionAnswers(List<SecurityQuestionAnswerBody> questionAnswerList) {
        mSecurityViewModel.updateUserSecurityQuestions(questionAnswerList).observe(this, result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mSecurityViewModel.setViewUserQuestionList(questionAnswerList);
                    getActivity().onBackPressed();
                    break;
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    showAlertDialog(result.message, null);
                    break;
            }
        });
    }
}
