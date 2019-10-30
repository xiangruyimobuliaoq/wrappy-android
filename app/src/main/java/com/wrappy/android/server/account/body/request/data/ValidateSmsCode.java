package com.wrappy.android.server.account.body.request.data;

public class ValidateSmsCode extends SendSmsCode {
    /**
     * code to be validated
     */
    String code;

    public ValidateSmsCode(String countryCode, String phone, String type, String code) {
        super(countryCode, phone);
        super.type = type;
        this.code = code;
    }

    @Override
    public DataType getType() {
        return DataType.VALIDATE_SMS_CODE;
    }
}
