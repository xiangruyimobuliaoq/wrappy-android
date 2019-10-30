package com.wrappy.android.di;

import com.wrappy.android.firebase.WrappyFirebaseMessagingService;
import dagger.Subcomponent;


@ServiceScope
@Subcomponent(modules = {ServiceProviderModule.class})
public interface ServiceInjector {
    void inject(WrappyFirebaseMessagingService wrappyFirebaseMessagingService);
}
