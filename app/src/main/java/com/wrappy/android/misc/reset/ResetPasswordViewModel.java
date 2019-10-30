package com.wrappy.android.misc.reset;

import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.xmpp.XMPPRepository;


public class ResetPasswordViewModel extends ViewModel {
    private AccountRepository mAccountRepository;
    private AuthRepository mAuthRepository;
    private XMPPRepository mXmppRepository;

    private String mSecretKey;

    public ResetPasswordViewModel(AccountRepository accountRepository, AuthRepository authRepository, XMPPRepository xmppRepository) {
        mAccountRepository = accountRepository;
        mAuthRepository = authRepository;
        mXmppRepository = xmppRepository;
    }

    public String getSecretKey() {
        return mSecretKey;
    }

    public void setSecretKey(String secretKey) {
        this.mSecretKey = secretKey;
    }

    public LiveData<Resource<List<String>>> validatePasswordFormat(String newPassword, ServerLocale language) {
        return mAccountRepository.validatePasswordFormat(newPassword, language);
    }

    public LiveData<Resource<Boolean>> resetUserPassword(String secretKey, String newPassword, String newPatternPassword) {
        return mAccountRepository.resetUserPassword(secretKey, newPassword, newPatternPassword);
    }

    public void logoutAccount() {
        mAuthRepository.logout();
    }

    public LiveData<Resource<Boolean>> logoutXMPP() {
        return mXmppRepository.logoutXMPP();
    }
}
