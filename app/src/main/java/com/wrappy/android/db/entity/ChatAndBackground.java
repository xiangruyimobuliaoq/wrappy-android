package com.wrappy.android.db.entity;

import java.util.Date;

public class ChatAndBackground {

    public String chatId;
    public String chatName;
    public String chatWhoIs;
    public String chatType;
    public String chatAvatar;
    public String chatLastMessage;
    public Date chatLastDate;
    public int chatUnreadCount;
    public boolean chatNotification;
    public String chatLanguage;
    public boolean chatAutoTranslate;
    public String chatBackground;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatWhoIs() {
        return chatWhoIs;
    }

    public void setChatWhoIs(String chatWhoIs) {
        this.chatWhoIs = chatWhoIs;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getChatAvatar() {
        return chatAvatar;
    }

    public void setChatAvatar(String chatAvatar) {
        this.chatAvatar = chatAvatar;
    }

    public String getChatLastMessage() {
        return chatLastMessage;
    }

    public void setChatLastMessage(String chatLastMessage) {
        this.chatLastMessage = chatLastMessage;
    }

    public Date getChatLastDate() {
        return chatLastDate;
    }

    public void setChatLastDate(Date chatLastDate) {
        this.chatLastDate = chatLastDate;
    }

    public int getChatUnreadCount() {
        return chatUnreadCount;
    }

    public void setChatUnreadCount(int chatUnreadCount) {
        this.chatUnreadCount = chatUnreadCount;
    }

    public boolean isChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(boolean chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getChatLanguage() {
        return chatLanguage;
    }

    public void setChatLanguage(String chatLanguage) {
        this.chatLanguage = chatLanguage;
    }

    public String getChatBackground() {
        return chatBackground;
    }

    public void setChatBackground(String chatBackground) {
        this.chatBackground = chatBackground;
    }

    public boolean isChatAutoTranslate() {
        return chatAutoTranslate;
    }

    public void setChatAutoTranslate(boolean chatAutoTranslate) {
        this.chatAutoTranslate = chatAutoTranslate;
    }
}
