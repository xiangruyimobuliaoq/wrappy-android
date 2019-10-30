package com.wrappy.android.server.account.body.request.data;


public class ValidateUsername implements HelperData {
    private String username;

    private String password;

    public ValidateUsername(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.VALIDATE_USERNAME;
    }
}
