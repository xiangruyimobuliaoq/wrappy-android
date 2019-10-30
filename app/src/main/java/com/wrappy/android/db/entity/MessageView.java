package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.support.annotation.NonNull;

import java.util.Date;

public class MessageView {

    @NonNull
    @ColumnInfo(name = "message_id")
    private String MessageId;

    @ColumnInfo(name = "chat_id")
    private String ChatId;

    @ColumnInfo(name = "message_text")
    private String MessageText;

    @ColumnInfo(name = "message_type")
    private int MessageType;

    @ColumnInfo(name = "message_subject")
    private String MessageSubject;

    @ColumnInfo(name = "message_from")
    private String MessageFrom;

    @ColumnInfo(name = "archive_id")
    private String ArchiveId;

    @ColumnInfo(name = "contact_name")
    private String ContactName;

    @ColumnInfo(name = "contact_avatar")
    private String ContactAvatar;

    @ColumnInfo(name = "message_created_at")
    private Date CreatedAt;

    @ColumnInfo(name = "message_status")
    private int MessageStatus;

    @ColumnInfo(name = "translated_message_text")
    private String TranslatedMessageText;

    public String getMessageId() {
        return MessageId;
    }

    public void setMessageId(String messageId) {
        MessageId = messageId;
    }

    public String getChatId() {
        return ChatId;
    }

    public void setChatId(String chatId) {
        ChatId = chatId;
    }

    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    public String getMessageSubject() {
        return MessageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        MessageSubject = messageSubject;
    }

    public String getMessageFrom() {
        return MessageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        MessageFrom = messageFrom;
    }

    public String getContactName() {
        return ContactName;
    }

    public void setContactName(String contactName) {
        ContactName = contactName;
    }

    public String getContactAvatar() {
        return ContactAvatar;
    }

    public void setContactAvatar(String contactAvatar) {
        ContactAvatar = contactAvatar;
    }

    public Date getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        CreatedAt = createdAt;
    }

    public String getArchiveId() {
        return ArchiveId;
    }

    public void setArchiveId(String archiveId) {
        ArchiveId = archiveId;
    }

    public int getMessageStatus() {
        return MessageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        MessageStatus = messageStatus;
    }

    public int getMessageType() {
        return MessageType;
    }

    public void setMessageType(int messageType) {
        MessageType = messageType;
    }

    public String getTranslatedMessageText() {
        return TranslatedMessageText;
    }

    public void setTranslatedMessageText(String translatedMessageText) {
        TranslatedMessageText = translatedMessageText;
    }
}
