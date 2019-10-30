package com.wrappy.android.register;


import java.util.ArrayList;
import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.Resource.Status;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.AccountBody;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.xmpp.XMPPRepository;

public class RegisterViewModel extends ViewModel {

    private XMPPRepository mXMPPRepository;

    private AuthRepository mAuthRepository;
    private AccountRepository mAccountRepository;
    private String mPhoneNumber;
    private String mCountryCode;
    private String mPassword;
    private List<SecurityQuestionAnswerBody> mQuestionList = new ArrayList<>();
    private List<SecurityQuestionAnswerBody> mQuestionAnswerList = new ArrayList<>();

    public RegisterViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAuthRepository = authRepository;
        mAccountRepository = accountRepository;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode = countryCode;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    private void setSecurityQuestions(List<SecurityQuestionAnswerBody> questionList) {
        mQuestionList.clear();
        mQuestionList.addAll(questionList);
    }

    public void setSecurityQuestionAnswers(List<SecurityQuestionAnswerBody> questionAnswerList) {
        mQuestionAnswerList.clear();
        mQuestionAnswerList.addAll(questionAnswerList);
    }

    public List<SecurityQuestionAnswerBody> getSecurityQuestions() {
        return mQuestionList;
    }

    public LiveData<Resource<Boolean>> validatePhone() {
        return mAccountRepository.validatePhone(mCountryCode, mPhoneNumber, true, true);
    }

    public LiveData<Resource<Boolean>> validateSmsCode(String smsCode) {
        return mAccountRepository.validateSmsCode(mCountryCode, mPhoneNumber, smsCode);
    }

    public LiveData<Resource<List<String>>> validatePasswordFormat(String password, ServerLocale language) {
        mPassword = password;
        return mAccountRepository.validatePasswordFormat(mPassword, language);
    }

    public LiveData<Resource<Boolean>> validateUsername(String username) {
        return mAccountRepository.validateUsername(username, mPassword);
    }

    public LiveData<Resource<Boolean>> validateEmail(String email) {
        return mAccountRepository.validateEmail(email);
    }

    public LiveData<Resource<AccountBody>> login(String username, String password) {
        return mAuthRepository.login(username, password);
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> loadSecurityQuestions(ServerLocale language) {
        return Transformations.map(mAccountRepository.getSecurityQuestions(language), result -> {
            if (result.status.equals(Status.SUCCESS)) {
                setSecurityQuestions(result.data);
            }
            return result;
        });
    }

    public LiveData<Resource<AccountBody>> createAccount(String name, String userId, String email, Uri profileImage, Uri backgroundImage) {
        return mAccountRepository.createAccount(name,
                email,
                mCountryCode + mPhoneNumber,
                userId,
                mPassword,
                profileImage != null ? new Base64ImageFile(profileImage) : null,
                backgroundImage != null ? new Base64ImageFile(backgroundImage) : null,
                ServerConstants.XMPP_SERVER,
                mQuestionAnswerList);
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> getConnectionStatus() {
        return mXMPPRepository.getConnectionStatus();
    }

    public void loginXMPP(String username, String password) {
        mXMPPRepository.loginXMPP(username, password);
    }

}
