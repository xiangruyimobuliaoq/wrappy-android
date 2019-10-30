package com.wrappy.android.common.utils;


import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * https://stackoverflow.com/questions/7531989/android-setimageuri-not-working-with-asset-uri
 */
public class AssetContentProvider extends ContentProvider {
    private AssetManager mAssetManager;
    public static final Uri CONTENT_URI =
            Uri.parse("content://com.wrappy.android.provider.assets");

    @Override
    public boolean onCreate() {
        mAssetManager = getContext().getAssets();
        return true;
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        String path = uri.getPath().substring(1);
        try {
            return mAssetManager.openFd(path);
        } catch (IOException e) {
            throw new FileNotFoundException("No asset found: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
