package com.wrappy.android;

import javax.inject.Inject;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;

import android.content.SharedPreferences;
import com.wrappy.android.common.AppLifecycleListener;
import com.wrappy.android.common.utils.KeyStoreUtils;
import com.wrappy.android.di.AppInjector;
import com.wrappy.android.di.AppProviderModule;
import com.wrappy.android.di.DaggerAppInjector;

public class WrappyApp extends Application {

    @Inject
    AppLifecycleListener mAppLifecycleListener;

    @Inject
    SharedPreferences mSharedPreferences;

    private AppInjector mAppInjector;

    private static WrappyApp mAppInstance;

    public WrappyApp() {
        mAppInstance = this;
    }

    public static WrappyApp getInstance() {
        return mAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getInjector().inject(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mAppLifecycleListener);

        KeyStoreUtils.initialize(this);
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public AppInjector getInjector() {
        if (mAppInjector == null) {
            mAppInjector = DaggerAppInjector.builder()
                    .appProviderModule(new AppProviderModule(this))
                    .build();
        }
        return mAppInjector;
    }



}
