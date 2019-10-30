package com.wrappy.android.server.account.body.request.data;


public class GetShareContent implements HelperData {
    @Override
    public boolean hasContents() {
        return false;
    }

    @Override
    public DataType getType() {
        return DataType.GET_SHARE_WRAPPY_CONTENT;
    }
}
