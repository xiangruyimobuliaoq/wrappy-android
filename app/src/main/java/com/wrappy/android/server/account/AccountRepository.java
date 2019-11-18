package com.wrappy.android.server.account;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.glide.GlideUtils;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.db.entity.AccountInfo;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.body.request.*;
import com.wrappy.android.server.account.body.request.FileBody.Type;
import com.wrappy.android.server.account.body.request.data.*;
import com.wrappy.android.server.account.body.response.AccountStatusResponse;
import com.wrappy.android.server.account.body.response.GetVersionResponse;
import com.wrappy.android.server.account.body.response.VCardInfoResponse;
import com.wrappy.android.server.response.CoreServiceResponse;
import com.wrappy.android.server.util.ApiResult;
import com.wrappy.android.server.util.NetworkBoundResource;

public class AccountRepository {
    private static final String PREF_KEY_TC_VERSION = "tc_version";
    private static final String PREF_KEY_TC_ACCEPTED = "tc_accepted";

    private AccountService mAccountService;
    private AuthRepository mAuthRepository;
    private AppDatabase mDatabase;
    private AppExecutors mAppExecutors;
    private SharedPreferences mSharedPreferences;

    public AccountRepository(AccountService accountService,
                             AuthRepository authRepository,
                             AppDatabase database,
                             AppExecutors appExecutors,
                             SharedPreferences sharedPreferences) {
        mAccountService = accountService;
        mDatabase = database;
        mAuthRepository = authRepository;
        mAppExecutors = appExecutors;
        mSharedPreferences = sharedPreferences;
    }

    public void storePrefTCAccepted(boolean isAccepted) {
        mSharedPreferences.edit()
                .putBoolean(PREF_KEY_TC_ACCEPTED, isAccepted)
                .apply();
    }

    public boolean getPrefTCAccepted() {
        return mSharedPreferences.getBoolean(PREF_KEY_TC_ACCEPTED, false);
    }

    private void storePrefTCVersion(float version) {
        mSharedPreferences.edit()
                .putFloat(PREF_KEY_TC_VERSION, version)
                .apply();
    }

    private float getPrefTCVersion() {
        return mSharedPreferences.getFloat(PREF_KEY_TC_VERSION, 0F);
    }

    public String getUserFileUrl(Type fileType) {
        return ServerConstants.CORE_SERVER_URL + "api/accounts/userFile/" + fileType.toString();
    }

    public String getContactFileUrl(Type fileType, String userId) {
        return getContactFileUrl(fileType, userId, false);
    }

    public String getContactFileUrl(Type fileType, String userId, boolean isGroup) {
        String path = "api/accounts/" + (isGroup ? "getGroupFile" : "userFile") + "/";

        return ServerConstants.CORE_SERVER_URL + path + userId + "/" + fileType.toString();
    }

