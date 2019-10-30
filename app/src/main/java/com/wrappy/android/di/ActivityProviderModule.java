package com.wrappy.android.di;

import android.support.v4.app.FragmentManager;
import com.wrappy.android.MainActivity;
import com.wrappy.android.NavigationManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityProviderModule {
    private MainActivity mMainActivity;

    public ActivityProviderModule(MainActivity activity) {
        mMainActivity = activity;
    }

    @Provides
    @Named("activity")
    FragmentManager provideSupportFragmentManager() {
        return mMainActivity.getSupportFragmentManager();
    }

    @Provides
    @ActivityScope
    NavigationManager provideNavigationManager(@Named("activity") FragmentManager fragmentManager) {
        return new NavigationManager(fragmentManager);
    }
}
