package com.wrappy.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.db.entity.MessageView;

import java.util.Date;
import java.util.List;

@Dao
public abstract class MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Message message);

    @Query("SELECT * FROM message_table")
    public abstract List<Message> getAllMessage();

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id ORDER BY message_created_at DESC LIMIT 30")
    public abstract List<MessageView> getInitialMessages(String chat_id);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE message_id = :message_id")
    public abstract LiveData<MessageView> getSingleMessage(String message_id);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id ORDER BY message_created_at DESC LIMIT 1")
    public abstract LiveData<MessageView> getMessages(String chat_id);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id AND message_subject='image' ORDER BY message_created_at DESC")
    public abstract DataSource.Factory<Integer, MessageView> getImageMessages(String chat_id);

    @Query("SELECT COUNT(*)-1 FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id AND message_subject='image' AND message_created_at>=:message_created_at ORDER BY message_created_at DESC")
    public abstract int getImageMessagePosition(String chat_id, Date message_created_at);

    @Query("SELECT message_id FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id ORDER BY message_created_at DESC LIMIT 1")
    public abstract String getLastMessageId(String chat_id);

    @Query("SELECT COUNT(message_id) FROM message_table WHERE chat_id=:chat_id")
    public abstract int getMessagesCount(String chat_id);

    @Query("SELECT * FROM message_table WHERE chat_id=:chat_id ORDER BY message_created_at DESC LIMIT 1")
    public abstract Message getLatestMessage(String chat_id);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id ORDER BY message_created_at DESC LIMIT 30 OFFSET :offset")
    public abstract List<MessageView> pageMessage(String chat_id, int offset);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id AND message_text=:search ORDER BY message_created_at DESC LIMIT 30 OFFSET :offset")
    public abstract List<MessageView> pageSearchMessage(String chat_id, String search, int offset);

    @Query("SELECT * FROM message_table LEFT JOIN contact_table ON message_table.message_from = contact_table.contact_id WHERE chat_id=:chat_id AND message_text LIKE :search AND message_subject IS NULL ORDER BY message_created_at DESC")
    public abstract List<MessageView> searchMessage(String chat_id, String search);

    @Query("UPDATE message_table SET archive_id=:archiveId WHERE message_id=:message_id")
    public abstract void setArchiveId(String archiveId, String message_id);

    @Query("DELETE FROM message_table WHERE chat_id=:chatId")
    public abstract void deleteMessages(String chatId);

    @Query("DELETE FROM message_table WHERE message_id=:message_id")
    public abstract void deleteMessage(String message_id);

    @Query("DELETE FROM message_table")
    public abstract void deleteAll();

    @Query("UPDATE message_table SET translated_message_text=:translated_text WHERE message_id=:message_id")
    public abstract void addMessageTranslation(String message_id, String translated_text);

    @Query("UPDATE message_table SET translated_message_text=NULL WHERE message_id=:message_id")
    public abstract void removeMessageTranslation(String message_id);

    @Transaction
    public void update(Message message) {
        update(message.getMessageId(),
                message.getChatId(),
                message.getMessageType(),
                message.getMessageText(),
                message.getMessageSubject(),
                message.getMessageFrom(),
                message.getArchiveId(),
                message.getCreatedAt(),
                message.getMessageStatus(),
                message.getTranslatedMessageText());
    }

    @Query("UPDATE OR IGNORE message_table SET message_id = :message_id,chat_id = :chat_id,message_type = :message_type,message_text = :message_text,message_subject = :message_subject,message_from = :message_from,archive_id = coalesce(:archive_id, archive_id),message_created_at = :message_created_at,message_status = :message_status,translated_message_text = coalesce(:translated_message_text, translated_message_text) WHERE message_id = :message_id")
    abstract void update(String message_id,
                         String chat_id,
                         int message_type,
                         String message_text,
                         String message_subject,
                         String message_from,
                         String archive_id,
                         Date message_created_at,
                         int message_status,
                         String translated_message_text);

    @Transaction
    public void upsert(Message message) {
        long id = insert(message);
        if (id == -1) {
            update(message);
        }
    }
}
