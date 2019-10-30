package com.wrappy.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.TranslateSetting;

import java.util.Date;
import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Chat chat);

    @Query("SELECT chats.chat_id AS chatId, chats.chat_name as chatName, chats.chat_whois AS chatWhoIs, chats.chat_type AS chatType, chats.chat_avatar AS chatAvatar, chats.chat_last_message AS chatLastMessage, chats.chat_last_created AS chatLastDate, chats.chat_unread_count AS chatUnreadCount, chats.chat_notification AS chatNotification, backgrounds.chat_language AS chatLanguage, backgrounds.chat_auto_translate AS chatAutoTranslate, backgrounds.chat_background AS chatBackground " +
            "FROM chat_table chats, chat_background_table backgrounds " +
            "WHERE (chats.chat_id = backgrounds.chat_Id AND chats.chat_whois = backgrounds.chat_whoIs) AND chats.chat_id=:chat_id")
    ChatAndBackground getChat(String chat_id);

    @Query("UPDATE chat_table SET chat_name=:chat_name WHERE chat_id=:chat_id")
    void changeChatName(String chat_name, String chat_id);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND (chat_type=='groupchat' OR (SELECT contact_isBlocked FROM chat_table JOIN contact_table ON chat_table.chat_id = contact_table.contact_id WHERE chat_id=chats.chat_id)=0) AND (chat_name LIKE :query) ORDER BY chat_name ASC")
    LiveData<List<Chat>> getQueryChats(String query, String whoIs);

    @Query("UPDATE chat_table SET chat_notification=:isNotifOn WHERE chat_id=:chat_id")
    void toggleNotif(boolean isNotifOn, String chat_id);

    @Query("UPDATE chat_table SET chat_last_message=:chat_last_message, chat_last_created=:chat_last_created WHERE chat_id=:chat_id AND (chat_last_message IS NULL OR chat_last_message=' ' OR chat_last_created<:chat_last_created )")
    void changeChatLastMessage(String chat_last_message, Date chat_last_created , String chat_id);

    @Query("UPDATE chat_table SET chat_last_message=:chat_last_message, chat_last_created=:chat_last_created WHERE chat_id=:chat_id")
    void overrideChatLastMessage(String chat_last_message, Date chat_last_created , String chat_id);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND (chat_type=='groupchat' OR (SELECT contact_isBlocked FROM chat_table JOIN contact_table ON chat_table.chat_id = contact_table.contact_id WHERE chat_id=chats.chat_id)==0) ORDER BY chat_last_created DESC")
    LiveData<List<Chat>> getChatList(String whoIs);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND (SELECT contact_isBlocked FROM chat_table JOIN contact_table ON chat_table.chat_id = contact_table.contact_id WHERE chat_id=chats.chat_id)==0 ORDER BY chat_last_created DESC")
    LiveData<List<Chat>> getOnetoOneChatList(String whoIs);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND (SELECT contact_isBlocked FROM chat_table JOIN contact_table ON chat_table.chat_id = contact_table.contact_id WHERE chat_id=chats.chat_id)==0 AND chat_name LIKE :query ORDER BY chat_last_created DESC")
    LiveData<List<Chat>> getQueryOnetoOneChatList(String query, String whoIs);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND chat_type=='groupchat' ORDER BY chat_last_created DESC")
    LiveData<List<Chat>> getGroupChatList(String whoIs);

    @Query("SELECT * FROM chat_table AS chats WHERE chat_last_message IS NOT NULL AND chat_whois=:whoIs AND chat_type=='groupchat' AND chat_name LIKE :query ORDER BY chat_last_created DESC")
    LiveData<List<Chat>> getQueryGroupChatList(String query, String whoIs);

    @Query("SELECT * FROM chat_table WHERE chat_id=:chat_id")
    LiveData<Chat> getChatUpdate(String chat_id);

    @Query("SELECT * FROM chat_table WHERE chat_id=:chat_id AND chat_last_message IS NOT NULL")
    Chat hasMessage(String chat_id);

    @Query("UPDATE chat_table SET chat_unread_count=:chat_unread_count WHERE chat_id=:chat_id")
    void updateChatUnreadCount(String chat_id, int chat_unread_count);

    @Query("UPDATE chat_table SET chat_unread_count=chat_unread_count+1 WHERE chat_id=:chat_id")
    void increaseChatUnreadCount(String chat_id);

    @Query("DELETE FROM chat_table WHERE chat_id=:chatId")
    void deleteChat(String chatId);

    @Query("DELETE FROM chat_table")
    void deleteAll();

}