    public LiveData<Resource<GetVersionResponse>> getWrappyTCVersion() {
        GetVersion verRequest = new GetVersion();
        AccountHelperBody<GetVersion> param = new AccountHelperBody<>(verRequest);

        MediatorLiveData<Resource<GetVersionResponse>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getVersion(param),
                        () -> mAccountService.getVersion(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            float resultTCVer = result.body.getData().termsAndConditionsVersion;
                            if (Float.compare(getPrefTCVersion(), resultTCVer) > 0) {
                                storePrefTCVersion(resultTCVer);
                                storePrefTCAccepted(false);
                            }
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<AccountInfo>> getCurrentAccountInfo() {
        return new NetworkBoundResource<AccountInfo, CoreServiceResponse<AccountBody>>(mAppExecutors) {
            @Override
            protected void saveCallResult(@NonNull CoreServiceResponse<AccountBody> item) {
                mDatabase.accountInfoDao().addAccountInfo(new AccountInfo(item.getData()));
            }

            @Override
            protected boolean shouldFetch(@Nullable AccountInfo data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<AccountInfo> loadFromDb() {
                return mDatabase.accountInfoDao().getAccountInfo(mAuthRepository.getAccountId());
            }

            @NonNull
            @Override
            protected LiveData<ApiResult<CoreServiceResponse<AccountBody>>> createCall() {
                return mAuthRepository.checkAccessToken(mAccountService.getAccountInfo(),
                        () -> mAccountService.getAccountInfo());
            }
        }.asLiveData();
    }

    public LiveData<Resource<String>> getTermsAndConditions(ServerLocale language) {
        GetTermsAndConditions tcRequest = new GetTermsAndConditions();
        AccountHelperBody<GetTermsAndConditions> param = new AccountHelperBody<>(tcRequest);
        param.setLanguage(language);

        MediatorLiveData<Resource<String>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getTermsAndConditions(param),
                        () -> mAccountService.getTermsAndConditions(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    /**
     * Validates phone number, checks format and if mobile phone is already registered
     *
     * @param phoneNumber validated mobile phone number
     * @param countryCode country code (ex. +86)
     * @return a Resource with phone number (full format) String
     */
    public LiveData<Resource<Boolean>> validatePhone(String countryCode, String phoneNumber, boolean checkRegistered, boolean sendValidationCode) {
        ValidatePhone validatePhone = new ValidatePhone(countryCode, phoneNumber);
        validatePhone.checkRegistered = checkRegistered;
        validatePhone.sendValidationCode = sendValidationCode;
        AccountHelperBody<ValidatePhone> param = new AccountHelperBody<>(validatePhone);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validatePhone(param),
                        () -> mAccountService.validatePhone(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                });
        return mld;
    }

    public LiveData<Resource<Boolean>> resendSmsCode(String countryCode, String phoneNumber) {
        SendSmsCode sendSmsCode = new SendSmsCode(countryCode, phoneNumber);
        AccountHelperBody<SendSmsCode> param = new AccountHelperBody<>(sendSmsCode);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.sendSmsCode(param),
                        () -> mAccountService.sendSmsCode(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> validateSmsCode(String countryCode, String phoneNumber, String smsCode) {
        ValidateSmsCode validateSmsCode = new ValidateSmsCode(countryCode, phoneNumber, "registration", smsCode);
        AccountHelperBody<ValidateSmsCode> param = new AccountHelperBody<>(validateSmsCode);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validateSmsCode(param),
                        () -> mAccountService.validateSmsCode(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<List<String>>> validatePasswordFormat(String password, ServerLocale language) {
        ValidatePassword validatePassword = new ValidatePassword(password);
        AccountHelperBody<ValidatePassword> param = new AccountHelperBody<>(validatePassword);
        //param.setLanguage(language.code);

        MediatorLiveData<Resource<List<String>>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validatePasswordFormat(param),
                        () -> mAccountService.validatePasswordFormat(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(Collections.emptyList()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), result.body.getData()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, Collections.emptyList()));
                    }
                });
        return mld;
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> getSecurityQuestions(ServerLocale language) {
        GetSecurityQuestions getSecurityQuestions = new GetSecurityQuestions();
        AccountHelperBody<GetSecurityQuestions> param = new AccountHelperBody<>(getSecurityQuestions);
        //param.setLanguage(language.code);

        MediatorLiveData<Resource<List<SecurityQuestionAnswerBody>>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getSecurityQuestions(param),
                        () -> mAccountService.getSecurityQuestions(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), Collections.emptyList()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, Collections.emptyList()));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> getUserSecurityQuestionsWithAnswer(String password) {
        TextPasswordBody textPasswordBody = new TextPasswordBody(password);

        MediatorLiveData<Resource<List<SecurityQuestionAnswerBody>>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getSecurityQuestionsWithAnswer(textPasswordBody),
                        () -> mAccountService.getSecurityQuestionsWithAnswer(textPasswordBody)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), Collections.emptyList()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, Collections.emptyList()));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> validateUsername(String username, String password) {
        ValidateUsername valUserRequest = new ValidateUsername(username, password);
        AccountHelperBody<ValidateUsername> param = new AccountHelperBody<>(valUserRequest);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validateUsername(param),
                        () -> mAccountService.validateUsername(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                });
        return mld;
    }

    public LiveData<Resource<Boolean>> validateEmail(String email) {
        ValidateEmail valEmailRequest = new ValidateEmail(email);
        AccountHelperBody<ValidateEmail> param = new AccountHelperBody<>(valEmailRequest);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validateEmail(param),
                        () -> mAccountService.validateEmail(param)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                });

