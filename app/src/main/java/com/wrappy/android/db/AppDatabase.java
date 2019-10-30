package com.wrappy.android.db;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import com.wrappy.android.db.dao.AccountInfoDao;
import com.wrappy.android.db.dao.AuthTokenDao;
import com.wrappy.android.db.dao.BlockDao;
import com.wrappy.android.db.dao.ChatBackgroundDao;
import com.wrappy.android.db.dao.ChatDao;
import com.wrappy.android.db.dao.ContactDao;
import com.wrappy.android.db.dao.DeleteAllDao;
import com.wrappy.android.db.dao.DeleteDao;
import com.wrappy.android.db.dao.MessageDao;
import com.wrappy.android.db.entity.AccountInfo;
import com.wrappy.android.db.entity.AuthToken;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.db.entity.Delete;
import com.wrappy.android.db.entity.DeleteAll;
import com.wrappy.android.db.entity.Message;

@Database(entities = {AuthToken.class, Contact.class, Chat.class, ChatBackground.class, Message.class, AccountInfo.class, Delete.class, DeleteAll.class, Block.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AuthTokenDao authDao();
    public abstract BlockDao blockDao();
    public abstract ContactDao contactDao();
    public abstract ChatDao chatDao();
    public abstract ChatBackgroundDao chatBackgroundDao();
    public abstract MessageDao messageDao();
    public abstract AccountInfoDao accountInfoDao();
    public abstract DeleteDao deleteDao();
    public abstract DeleteAllDao deleteAllDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // chat_table
            // remove chat_language column
            // Create the new table
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_table_new (chat_id TEXT NOT NULL, chat_name TEXT NOT NULL, chat_whois TEXT NOT NULL, chat_type TEXT NOT NULL, chat_avatar TEXT, chat_last_message TEXT, chat_last_created INTEGER, chat_unread_count INTEGER NOT NULL, chat_notification INTEGER NOT NULL, PRIMARY KEY(chat_id))");
            // Copy the data
            db.execSQL(
                    "INSERT INTO chat_table_new (chat_id, chat_name, chat_whois, chat_type, chat_avatar, chat_last_message, chat_last_created, chat_unread_count, chat_notification) SELECT chat_id, chat_name, chat_whois, chat_type, chat_avatar, chat_last_message, chat_last_created, chat_unread_count, chat_notification FROM chat_table");
            // Remove the old table
            db.execSQL(
                    "DROP TABLE chat_table");
            // Change the table name to the correct one
            db.execSQL(
                    "ALTER TABLE chat_table_new RENAME TO chat_table");

            // chat_background_table
            // add translate and language columns
            db.execSQL(
                    "ALTER TABLE chat_background_table ADD COLUMN chat_language TEXT");
            db.execSQL(
                    "ALTER TABLE chat_background_table ADD COLUMN chat_auto_translate INTEGER NOT NULL DEFAULT 0");

            // message_table
            // add translate column
            db.execSQL(
                    "ALTER TABLE message_table ADD COLUMN translated_message_text TEXT");
        }
    };
}
