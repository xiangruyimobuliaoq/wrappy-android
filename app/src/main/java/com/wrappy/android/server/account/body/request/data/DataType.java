package com.wrappy.android.server.account.body.request.data;


public enum DataType {
    GET_TERMS_AND_CONDITIONS("terms_and_condition"),
    VALIDATE_PHONE("validate_phone"),
    SEND_SMS_CODE("send_sms_code"),
    VALIDATE_SMS_CODE("validate_sms_code"),
    VALIDATE_EMAIL("validate_email"),
    VALIDATE_PASSWORD("validate_password"),
    VALIDATE_USERNAME("validate_username"),
    GET_SECURITY_QUESTIONS("security_questions"),
    FIND_USER_ACCOUNT("find_user_account"),
    ACCOUNT_STATUS("account_status"),
    GET_SHARE_WRAPPY_CONTENT("share_wrappy_content"),
    GET_VERSION("version");

    private String type;

    DataType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
