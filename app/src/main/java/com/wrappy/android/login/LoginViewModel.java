package com.wrappy.android.login;

import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.AccountBody;
import com.wrappy.android.server.account.body.request.ExtendedInfoBody;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.server.account.body.response.AccountStatusResponse;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;


public class LoginViewModel extends ViewModel {
    private AuthRepository mAuthRepository;
    private AccountRepository mAccountRepository;
    private XMPPRepository mXmppRepository;

    private String mUsername;

    private String mPassword;
    public LoginViewModel(AuthRepository authRepository, AccountRepository accountRepository, XMPPRepository xmppRepository) {
        mAuthRepository = authRepository;
        mAccountRepository = accountRepository;
        mXmppRepository = xmppRepository;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public LiveData<Resource<AccountBody>> login() {
        return mAuthRepository.login(mUsername, mPassword);
    }

    public LiveData<Resource<AccountStatusResponse>> getAccountStatus(String username) {
        return mAccountRepository.getAccountStatus(username);
    }

    public LiveData<Resource<Boolean>> validatePhoneAndSendSmsCode(String countryCode, String phoneNumber) {
        return mAccountRepository.validatePhone(countryCode, phoneNumber, true, true);
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> getUserSecurityQuestions() {
        return mAccountRepository.getUserSecurityQuestions(mUsername, 3);
    }

    public LiveData<Resource<Boolean>> findUserAccount(String email, String phone) {
        return mAccountRepository.forgotUserId(email, phone, true, true);
    }

    public LiveData<Resource<Boolean>> validatePatternPassword(String patternPassword) {
        return mAccountRepository.validatePatternPassword(patternPassword);
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> getConnectionStatus() {
        return mXmppRepository.getConnectionStatus();
    }

    public void loginXMPP() {
        mXmppRepository.loginXMPP(mUsername, mPassword);
    }

    public void finishPendingLogin() {
        mAuthRepository.finishPendingLogin();
    }

    public LiveData<Resource<List<RoomMUCExtend>>> getMUCRooms() {
        return mXmppRepository.getMUCRooms();
    }
}
