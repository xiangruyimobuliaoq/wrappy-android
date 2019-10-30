package com.wrappy.android.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Base64;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class Base64ImageFileAdapter extends TypeAdapter<Base64ImageFile> {
    private static final String ASSET_PREFIX = ContentResolver.SCHEME_FILE + ":///android_asset/";
    private Application mApplication;

    public Base64ImageFileAdapter(Application application) {
        mApplication = application;
    }

    @Override
    public void write(JsonWriter out, Base64ImageFile value) throws IOException {
        try {
            if (Uri.EMPTY.equals(value.getUri())) {
                out.value("");
                return;
            }

            InputStream is;
            if (value.getUri().toString().startsWith(ASSET_PREFIX)) {
                is = mApplication.getAssets().open(value.getUri().toString().substring(ASSET_PREFIX.length()));
            } else {
                is = mApplication.getContentResolver().openInputStream(value.getUri());
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] ba = new byte[1024];
            while ((nRead = is.read(ba, 0, ba.length)) != -1) {
                buffer.write(ba, 0, nRead);
            }
            out.value(Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP));
        } catch (Exception e) {
            out.nullValue();
            e.printStackTrace();
        }
    }

    @Override
    public Base64ImageFile read(JsonReader in) throws IOException {
        return new Base64ImageFile(in.nextString());
    }
}
