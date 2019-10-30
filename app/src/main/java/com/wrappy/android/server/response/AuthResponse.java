package com.wrappy.android.server.response;

import com.google.gson.annotations.SerializedName;
import com.wrappy.android.server.account.body.request.AccountBody;

public class AuthResponse {

    @SerializedName("access_token")
    private String mAccessToken;

    @SerializedName("refresh_token")
    private String mRefreshToken;

    @SerializedName("account")
    private AccountBody mAccount;

    public String getAccessToken() {
        return mAccessToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public AccountBody getAccount() {
        return mAccount;
    }
}
