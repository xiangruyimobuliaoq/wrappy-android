package com.wrappy.android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.wrappy.android.common.Resource;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.AuthRepository.LoginStatus;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;

import java.util.List;


public class MainActivityViewModel extends ViewModel {
    private XMPPRepository mXMPPRepository;
    private AuthRepository mAuthRepository;
    private AccountRepository mAccountRepository;
    private boolean mForgotLogout;

    public MainActivityViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAuthRepository = authRepository;
        mAccountRepository = accountRepository;
    }

    public LiveData<LoginStatus> getLoginStatus() {
        return mAuthRepository.getLoginStatus();
    }

    public void checkLogin() {
        mAuthRepository.checkUserLoginToken();
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> getConnectionStatus() {
        return mXMPPRepository.getConnectionStatus();
    }

    public void loginXMPP() {
        mXMPPRepository.loginXMPP(
                mAuthRepository.getLocalUsername(),
                mAuthRepository.getLocalPassword());
    }

    public void disconnectXMPP() {
        mXMPPRepository.disconnectXMPP();
    }

    public boolean isConnectionAuth() {
        return mXMPPRepository.isConnectionAuth();
    }

    public void logoutXMPP() {
        mXMPPRepository.logoutXMPP();
    }

    public LiveData<Resource<List<RoomMUCExtend>>> getMUCRooms() {
        return mXMPPRepository.getMUCRooms();
    }

    public void initFirebase() {
        mXMPPRepository.initFirebase();
    }

    public boolean isForgotLogout() {
        return mForgotLogout;
    }

    public void setForgotLogout(boolean forgotLogout) {
        mForgotLogout = forgotLogout;
    }

    @Override
    protected void onCleared() {
        mXMPPRepository.disconnectXMPP();
        super.onCleared();
    }
}
