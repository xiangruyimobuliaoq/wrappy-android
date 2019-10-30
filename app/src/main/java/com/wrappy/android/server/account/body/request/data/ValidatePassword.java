package com.wrappy.android.server.account.body.request.data;

public class ValidatePassword implements HelperData {
    /**
     * password for validation
     */
    String password;

    public ValidatePassword(String password) {
        this.password = password;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.VALIDATE_PASSWORD;
    }
}
