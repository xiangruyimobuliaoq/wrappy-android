package com.wrappy.android.misc.forgotanswer;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.account.AccountRepository;

public class ForgotAnswerViewModel extends ViewModel {
    private AccountRepository mAccountRepository;
    private String mRecoveryType;

    public ForgotAnswerViewModel(AccountRepository accountRepository) {
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<Boolean>> sendRecoveryEmail(String email, String type) {
        return mAccountRepository.sendRecoveryEmail(email, type);
    }

    public void setRecoveryType(String recoveryType) {
        this.mRecoveryType = recoveryType;
    }

    public String getRecoveryType() {
        return mRecoveryType;
    }
}
