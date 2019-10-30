package com.wrappy.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.Delete;

@Dao
public interface DeleteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Delete delete);

    @Query("SELECT * FROM delete_table WHERE message_id=:messageId")
    Delete isExist(String messageId);

}
