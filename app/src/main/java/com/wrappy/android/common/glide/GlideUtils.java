package com.wrappy.android.common.glide;


import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.server.AuthRepository;

public class GlideUtils {
    private static final int REFRESH_INTERVAL = 1000 * 60 * 60 * 3; // 3 hours

    /**
     * Temporarily tracks images loaded using Glide by storing the ObjectKey signature offset.
     * This is a workaround to invalidate images in the UI.
     * <p>
     * This is cleared when app is swiped from recents or stopped by the system.
     */
    private static SimpleArrayMap<String, Integer> sImageVersionMap = new SimpleArrayMap<>();
    private static String sKeyPrefix;

    public static void resetVersionCache() {
        sImageVersionMap.clear();
        sKeyPrefix = null;
    }

    public static ObjectKey createObjectKey(String url) {
        return new ObjectKey(
                getKeyPrefix() + (getImageUrlVersion(url) + (System.currentTimeMillis() / REFRESH_INTERVAL)));
    }

    public static void updateKeyForUrl(String url) {
        sImageVersionMap.put(url, getImageUrlVersion(url) + 1);
    }

    public static void clearGlideMemory() {
        Glide.get(WrappyApp.getInstance()).clearMemory();
    }

    @WorkerThread
    public static void clearGlideDiskCache() {
        Glide.get(WrappyApp.getInstance()).clearDiskCache();
    }

    private static int getImageUrlVersion(String url) {
        Integer version = sImageVersionMap.get(url);
        return version != null ? version : 0;
    }

    private static String getKeyPrefix() {
        if (TextUtils.isEmpty(sKeyPrefix)) {
            String username = WrappyApp.getInstance()
                    .getSharedPreferences()
                    .getString(AuthRepository.PREF_KEY_USER, "");
            sKeyPrefix = username.length() > 16 ?
                    username.substring(0, 16) :
                    username;
        }
        return sKeyPrefix;
    }
}
