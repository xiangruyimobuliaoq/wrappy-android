package com.wrappy.android.common;


import java.util.Map;
import javax.inject.Provider;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;


/**
 * Workaround to make AAC View Models injectable by Dagger
 * Taken from
 * googlesamples/android-architecture-components/GithubBrowserSample
 */
public class WrappyViewModelFactory implements ViewModelProvider.Factory {
    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> mProviders;

    public WrappyViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providers) {
        mProviders = providers;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        Provider<? extends ViewModel> provider = mProviders.get(modelClass);
        if (provider == null) {
            for (Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry : mProviders.entrySet()) {
                if (modelClass.isAssignableFrom(entry.getKey())) {
                    provider = entry.getValue();
                    break;
                }
            }
        }
        if (provider == null) {
            throw new IllegalArgumentException("unknown model class " + modelClass);
        }
        try {
            return (T) provider.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
