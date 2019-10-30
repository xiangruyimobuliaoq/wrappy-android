package com.wrappy.android.about;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;

public class AboutViewModel extends ViewModel {
    AccountRepository mAccountRepository;

    public AboutViewModel(AccountRepository accountRepository) {
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<String>> getShareWrappyContent() {
        return mAccountRepository.getShareWrappyContent(ServerLocale.ENGLISH);
    }
}
