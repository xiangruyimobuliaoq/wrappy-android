package com.wrappy.android.server.account.body.request.data;

public class ValidateEmail implements HelperData {
    /**
     * email address for validation
     */
    String emailAddress;

    public ValidateEmail(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.VALIDATE_EMAIL;
    }
}
