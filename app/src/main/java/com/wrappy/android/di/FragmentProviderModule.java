package com.wrappy.android.di;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.wrappy.android.about.AboutNavigationManager;
import com.wrappy.android.chat.ChatNavigationManager;
import com.wrappy.android.contact.ContactNavigationManager;
import com.wrappy.android.forgot.ForgotPasswordNavigationManager;
import com.wrappy.android.home.profile.EditProfileNavigationManager;
import com.wrappy.android.login.LoginNavigationManager;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerNavigationManager;
import com.wrappy.android.misc.reset.ResetPasswordNavigationManager;
import com.wrappy.android.register.RegisterNavigationManager;
import com.wrappy.android.security.SecurityNavigationManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentProviderModule {
    private Fragment mFragment;

    public FragmentProviderModule(Fragment fragment) {
        mFragment = fragment;
    }

    @Provides
    @Named("fragment")
    FragmentManager providesFragmentManager() {
        return mFragment.getChildFragmentManager();
    }

    @Provides
    @FragmentScope
    LoginNavigationManager providesLoginNavigationManager(@Named("fragment")FragmentManager fragmentManager) {
        return new LoginNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    RegisterNavigationManager providesRegisterNavigationManager(@Named("fragment")FragmentManager fragmentManager) {
        return new RegisterNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    ForgotPasswordNavigationManager providesForgotPasswordNavigationManager(@Named("fragment")FragmentManager fragmentManager) {
        return new ForgotPasswordNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    AboutNavigationManager providesAboutNavigationManager(@Named("fragment")FragmentManager fragmentManager){
        return new AboutNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    SecurityNavigationManager providesSecurityNavigationManager(@Named("fragment")FragmentManager fragmentManager){
        return new SecurityNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    ChatNavigationManager providesChatNavigationManager(@Named("fragment") FragmentManager fragmentManager) {
        return new ChatNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    ContactNavigationManager providesContactNavigationManager(@Named("fragment") FragmentManager fragmentManager) {
        return new ContactNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    EditProfileNavigationManager providesEditProfileNavigationManager(@Named("fragment")FragmentManager fragmentManager) {
        return new EditProfileNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    ForgotAnswerNavigationManager providesForgotAnswerNavigationManager(@Named("fragment") FragmentManager fragmentManager) {
        return new ForgotAnswerNavigationManager(fragmentManager);
    }

    @Provides
    @FragmentScope
    ResetPasswordNavigationManager providesResetPasswordNavigationManager(@Named("fragment") FragmentManager fragmentManager) {
        return new ResetPasswordNavigationManager(fragmentManager);
    }
}
