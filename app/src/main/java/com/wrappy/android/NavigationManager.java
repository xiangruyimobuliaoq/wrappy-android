package com.wrappy.android;


import android.support.v4.app.FragmentManager;

import com.wrappy.android.about.AboutFragment;
import com.wrappy.android.chat.ChatFragment;
import com.wrappy.android.common.AbstractNavigationManager;
import com.wrappy.android.contact.ContactFragment;
import com.wrappy.android.forgot.ForgotPasswordFragment;
import com.wrappy.android.home.HomeFragment;
import com.wrappy.android.home.profile.EditProfileFragment;
import com.wrappy.android.login.LoginFragment;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerFragment;
import com.wrappy.android.misc.MyQRCodeFragment;
import com.wrappy.android.misc.reset.ResetPasswordFragment;
import com.wrappy.android.register.RegisterFragment;
import com.wrappy.android.security.SecurityFragment;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;
import com.wrappy.android.welcome.WelcomeFragment;

import java.util.ArrayList;
import java.util.List;

public class NavigationManager extends AbstractNavigationManager {

    public NavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.main_container;
    }

    public void showWelcomePage() {
        clearBackstack();
        showPage(new WelcomeFragment(), false);
    }

    public void showLoginPage() {
        clearBackstack();
        showPage(new LoginFragment(), false);
    }

    public void showRegisterPage(String countryCode, String phoneNumber) {
        showPage(RegisterFragment.create(countryCode, phoneNumber), true);
    }

    public void showHomePage(boolean showWelcome) {
        clearBackstack();
        showPage(HomeFragment.create(showWelcome), false);
    }

    public void showForgotPasswordPage(String username, List<SecurityQuestionAnswerBody> questionList) {
        showPage(ForgotPasswordFragment.create(username, questionList), true);
    }

    public void showEditProfilePage(String name,
                                    String userId,
                                    String countryCode,
                                    String phoneNumber,
                                    String email,
                                    String profileUrl,
                                    String bannerUrl) {
        showPage(
                EditProfileFragment.create(name,
                        userId,
                        countryCode,
                        phoneNumber,
                        email,
                        profileUrl,
                        bannerUrl),
                true);
    }


    public void showMyQRCodePage(String userJid) {
        showPage(MyQRCodeFragment.create(userJid), true);
    }

    public void showAboutPage() {
        showPage(new AboutFragment(), true);
    }

    public void showSecurityPage(String patternPassword,
                                 boolean patternPasswordFlag) {
        showPage(
                SecurityFragment.create(patternPassword,
                        patternPasswordFlag),
                true);
    }

    public void showChatPage(String chatJid, String chatName, String chatType) {
        showPage(ChatFragment.create(chatJid,chatName,chatType), true);
    }

    public void showContactPage(int type, String userJid) {
        showPage(ContactFragment.create(type, userJid), true);
    }

    public void showForgotAnswerPage(String type) {
        showPage(ForgotAnswerFragment.create(type), true);
    }

    public void showResetPasswordPage(String mode, String secretKey) {
        clearBackstack();
        showPage(ResetPasswordFragment.create(mode, secretKey), false);
    }
}
