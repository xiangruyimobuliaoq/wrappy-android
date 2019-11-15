package com.wrappy.android.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import android.arch.paging.PagedList;
import android.net.Uri;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.db.entity.MessageView;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.aws.AWSCertificate;

import org.jxmpp.jid.EntityBareJid;

import java.util.Date;
import java.util.List;

public class ChatViewModel extends ViewModel {

    XMPPRepository mXMPPRepository;
    AccountRepository mAccountRepository;

    private boolean isSearch;
    private boolean onChat;

    private boolean mIsGalleryShown;
    private AWSCertificate mAWSCertificate;

    public ChatViewModel(XMPPRepository xmppRepository, AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> isConnected() {
        return mXMPPRepository.isConnected();
    }

    public LiveData<Resource<ChatAndBackground>> startChat(String userJid, String chatName, String type) {
        return mXMPPRepository.startChat(userJid, chatName, type);
    }

    public LiveData<Resource<ChatAndBackground>> getChat(String chatId) {
        return mXMPPRepository.getChat(chatId);
    }

    public LiveData<Resource<List<MessageView>>> getInitialMessages(String chatJid) {
        return mXMPPRepository.getInitialMessages(chatJid);
    }

    public LiveData<String> getContactStatus(String userJid) {
        return mXMPPRepository.getContactStatus(userJid);
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

    public LiveData<Resource<Boolean>> saveGroupFile(String groupId, FileBody.Type fileType, Uri imageUri) {
        return mAccountRepository.saveGroupFile(groupId, fileType, new Base64ImageFile(imageUri));
    }

    public LiveData<MessageView> getMessages(EntityBareJid chatJid) {
        return mXMPPRepository.getMessages(chatJid);
    }

    public LiveData<Resource<String>> sendMessage(String message, String subject, String type, String chatName) {
        if(chatName.isEmpty()) {
            return mXMPPRepository.sendMessage(message, subject, type, getUserName());
        } else {
            return mXMPPRepository.sendMessage(message, subject, type, chatName);
        }
    }

    public LiveData<Resource<Contact>> getContact(String userJid) {
        return mXMPPRepository.getContact(userJid);
    }

    public LiveData<Resource<List<MessageView>>> pageChat(EntityBareJid chatJid, int offset, String lastMessageId, String type, String keyword) {
        return mXMPPRepository.pageChat(chatJid, offset, lastMessageId, type, keyword);
    }

    public List<Contact> getPariticipants(String chatJid, String type) {
        return mXMPPRepository.getParticipantsViewObject(chatJid, type);
    }

    public String getUserId() {
        return mXMPPRepository.showUserId();
    }

    public String getUserName() {
        return mXMPPRepository.showUserName();
    }

    public boolean removeMember(String userJid) {
        return mXMPPRepository.removeMember(userJid);
    }

    public LiveData<List<Contact>> getContacts() {
        return mXMPPRepository.getContacts();
    }

    public void addMember(String userJid) {
        mXMPPRepository.addMember(userJid);
    }

    public void setRoomName(String roomName) {
        mXMPPRepository.setRoomName(roomName);
    }

    public void setChatBackground(String path, String chatId) {
        mXMPPRepository.setChatBackground(path, chatId);
    }

    public LiveData<Resource<Boolean>> setChatNotification(boolean notification, String chatId, String chatType) {
        return mXMPPRepository.setChatNotification(notification, chatId, chatType);
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    public boolean getSearch() {
        return isSearch;
    }

    public LiveData<Resource<List<MessageView>>> searchMessage(EntityBareJid chatJid, String keyword, String type, String name) {
        return mXMPPRepository.searchMessage(chatJid, keyword, type, name);
    }

    public void setChatState(String chatId, String type, boolean isTyping) {
        mXMPPRepository.setChatState(chatId, type, isTyping);
    }

    public LiveData<Resource<Boolean>> isTyping(String chatId) {
        return mXMPPRepository.isTyping(chatId);
    }

    public LiveData<Resource<Boolean>> isTypingMUC() {
        return mXMPPRepository.isTypingMUC();
    }

    public LiveData<Chat> getChatUpdate(String chatId) {
        return mXMPPRepository.getChatUpdate(chatId);
    }

    public LiveData<Contact> getContactUpdate(String userId) {
        return mXMPPRepository.getContactUpdate(userId);
    }

    public void deleteMessage(String id, String message, String type, String chat_id) {
        mXMPPRepository.deleteMessage(id, message, type, chat_id);
    }

    public void deleteMessages(String chat_id, String type) {
        mXMPPRepository.deleteMessages(chat_id, type);
    }

    public LiveData<Resource<XMPPRepository.ConnectionStatus>> getConnectionStatus() {
        return mXMPPRepository.getConnectionStatus();
    }

    public AWSCertificate getAWSCertificate() {
        if (mAWSCertificate == null) {
            mAWSCertificate = mXMPPRepository.getAWSCertificate();
        }
        return mAWSCertificate;
    }

    public void setOnChat(boolean onChat) {
        this.onChat = onChat;
    }

    public boolean isOnChat() {
        return onChat;
    }

    public void updateReadStatus(String chatId, String chatType) {
        mXMPPRepository.updateReadStatus(chatId, chatType);
    }

    public void translateText(String messageId, String message, String languageCode) {
        mXMPPRepository.translateText(messageId, message, languageCode);
    }

    public void removeTranslation(String messageId) {
        mXMPPRepository.removeTranslation(messageId);
    }

    public LiveData<MessageView> getSingleMessage(String messageId) {
        return mXMPPRepository.getSingleMessage(messageId);
    }

    public boolean isSupportedLanguage(String chatLanguage) {
        return mXMPPRepository.isSupportedLanguage(chatLanguage);
    }

    public LiveData<String> setChatLanguage(String chatId, String chatLanguage) {
        return mXMPPRepository.setChatLanguage(chatId, chatLanguage);
    }

    public LiveData<Boolean> setChatAutoTranslate(String chatId, boolean autoTranslate) {
        return mXMPPRepository.setChatAutoTranslate(chatId, autoTranslate);
    }

    public LiveData<PagedList<MessageView>> getImageMessages(String chatId) {
        return mXMPPRepository.getImageMessages(chatId);
    }

    public LiveData<Integer> getImagePosition(String chatId, Date messageCreatedAt) {
        return mXMPPRepository.getImagePosition(chatId, messageCreatedAt);
    }

    public LiveData<Boolean> loadMoreImages(String chatJid, String type, String lastMessageId) {
        return mXMPPRepository.loadMoreImages(chatJid, type, lastMessageId);
    }

    public void setGalleryShown(boolean isShown) {
        mIsGalleryShown = isShown;
    }

    public boolean isGalleryShown() {
        return mIsGalleryShown;
    }

    public LiveData<Boolean> isOtrEncyption() {
        return mXMPPRepository.isOtrEncyption();
    }

    public void startOtr() {
        mXMPPRepository.startOtr();
    }

    public void endOtr() {
        mXMPPRepository.endOtr();
    }
}
