package com.wrappy.android.server.account.body.request.data;


public class SendSmsCode implements HelperData {

    /**
     * SMS validation code type, currently only support "registration"
     */
    String countryCode;
    String phone;
    String type = "registration";

    public SendSmsCode(String countryCode, String phone) {
        this.countryCode = countryCode;
        this.phone = phone;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.SEND_SMS_CODE;
    }
}
