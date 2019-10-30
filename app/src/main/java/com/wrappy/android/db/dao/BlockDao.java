package com.wrappy.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.Block;

import java.util.List;

@Dao
public interface BlockDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Block block);

    @Query("SELECT * FROM block_table")
    LiveData<List<Block>> getBlockList();

    @Query("SELECT * FROM block_table WHERE block_id LIKE :query OR block_name LIKE :query")
    LiveData<List<Block>> searchBlock(String query);

    @Delete
    void delete(Block block);

}
