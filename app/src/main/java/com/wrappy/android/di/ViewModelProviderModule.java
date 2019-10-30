package com.wrappy.android.di;


import android.accounts.Account;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.wrappy.android.MainActivityViewModel;
import com.wrappy.android.about.AboutViewModel;
import com.wrappy.android.chat.ChatViewModel;
import com.wrappy.android.common.WrappyViewModelFactory;
import com.wrappy.android.contact.ContactViewModel;
import com.wrappy.android.forgot.ForgotPasswordViewModel;
import com.wrappy.android.home.HomeViewModel;
import com.wrappy.android.home.profile.EditProfileViewModel;
import com.wrappy.android.login.LoginViewModel;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerViewModel;
import com.wrappy.android.misc.reset.ResetPasswordViewModel;
import com.wrappy.android.register.RegisterViewModel;
import com.wrappy.android.security.SecurityViewModel;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.welcome.WelcomeViewModel;
import com.wrappy.android.xmpp.XMPPRepository;

import java.util.Map;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class ViewModelProviderModule {

    @Provides
    @AppScope
    public ViewModelProvider.Factory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
        return new WrappyViewModelFactory(creators);
    }

    @Provides
    @IntoMap
    @ViewModelKey(WelcomeViewModel.class)
    public ViewModel provideWelcomeViewModel(AccountRepository accountRepository) {
        return new WelcomeViewModel(accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(MainActivityViewModel.class)
    public ViewModel provideMainActivityViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        return new MainActivityViewModel(xmppRepository, authRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    public ViewModel provideLoginViewModel(AuthRepository authRepository, AccountRepository accountRepository, XMPPRepository xmppRepository) {
        return new LoginViewModel(authRepository, accountRepository, xmppRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(RegisterViewModel.class)
    public ViewModel provideRegisterViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        return new RegisterViewModel(xmppRepository, authRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel.class)
    public ViewModel provideForgotPasswordViewModel(AccountRepository accountRepository) {
        return new ForgotPasswordViewModel(accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(HomeViewModel.class)
    public ViewModel provideHomeViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        return new HomeViewModel(xmppRepository, authRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ContactViewModel.class)
    public ViewModel provideContactViewModel(XMPPRepository xmppRepository, AccountRepository accountRepository) {
        return new ContactViewModel(xmppRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(EditProfileViewModel.class)
    public ViewModel provideEditProfileViewModel(AccountRepository accountRepository) {
        return new EditProfileViewModel(accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SecurityViewModel.class)
    public ViewModel provideSecurityViewModel(XMPPRepository xmppRepository, AuthRepository authRepository, AccountRepository accountRepository) {
        return new SecurityViewModel(xmppRepository, authRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ChatViewModel.class)
    public ViewModel provideChatViewModel(XMPPRepository xmppRepository, AccountRepository accountRepository) {
        return new ChatViewModel(xmppRepository, accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(AboutViewModel.class)
    public ViewModel provideAboutViewModel(AccountRepository accountRepository) {
        return new AboutViewModel(accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ForgotAnswerViewModel.class)
    public ViewModel provideForgotAnswerViewModel(AccountRepository accountRepository) {
        return new ForgotAnswerViewModel(accountRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ResetPasswordViewModel.class)
    public ViewModel provideResetPasswordViewModel(AccountRepository accountRepository, AuthRepository authRepository, XMPPRepository xmppRepository) {
        return new ResetPasswordViewModel(accountRepository, authRepository, xmppRepository);
    }

}
