package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.server.response.AuthResponse;

@Entity(tableName = "auth_token_table")
public class AuthToken {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "type")
    private String mAccountType = ServerConstants.ACC_TYPE_WRAPPY;

    @ColumnInfo(name = "access_token")
    private String mAccessToken;

    @ColumnInfo(name = "refresh_token")
    private String mRefreshToken;

    @ColumnInfo(name = "account_id")
    private String mAccountId;

    public AuthToken(AuthResponse response) {
        mAccountType = response.getAccount().extendedInfo.accountType;
        mAccountId = response.getAccount().id;
        mAccessToken = response.getAccessToken();
        mRefreshToken = response.getRefreshToken();
    }

    public AuthToken(String accountType, String accessToken, String refreshToken, String accountId) {
        mAccountType = accountType;
        mAccessToken = accessToken;
        mRefreshToken = refreshToken;
        mAccountId = accountId;
    }

    public String getAccountType() {
        return mAccountType;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getAccountId() {
        return mAccountId;
    }
}
