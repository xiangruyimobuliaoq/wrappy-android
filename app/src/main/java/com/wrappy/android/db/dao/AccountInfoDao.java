package com.wrappy.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.wrappy.android.db.entity.AccountInfo;

@Dao
public interface AccountInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addAccountInfo(AccountInfo accountBody);

    @Query("SELECT * FROM account_info_table WHERE id=:id")
    LiveData<AccountInfo> getAccountInfo(String id);

    @Query("DELETE FROM account_info_table")
    void deleteAll();
}
