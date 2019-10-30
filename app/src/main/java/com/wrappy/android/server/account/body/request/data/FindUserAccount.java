package com.wrappy.android.server.account.body.request.data;

public class FindUserAccount implements HelperData {
    /**
     * account's email address
     */
    public String email;

    /**
     * account's phone number
     */
    public String phone;

    /**
     * Whether send account to email address
     */
    public boolean sendEmail;

    /**
     * Whether send account to mobile
     */
    public boolean sendSms;

    public FindUserAccount(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.FIND_USER_ACCOUNT;
    }
}
