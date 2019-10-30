package com.wrappy.android.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.wrappy.android.common.Resource;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.db.entity.AccountInfo;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private XMPPRepository mXMPPRepository;
    private AuthRepository mAuthRepository;
    private AccountRepository mAccountRepository;

    public HomeViewModel(XMPPRepository xmppRepository,
                         AuthRepository authRepository,
                         AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAuthRepository = authRepository;
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<AccountInfo>> getAccountInfo() {
        return mAccountRepository.getCurrentAccountInfo();
    }

    public LiveData<Resource<Boolean>> uploadFile(FileBody.Type fileType, Uri imageUri) {
        return mAccountRepository.saveUserFile(fileType, new Base64ImageFile(imageUri));
    }

    public String getUserFileUrl(FileBody.Type fileTypeAvatar) {
        return mAccountRepository.getUserFileUrl(fileTypeAvatar);
    }

    public String getContactFileUrl(FileBody.Type fileTypeAvatar, String userName) {
        return getContactFileUrl(fileTypeAvatar, userName, false);
    }

    public String getContactFileUrl(FileBody.Type fileTypeAvatar, String userName, boolean isGroup) {
        return mAccountRepository.getContactFileUrl(fileTypeAvatar, userName, isGroup);
    }

    public void logoutAccount() {
        mAuthRepository.logout();
    }

    public LiveData<Resource<Boolean>> logoutXMPP() {
        return mXMPPRepository.logoutXMPP();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return mXMPPRepository.getAllContacts();
    }

    public LiveData<List<Contact>> getContacts() {
        return mXMPPRepository.getContacts();
    }

    public void initMessages(Fragment fragment) {
        mXMPPRepository.initMessages(fragment);
    }

    public LiveData<List<Contact>> getQueryContacts(String query) {
        return mXMPPRepository.getQueryContacts(query);
    }

    public LiveData<String> getUserJid() {
        return mXMPPRepository.getUserJid();
    }

    public LiveData<List<Chat>> getChatList(String whoIs) {
        return mXMPPRepository.getChatList(whoIs);
    }

    public LiveData<List<Chat>> getOnetoOneChatList(String whoIs) {
        return mXMPPRepository.getOnetoOneChatList(whoIs);
    }

    public LiveData<List<Chat>> getQueryOnetoOneChatList(String query, String whoIs) {
        return mXMPPRepository.getQueryOnetoOneChatList(query, whoIs);
    }

    public LiveData<List<Chat>> getGroupChatList(String whoIs) {
        return mXMPPRepository.getGroupChatList(whoIs);
    }

    public LiveData<List<Chat>> getQueryGroupChatList(String query, String whoIs) {
        return mXMPPRepository.getQueryGroupChatList(query, whoIs);
    }

    public LiveData<Resource<List<RoomMUCExtend>>> getMUCRooms() {
        return mXMPPRepository.getMUCRooms();
    }

    public String getUserId() {
        return mXMPPRepository.showUserName();
    }

    public void acceptRequest(String userJid) {
        mXMPPRepository.acceptContact(userJid);
    }

    public void removeContact(String userJid) {
        mXMPPRepository.removeContact(userJid);
    }

    public void cancelRequest(String userJid) {
        mXMPPRepository.cancelRequest(userJid);
    }

    public LiveData<Resource<String>> createMUC(List<String> userJids) {
        return mXMPPRepository.createMUC(userJids);
    }

    public String getRoomName() {
        return mXMPPRepository.getRoomName();
    }

    public LiveData<String> getContactStatus(String userJid) {
        return mXMPPRepository.getContactStatus(userJid);
    }

    public LiveData<List<Chat>> getQueryChats(String query, String whoIs) {
        return mXMPPRepository.getQueryChats(query, whoIs);
    }

    public void deleteMessages(String chatId, String type) {
        mXMPPRepository.deleteMessages(chatId, type);
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> getConnectionStatus() {
        return mXMPPRepository.getConnectionStatus();
    }

}
