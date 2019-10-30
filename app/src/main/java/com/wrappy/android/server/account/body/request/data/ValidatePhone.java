package com.wrappy.android.server.account.body.request.data;


public class ValidatePhone implements HelperData {
    /**
     * mobile phone country code
     */
    public String countryCode;

    /**
     * phone number
     */
    public String phone;

    /**
     * Whether check the phone have been used
     */
    public boolean sendValidationCode;

    /**
     * Whether send SMS validation code after validation
     */
    public boolean checkRegistered;

    /**
     * SMS validation code type, currently only supports "registration"
     */
    public String codeType;

    public ValidatePhone(String countryCode, String phone) {
        this.countryCode = countryCode;
        this.phone = phone;
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public DataType getType() {
        return DataType.VALIDATE_PHONE;
    }
}
