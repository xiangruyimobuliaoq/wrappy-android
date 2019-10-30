package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "block_table")
public class Block {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "block_id")
    private String blockId;

    @NonNull
    @ColumnInfo(name = "block_name")
    private String blockName;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    @NonNull
    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(@NonNull String blockName) {
        this.blockName = blockName;
    }
}
