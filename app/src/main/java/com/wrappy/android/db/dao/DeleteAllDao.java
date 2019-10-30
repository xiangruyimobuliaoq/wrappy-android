package com.wrappy.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.DeleteAll;

@Dao
public interface DeleteAllDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DeleteAll deleteAll);

    @Query("SELECT * FROM deleteall_table WHERE message_id=:messageId")
    DeleteAll isExist(String messageId);

}
