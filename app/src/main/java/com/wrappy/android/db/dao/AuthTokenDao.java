package com.wrappy.android.db.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.wrappy.android.db.entity.AuthToken;

@Dao
public interface AuthTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToken(AuthToken token);

    @Query("SELECT * FROM auth_token_table WHERE type=:type")
    AuthToken getToken(String type);

    @Query("DELETE FROM auth_token_table")
    void deleteAll();
}
