package com.wrappy.android.di;

import com.wrappy.android.about.AboutFragment;
import com.wrappy.android.about.AboutPageFragment;
import com.wrappy.android.chat.ChatAddMemberFragment;
import com.wrappy.android.chat.ChatFragment;
import com.wrappy.android.chat.ChatGroupDetailsFragment;
import com.wrappy.android.chat.ChatMessageFragment;
import com.wrappy.android.chat.ChatSettingsFragment;
import com.wrappy.android.chat.ChatTranslateFragment;
import com.wrappy.android.chat.gallery.ChatImageGalleryFragment;
import com.wrappy.android.contact.ContactAddFragment;
import com.wrappy.android.contact.ContactEditFragment;
import com.wrappy.android.contact.ContactFragment;
import com.wrappy.android.forgot.ForgotPasswordChangePasswordFragment;
import com.wrappy.android.forgot.ForgotPasswordFragment;
import com.wrappy.android.forgot.ForgotPasswordQuestionFragment;
import com.wrappy.android.home.HomeChatsFragment;
import com.wrappy.android.home.HomeContactsFragment;
import com.wrappy.android.home.HomeProfileFragment;
import com.wrappy.android.home.profile.EditProfileFragment;
import com.wrappy.android.home.profile.EditProfileInfoFragment;
import com.wrappy.android.home.profile.EditProfileSmsVerificationFragment;
import com.wrappy.android.login.ForgotUsernameFragment;
import com.wrappy.android.login.LoginFragment;
import com.wrappy.android.login.LoginPasswordFragment;
import com.wrappy.android.login.LoginPatternFragment;
import com.wrappy.android.login.LoginRegisterPhoneFragment;
import com.wrappy.android.login.LoginUserFragment;
import com.wrappy.android.misc.MyQRCodeFragment;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerEmailInputFragment;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerEmailSentFragment;
import com.wrappy.android.misc.forgotanswer.ForgotAnswerFragment;
import com.wrappy.android.misc.reset.ResetPasswordFragment;
import com.wrappy.android.misc.reset.ResetPasswordPatternFragment;
import com.wrappy.android.misc.reset.ResetPasswordTextFragment;
import com.wrappy.android.register.RegisterFragment;
import com.wrappy.android.register.RegisterPasswordFragment;
import com.wrappy.android.register.RegisterProfileFragment;
import com.wrappy.android.register.RegisterQuestionFragment;
import com.wrappy.android.register.RegisterSmsVerificationFragment;
import com.wrappy.android.security.SecurityBlockedUsersFragment;
import com.wrappy.android.security.SecurityChangePasswordFragment;
import com.wrappy.android.security.SecurityChooseQuestionFragment;
import com.wrappy.android.security.SecurityFragment;
import com.wrappy.android.security.SecurityPageFragment;
import com.wrappy.android.security.SecurityPatternFragment;
import com.wrappy.android.security.SecurityValidatePasswordFragment;
import com.wrappy.android.security.SecurityValidatePatternFragment;
import com.wrappy.android.welcome.WelcomeFragment;

import dagger.Subcomponent;

@FragmentScope
@Subcomponent(modules = {FragmentProviderModule.class})
public interface FragmentInjector {
    void inject(WelcomeFragment welcomeFragment);

    void inject(LoginUserFragment loginUserFragment);

    void inject(LoginFragment loginFragment);

    void inject(RegisterFragment registerFragment);

    void inject(RegisterPasswordFragment registerPasswordFragment);

    void inject(RegisterQuestionFragment registerQuestionFragment);

    void inject(RegisterProfileFragment registerProfileFragment);

    void inject(RegisterSmsVerificationFragment registerSmsVerificationFragment);

    void inject(LoginPasswordFragment loginPasswordFragment);

    void inject(ForgotPasswordFragment forgotPasswordFragment);

    void inject(ForgotPasswordQuestionFragment forgotPasswordQuestionFragment);

    void inject(ForgotPasswordChangePasswordFragment forgotPasswordChangePasswordFragment);

    void inject(HomeProfileFragment homeProfileFragment);

    void inject(AboutFragment aboutFragment);

    void inject(AboutPageFragment aboutPageFragment);

    void inject(SecurityFragment securityFragment);

    void inject(SecurityPageFragment securityPageFragment);

    void inject(SecurityChooseQuestionFragment securityChooseQuestionFragment);

    void inject(SecurityChangePasswordFragment securityChangePasswordFragment);

    void inject(SecurityBlockedUsersFragment securityBlockedUsersFragment);

    void inject(ChatFragment chatFragment);

    void inject(HomeChatsFragment homeChatsFragment);

    void inject(ContactFragment contactFragment);

    void inject(HomeContactsFragment homeContactsFragment);

    void inject(ForgotUsernameFragment forgotUsernameFragment);

    void inject(LoginPatternFragment loginPatternFragment);

    void inject(SecurityPatternFragment securityPatternFragment);

    void inject(ContactAddFragment contactAddFragment);

    void inject(ContactEditFragment contactEditFragment);

    void inject(MyQRCodeFragment myQRCodeFragment);

    void inject(ChatMessageFragment chatMessageFragment);

    void inject(EditProfileFragment editProfileFragment);

    void inject(EditProfileInfoFragment editProfileFragment);

    void inject(EditProfileSmsVerificationFragment editProfileSmsVerificationFragment);

    void inject(SecurityValidatePatternFragment securityValidatePatternFragment);

    void inject(SecurityValidatePasswordFragment securityValidatePasswordFragment);

    void inject(ForgotAnswerFragment forgotAnswerFragment);

    void inject(ForgotAnswerEmailInputFragment forgotAnswerEmailInputFragment);

    void inject(ForgotAnswerEmailSentFragment forgotAnswerEmailSentFragment);

    void inject(ChatSettingsFragment chatSettingsFragment);

    void inject(ChatGroupDetailsFragment chatGroupDetailsFragment);

    void inject(ChatAddMemberFragment chatAddMemberFragment);

    void inject(ResetPasswordFragment resetPasswordFragment);

    void inject(ResetPasswordTextFragment resetPasswordTextFragment);

    void inject(ResetPasswordPatternFragment resetPasswordPatternFragment);

    void inject(LoginRegisterPhoneFragment loginRegisterPhoneFragment);

    void inject(ChatTranslateFragment chatTranslateFragment);

    void inject(ChatImageGalleryFragment chatImageGalleryFragment);

    /* void inject(LoginFragment loginFragment); */
}
