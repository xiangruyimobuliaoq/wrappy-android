package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Entity(tableName = "chat_background_table", primaryKeys = {"chat_id", "chat_whoIs"})
public class ChatBackground {

    @NonNull
    @ColumnInfo(name = "chat_id")
    private String chatId;

    @NonNull
    @ColumnInfo(name = "chat_whoIs")
    private String chatWhoIs;

    @ColumnInfo(name = "chat_background")
    private String chatBackground;

    @ColumnInfo(name = "chat_language")
    private String chatLanguage;

    @ColumnInfo(name = "chat_auto_translate")
    private boolean chatAutoTranslate;

    public ChatBackground(@NonNull String chatId, @NonNull String chatWhoIs, String chatBackground) {
        this.chatId = chatId;
        this.chatWhoIs = chatWhoIs;
        this.chatBackground = chatBackground;
    }

    @NonNull
    public String getChatId() {
        return chatId;
    }

    public void setChatId(@NonNull String chatId) {
        this.chatId = chatId;
    }

    @NonNull
    public String getChatWhoIs() {
        return chatWhoIs;
    }

    public void setChatWhoIs(@NonNull String chatWhoIs) {
        this.chatWhoIs = chatWhoIs;
    }

    public String getChatBackground() {
        return chatBackground;
    }

    public void setChatBackground(String chatBackground) {
        this.chatBackground = chatBackground;
    }

    public String getChatLanguage() {
        return chatLanguage;
    }

    public void setChatLanguage(String chatLanguage) {
        this.chatLanguage = chatLanguage;
    }

    public boolean isChatAutoTranslate() {
        return chatAutoTranslate;
    }

    public void setChatAutoTranslate(boolean chatAutoTranslate) {
        this.chatAutoTranslate = chatAutoTranslate;
    }
}
