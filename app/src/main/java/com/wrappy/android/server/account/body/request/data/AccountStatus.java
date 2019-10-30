package com.wrappy.android.server.account.body.request.data;


public class AccountStatus implements HelperData {

    String username;

    public AccountStatus(String username) {
        this.username = username;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.ACCOUNT_STATUS;
    }
}
