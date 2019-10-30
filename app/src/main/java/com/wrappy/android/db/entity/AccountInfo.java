package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.wrappy.android.server.account.body.request.AccountBody;

@Entity(tableName = "account_info_table")
public class AccountInfo {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String mId;

    @Embedded(prefix = "acc_")
    private AccountBody mAccountBody;

    @Ignore
    public AccountInfo(AccountBody accountBody) {
        mAccountBody = accountBody;
        mId = mAccountBody.id;
    }

    public AccountInfo(String id, AccountBody accountBody) {
        mId = id;
        mAccountBody = accountBody;
    }

    public String getId() {
        return mId;
    }

    public AccountBody getAccountBody() {
        return mAccountBody;
    }
}
