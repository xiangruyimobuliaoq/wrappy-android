package com.wrappy.android.security;


import java.util.ArrayList;
import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.Resource.Status;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.xmpp.XMPPRepository;

import org.jxmpp.jid.Jid;

public class SecurityViewModel extends ViewModel {

    private XMPPRepository mXMPPRepository;
    private AuthRepository mAuthRepository;
    private AccountRepository mAccountRepository;

    private String mPassword;
    private String mPatternPassword;
    private boolean mPatternPasswordFlag;

    private List<SecurityQuestionAnswerBody> mUserQuestionList = new ArrayList<>();
    private List<SecurityQuestionAnswerBody> mAllSecurityQuestionList = new ArrayList<>();

    public SecurityViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAuthRepository = authRepository;
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<Boolean>> validateTextPassword(String password) {
        return mAccountRepository.validateTextPassword(password);
    }

    public LiveData<Resource<Boolean>> validatePatternPassword(String patternPassword) {
        return mAccountRepository.validatePatternPassword(patternPassword);
    }

    public String getViewPassword() {
        return mPassword;
    }

    public void setViewPassword(String password) {
        mPassword = password;
    }

    public String getViewPatternPassword() {
        return mPatternPassword;
    }

    public void setViewPatternPassword(String patternPassword) {
        mPatternPassword = patternPassword;
    }

    public boolean getViewPatternPasswordFlag() {
        return mPatternPasswordFlag;
    }

    public void setViewPatternPasswordFlag(boolean flag) {
        mPatternPasswordFlag = flag;
    }

    public List<SecurityQuestionAnswerBody> getViewUserQuestionList() {
        return mUserQuestionList;
    }

    public void setViewUserQuestionList(List<SecurityQuestionAnswerBody> questionList) {
        mUserQuestionList.clear();
        if (questionList != null) {
            mUserQuestionList.addAll(questionList);
        }
    }

    public List<SecurityQuestionAnswerBody> getAllQuestionList() {
        return mAllSecurityQuestionList;
    }

    private void setAllQuestionList(List<SecurityQuestionAnswerBody> questionList) {
        mAllSecurityQuestionList.clear();
        if (questionList != null) {
            mAllSecurityQuestionList.addAll(questionList);
        }
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> requestAllQuestionList() {
        return Transformations.map(
                mAccountRepository.getSecurityQuestions(ServerLocale.ENGLISH),
                result -> {
                    if (result.status.equals(Status.SUCCESS)) {
                        setAllQuestionList(result.data);
                    }
                    return result;
                });
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> requestUserQuestionWithAnswerList() {
        return Transformations.map(
                mAccountRepository.getUserSecurityQuestionsWithAnswer(mPassword),
                result -> {
                    if (result.status.equals(Status.SUCCESS)) {
                        setViewUserQuestionList(result.data);
                    }
                    return result;
                });
    }

    public void logoutAccount() {
        mAuthRepository.logout();
    }

    public LiveData<Resource<List<String>>> updateUserPassword(String newPassword) {
        return mAccountRepository.updateUserPassword(mPassword, newPassword);
    }

    public LiveData<Resource<Boolean>> updatePatternPassword(String pattern) {
        return mAccountRepository.updatePatternPassword(pattern);
    }

    public LiveData<Resource<Boolean>> updateUserSecurityQuestions(List<SecurityQuestionAnswerBody> questionList) {
        return mAccountRepository.updateUserSecurityQuestions(questionList);
    }

    public LiveData<Resource<Boolean>> setPatternPasswordFlag(boolean flag) {
        return mAccountRepository.setPatternPasswordFlag(flag);
    }

    public LiveData<List<Block>> getBlockList() {
        return mXMPPRepository.getBlockList();
    }

    public LiveData<List<Block>> getBlockListQuery(String query) {
        return mXMPPRepository.getBlockListQuery(query);
    }

    public List<Jid> getBlockListasJid() {
        return mXMPPRepository.getBlockListasJid();
    }

    public void unblockContact(List<Block> userJids) {
        mXMPPRepository.unblockContact(userJids);
    }

    public String getContactFileUrl(FileBody.Type fileTypeAvatar, String contactId) {
        return mAccountRepository.getContactFileUrl(fileTypeAvatar, contactId, false);
    }
}
