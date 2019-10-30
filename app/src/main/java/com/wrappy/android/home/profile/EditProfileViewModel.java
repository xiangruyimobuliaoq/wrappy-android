package com.wrappy.android.home.profile;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.db.entity.AccountInfo;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.AccountBody;
import com.wrappy.android.server.account.body.request.FileBody.Type;

public class EditProfileViewModel extends ViewModel {
    private AccountRepository mAccountRepository;
    private String name;
    private String userId;
    private String phoneNumber;
    private String countryCode;
    private String email;
    private String profileUrl;
    private String bannerUrl;
    private boolean isSmsVerified;

    public EditProfileViewModel(AccountRepository accountRepository) {
        mAccountRepository = accountRepository;
    }

    public LiveData<Resource<AccountInfo>> getAccountInfo() {
        return mAccountRepository.getCurrentAccountInfo();
    }

    public LiveData<Resource<Boolean>> validateEmail(String email) {
        return mAccountRepository.validateEmail(email);
    }

    public LiveData<Resource<Boolean>> validatePhoneAndSendSmsCode(String countryCode, String phoneNumber) {
        return mAccountRepository.validatePhone(countryCode, phoneNumber, true, true);
    }

    public LiveData<Resource<Boolean>> validateSmsCode(String countryCode, String phoneNumber, String smsCode) {
        return mAccountRepository.validateSmsCode(countryCode, phoneNumber, smsCode);
    }

    public LiveData<Resource<AccountBody>> updateAccount(String name, String email, String mobilePhone, Uri avatarImage, Uri backgroundImage) {
        return mAccountRepository.updateAccount(name,
                email,
                mobilePhone,
                avatarImage != null ? new Base64ImageFile(avatarImage) : null,
                backgroundImage != null ? new Base64ImageFile(backgroundImage) : null);
    }

    public void setSmsVerified(boolean smsVerified) {
        this.isSmsVerified = smsVerified;
    }

    public boolean isSmsVerified() {
        return isSmsVerified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }


    public String getCountryCode() {
        return countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getUserFileUrl(Type type) {
        return mAccountRepository.getUserFileUrl(type);
    }
}
