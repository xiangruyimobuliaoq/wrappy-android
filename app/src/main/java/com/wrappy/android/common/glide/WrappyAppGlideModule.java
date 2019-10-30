package com.wrappy.android.common.glide;

import java.io.InputStream;
import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.google.gson.Gson;
import com.wrappy.android.WrappyApp;
import okhttp3.OkHttpClient;

@GlideModule
public class WrappyAppGlideModule extends AppGlideModule {
    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Gson gson;

    public WrappyAppGlideModule() {
        WrappyApp.getInstance().getInjector().inject(this);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
        registry.prepend(GlideUrl.class, InputStream.class, new WrappyUserFileGlideLoader.Factory(gson));
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setLogLevel(Log.ERROR);
        super.applyOptions(context, builder);
    }
}
