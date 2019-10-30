package com.wrappy.android.server.account.body.request;


public class UpdateUserBody {
    String patternPassword;
    AccountBody account;

    public UpdateUserBody(String patternPassword, String nickName) {
        this.patternPassword = patternPassword;
        this.account = new AccountBody();
        account.nickName = nickName;
    }
}
