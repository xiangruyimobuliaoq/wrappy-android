package com.wrappy.android.welcome;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.response.GetVersionResponse;


public class WelcomeViewModel extends ViewModel {
    private AccountRepository mAccountRepository;

    public WelcomeViewModel(AccountRepository accountRepository) {
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<String>> getTermsAndConditions(ServerLocale language) {
        return mAccountRepository.getTermsAndConditions(language);
    }

    public void acceptTermsAndConditions() {
        mAccountRepository.storePrefTCAccepted(true);
    }

    public boolean isTermsAndConditionsAccepted() {
        return mAccountRepository.getPrefTCAccepted();
    }

    public LiveData<Resource<GetVersionResponse>> getLatestTCVersion() {
        return mAccountRepository.getWrappyTCVersion();
    }
}
