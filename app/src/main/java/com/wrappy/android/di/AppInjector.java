package com.wrappy.android.di;

import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.glide.WrappyAppGlideModule;
import com.wrappy.android.db.Converters;
import dagger.Component;

@AppScope
@Component(modules = {AppProviderModule.class})
public interface AppInjector {
    void inject(WrappyApp wrappyApp);

    void inject(WrappyAppGlideModule wrappyAppGlideModule);

    void inject(Converters converters);

    ActivityInjector plusActivityInjector(ActivityProviderModule activityProviderModule);

    ServiceInjector plusServiceInjector(ServiceProviderModule serviceProviderModule);
}
