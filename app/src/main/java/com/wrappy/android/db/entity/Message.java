package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

@Entity(tableName = "message_table")
public class Message {

    public static final int MESSAGE_TYPE_GROUP = 1;
    public static final int MESSAGE_TYPE_CHAT = 0;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "message_id")
    private String MessageId;

    @NonNull
    @ColumnInfo(name = "chat_id")
    private String ChatId;

    @NonNull
    @ColumnInfo(name = "message_type")
    private int MessageType;

    @NonNull
    @ColumnInfo(name = "message_text")
    private String MessageText;

    @ColumnInfo(name = "message_subject")
    private String MessageSubject;

    @NonNull
    @ColumnInfo(name = "message_from")
    private String MessageFrom;

    @ColumnInfo(name = "archive_id")
    private String ArchiveId;

    @NonNull
    @ColumnInfo(name = "message_created_at")
    private Date CreatedAt;

    @NonNull
    @ColumnInfo(name = "message_status")
    private int MessageStatus;

    @ColumnInfo(name = "translated_message_text")
    private String TranslatedMessageText;

    public Message() {

    }

    public Message(String messageId,
                   String chatId,
                   int messageType,
                   String messageText,
                   String messageSubject,
                   String messageFrom,
                   Date createdAt,
                   int messageStatus) {

        MessageId = messageId;
        ChatId = chatId;
        MessageType = messageType;
        MessageText = messageText;
        MessageSubject = messageSubject;
        MessageFrom = messageFrom;
        CreatedAt = createdAt;
        MessageStatus = messageStatus;

    }

    public String getMessageId() {
        return MessageId;
    }

    public void setMessageId(String messageId) {
        MessageId = messageId;
    }

    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    public String getMessageFrom() {
        return MessageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        MessageFrom = messageFrom;
    }

    public Date getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        CreatedAt = createdAt;
    }

    public String getMessageSubject() {
        return MessageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        MessageSubject = messageSubject;
    }

    @NonNull
    public String getChatId() {
        return ChatId;
    }

    public void setChatId(@NonNull String chatId) {
        ChatId = chatId;
    }

    public String getArchiveId() {
        return ArchiveId;
    }

    public void setArchiveId(String archiveId) {
        ArchiveId = archiveId;
    }

    @NonNull
    public int getMessageStatus() {
        return MessageStatus;
    }

    public void setMessageStatus(@NonNull int messageStatus) {
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
