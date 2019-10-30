package com.wrappy.android.register;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import com.wrappy.android.R;
import com.wrappy.android.common.ui.AbstractSecurityQuestionFragment;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

public class RegisterQuestionFragment extends AbstractSecurityQuestionFragment {
    @Inject
    RegisterNavigationManager mRegisterNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    RegisterViewModel mRegisterViewModel;

    @Override
    protected void onActivityCreatedInternal() {
        getInjector().inject(this);
        mRegisterViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(RegisterViewModel.class);
        setToolbarTitle(getString(R.string.registration));
        showToolbarBackButton(true);
    }

    @Override
    protected String getNextButtonText() {
        return null;
    }

    @Override
    protected List<SecurityQuestionAnswerBody> getSelectedQuestions() {
        return Collections.emptyList();
    }

    @Override
    protected List<SecurityQuestionAnswerBody> getQuestions() {
        return mRegisterViewModel.getSecurityQuestions();
    }

    @Override
    protected void onSubmitQuestionAnswers(List<SecurityQuestionAnswerBody> questionAnswerList) {
        mRegisterViewModel.setSecurityQuestionAnswers(questionAnswerList);
        mRegisterNavigationManager.showProfilePage();
    }


}
