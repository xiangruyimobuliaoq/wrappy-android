package com.wrappy.android.forgot;


import java.util.ArrayList;
import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.server.account.body.response.AccountStatusResponse;

public class ForgotPasswordViewModel extends ViewModel {
    private AccountRepository mAccountRepository;
    private String mUsername;

    private List<SecurityQuestionAnswerBody> mQuestionList = new ArrayList<>();
    private List<SecurityQuestionAnswerBody> mQuestionAnswerList = new ArrayList<>();

    public ForgotPasswordViewModel(AccountRepository accountRepository) {
        mAccountRepository = accountRepository;
        mQuestionList = new ArrayList<>();
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public List<SecurityQuestionAnswerBody> getQuestionList() {
        return mQuestionList;
    }

    public void setQuestionList(List<SecurityQuestionAnswerBody> questionList) {
        this.mQuestionList.clear();
        this.mQuestionList.addAll(questionList);
    }

    public void setQuestionAnswerList(List<SecurityQuestionAnswerBody> questionList) {
        this.mQuestionAnswerList.clear();
        this.mQuestionAnswerList.addAll(questionList);
    }

    public LiveData<Resource<String>> validateSecurityQuestionAnswers() {
        return mAccountRepository.validateUserSecurityQuestionAnswers(mUsername, mQuestionAnswerList);
    }

    public LiveData<Resource<AccountStatusResponse>> getAccountStatus(String username) {
        return mAccountRepository.getAccountStatus(username);
    }

    public LiveData<Resource<List<String>>> validatePasswordFormat(String password, ServerLocale language) {
        return mAccountRepository.validatePasswordFormat(password, language);
    }

    public LiveData<Resource<Boolean>> resetUserPassword(String secretKey, String newPassword) {
        return mAccountRepository.resetUserPassword(secretKey, newPassword, null);
    }
}
