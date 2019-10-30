package com.wrappy.android.server.account.body.request.data;


public class GetTermsAndConditions implements HelperData {
    @Override
    public boolean hasContents() {
        return false;
    }

    @Override
    public DataType getType() {
        return DataType.GET_TERMS_AND_CONDITIONS;
    }
}
