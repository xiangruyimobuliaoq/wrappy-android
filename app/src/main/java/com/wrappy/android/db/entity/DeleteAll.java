package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "deleteall_table")
public class DeleteAll {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "message_id")
    private String messageId;

    public DeleteAll(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
