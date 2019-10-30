package com.wrappy.android.server.account.body.request.data;

public class GetSecurityQuestions implements HelperData {
    @Override
    public boolean hasContents() {
        return false;
    }

    @Override
    public DataType getType() {
        return DataType.GET_SECURITY_QUESTIONS;
    }
}
