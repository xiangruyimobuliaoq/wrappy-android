package com.wrappy.android.di;

import com.wrappy.android.MainActivity;
import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {ActivityProviderModule.class})
public interface ActivityInjector {
    void inject(MainActivity activity);

    FragmentInjector plusFragmentInjector(FragmentProviderModule fragmentProviderModule);
}
