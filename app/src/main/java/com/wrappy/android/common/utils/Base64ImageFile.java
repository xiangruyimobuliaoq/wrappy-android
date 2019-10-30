package com.wrappy.android.common.utils;


import android.net.Uri;

public class Base64ImageFile {

    private Uri mUri;
    private String mValue = "";

    public Base64ImageFile(Uri uri) {
        mUri = uri;
    }

    public Base64ImageFile(String value) {
        mValue = value;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getStringValue() {
        return mValue;
    }
}
