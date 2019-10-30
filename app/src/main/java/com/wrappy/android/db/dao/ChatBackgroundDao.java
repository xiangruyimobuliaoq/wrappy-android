package com.wrappy.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.ChatBackground;
import com.wrappy.android.db.entity.TranslateSetting;

@Dao
public interface ChatBackgroundDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ChatBackground chatBackground);

    @Query("UPDATE chat_background_table SET chat_background=:chatBackground WHERE chat_id=:chatId AND chat_whoIs=:chatWhoIs")
    void setChatBackground(String chatBackground, String chatId, String chatWhoIs);

    @Query("SELECT * FROM chat_background_table WHERE chat_Id=:chatId AND chat_whoIs=:chatWhoIs")
    public ChatBackground getChatBackground(String chatId, String chatWhoIs);

    @Query("UPDATE chat_background_table SET chat_auto_translate = :chat_auto_translate WHERE chat_id = :chat_id")
    void setAutoTranslate(String chat_id, boolean chat_auto_translate);

    @Query("SELECT chat_auto_translate AS isAutoTranslate, chat_language AS language FROM chat_background_table WHERE chat_id  = :chat_id")
    TranslateSetting getTranslateSetting(String chat_id);

    @Query("UPDATE chat_background_table SET chat_language = :chat_language WHERE chat_id = :chat_id")
    void setChatLanguage(String chat_id, String chat_language);

}