        return mld;
    }

    public LiveData<Resource<AccountBody>> createAccount(String name,
                                                         String email,
                                                         String mobilePhone,
                                                         String username,
                                                         String password,
                                                         Base64ImageFile avatarImage,
                                                         Base64ImageFile backgroundImage,
                                                         String xmppServer,
                                                         List<SecurityQuestionAnswerBody> qaList) {
        AccountBody accountBody = new AccountBody();
        accountBody.firstName = name;
        accountBody.email = email;
        accountBody.mobilePhone = mobilePhone;

        accountBody.extendedInfo = new ExtendedInfoBody();
        accountBody.extendedInfo.username = username;
        accountBody.extendedInfo.password = password;
        accountBody.extendedInfo.avatar = avatarImage;
        accountBody.extendedInfo.backgroundImage = backgroundImage;
        accountBody.extendedInfo.server = xmppServer;

        accountBody.securityQuestions = new ArrayList<>();
        accountBody.securityQuestions.addAll(qaList);

        MediatorLiveData<Resource<AccountBody>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.createAccount(accountBody),
                        () -> mAccountService.createAccount(accountBody)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<AccountBody>> updateAccount(String name,
                                                         String email,
                                                         String mobilePhone,
                                                         Base64ImageFile avatarImage,
                                                         Base64ImageFile backgroundImage) {
        AccountBody accountBody = new AccountBody();
        accountBody.firstName = name;
        accountBody.email = email;
        accountBody.mobilePhone = mobilePhone;

        accountBody.extendedInfo = new ExtendedInfoBody();
        accountBody.extendedInfo.avatar = avatarImage;
        accountBody.extendedInfo.backgroundImage = backgroundImage;

        MediatorLiveData<Resource<AccountBody>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.updateAccount(accountBody),
                        () -> mAccountService.updateAccount(accountBody)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            AccountBody resultAccountBody = result.body.getData();
                            mAppExecutors.diskIO().execute(() -> {
                                mDatabase.accountInfoDao().addAccountInfo(new AccountInfo(resultAccountBody));
                                if (avatarImage != null || backgroundImage != null) {
                                    GlideUtils.clearGlideDiskCache();
                                }
                                mld.postValue(Resource.success(resultAccountBody));
                            });
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<List<SecurityQuestionAnswerBody>>> getUserSecurityQuestions(String username, int count) {
        MediatorLiveData<Resource<List<SecurityQuestionAnswerBody>>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.findUserSecurityQuestions(username, count),
                        () -> mAccountService.findUserSecurityQuestions(username, count)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), Collections.emptyList()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, Collections.emptyList()));
                    }
                });
        return mld;
    }

    public LiveData<Resource<String>> validateUserSecurityQuestionAnswers(String username, List<SecurityQuestionAnswerBody> qaList) {
        MediatorLiveData<Resource<String>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));

        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validateUserSecurityQuestionAnswer(username, qaList),
                        () -> mAccountService.validateUserSecurityQuestionAnswer(username, qaList)),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> forgotUserId(String email, String phone, boolean sendEmail, boolean sendSms) {
        FindUserAccount requestForgotUserId = new FindUserAccount(email, phone);
        requestForgotUserId.sendEmail = sendEmail;
        requestForgotUserId.sendSms = sendSms;

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));

        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.findUserAccount(new AccountHelperBody<>(requestForgotUserId)),
                        () -> mAccountService.findUserAccount(new AccountHelperBody<>(requestForgotUserId))),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<AccountStatusResponse>> getAccountStatus(String username) {
        AccountStatus statusRequest = new AccountStatus(username);
        AccountHelperBody<AccountStatus> param = new AccountHelperBody<>(statusRequest);

        MediatorLiveData<Resource<AccountStatusResponse>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));

        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getAccountStatus(param),
                        () -> mAccountService.getAccountStatus(param)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), result.body.getData()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> validateTextPassword(String password) {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validateTextPassword(password),
                        () -> mAccountService.validateTextPassword(password)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> validatePatternPassword(String patternPassword) {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.validatePatternPassword(patternPassword),
                        () -> mAccountService.validatePatternPassword(patternPassword)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> saveUserFile(FileBody.Type type, Base64ImageFile imageFile) {
        FileBody fileRequest = new FileBody(type, imageFile);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.saveUserFile(fileRequest),
                        () -> mAccountService.saveUserFile(fileRequest)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mAppExecutors.diskIO().execute(() -> {
                                GlideUtils.clearGlideDiskCache();
                                mld.postValue(Resource.success(true));
                            });
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> saveGroupFile(String groupId, FileBody.Type type, Base64ImageFile imageFile) {
        GroupFileBody fileRequest = new GroupFileBody(groupId, type, imageFile);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.saveGroupFile(fileRequest),
                        () -> mAccountService.saveGroupFile(fileRequest)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mAppExecutors.diskIO().execute(() -> {
                                GlideUtils.clearGlideDiskCache();
                                mld.postValue(Resource.success(true));
                            });
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<List<String>>> updateUserPassword(String oldPassword, String newPassword) {
        UpdatePasswordBody updatePassword = new UpdatePasswordBody();
        updatePassword.oldPass = oldPassword;
        updatePassword.newPass = newPassword;

        MediatorLiveData<Resource<List<String>>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.updateUserPassword(updatePassword),
                        () -> mAccountService.updateUserPassword(updatePassword)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(Collections.emptyList()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), result.body.getData()));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, Collections.emptyList()));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> updatePatternPassword(String patternPassword) {
        UpdatePasswordBody updatePassword = new UpdatePasswordBody();
        updatePassword.newPass = patternPassword;

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.updatePatternPassword(updatePassword),
                        () -> mAccountService.updatePatternPassword(updatePassword)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> updateUserSecurityQuestions(List<SecurityQuestionAnswerBody> questionList) {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.updateUserSecurityQuestions(questionList),
                        () -> mAccountService.updateUserSecurityQuestions(questionList)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> setPatternPasswordFlag(boolean flag) {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.setPatternPasswordFlag(flag),
                        () -> mAccountService.setPatternPasswordFlag(flag)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> getPatternPasswordFlag() {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getPatternPasswordFlag(),
                        () -> mAccountService.getPatternPasswordFlag()
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<String>> getShareWrappyContent(ServerLocale language) {
        GetShareContent shareContent = new GetShareContent();
        AccountHelperBody<GetShareContent> param = new AccountHelperBody<>(shareContent);
        //param.setLanguage(language.code);

        MediatorLiveData<Resource<String>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getShareWrappyContent(param),
                        () -> mAccountService.getShareWrappyContent(param)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<VCardInfoResponse>> getVCardInfo(String username) {
        MediatorLiveData<Resource<VCardInfoResponse>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.getVCardInfo(username),
                        () -> mAccountService.getVCardInfo(username)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(result.body.getData()));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), null));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, null));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> resetUserPassword(String secretKey, String newPassword, String newPatternPassword) {
        ResetPasswordBody resetPassword = new ResetPasswordBody(secretKey, newPassword, newPatternPassword);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.resetUserPassword(resetPassword),
                        () -> mAccountService.resetUserPassword(resetPassword)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> sendRecoveryEmail(String email, String type) {
        SendRecoveryEmailBody emailRequest = new SendRecoveryEmailBody(email, type);

        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(
                mAuthRepository.checkAccessToken(
                        mAccountService.sendRecoveryEmail(emailRequest),
                        () -> mAccountService.sendRecoveryEmail(emailRequest)
                ),
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                        } else {
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

    public LiveData<Resource<Boolean>> lockAccount() {
        MediatorLiveData<Resource<Boolean>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        Log.e("123","lockAccount执行中");
        mld.addSource(mAuthRepository.checkAccessToken(
                mAccountService.lockAccount(),
                () -> mAccountService.lockAccount())
                ,
                result -> {
                    if (result.isSuccessful()) {
                        if (result.body.isSuccess()) {
                            mld.postValue(Resource.success(true));
                            Log.e("123","lockAccount执行后1");
                        } else {
                            Log.e("123","lockAccount执行后2");
                            mld.postValue(Resource.serverError(result.body.getMessage(), false));
                        }
                    } else {
                            Log.e("123","lockAccount执行后3");
                        mld.postValue(Resource.clientError(result.errorMessage, false));
                    }
                }
        );
        return mld;
    }

}
