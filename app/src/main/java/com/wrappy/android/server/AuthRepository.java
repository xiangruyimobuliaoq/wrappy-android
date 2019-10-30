package com.wrappy.android.server;


import java.io.IOException;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.wrappy.android.BuildConfig;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.glide.GlideUtils;
import com.wrappy.android.common.utils.KeyStoreUtils;
import com.wrappy.android.common.utils.Supplier;
import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.db.entity.AccountInfo;
import com.wrappy.android.db.entity.AuthToken;
import com.wrappy.android.server.account.body.request.AccountBody;
import com.wrappy.android.server.response.AuthResponse;
import com.wrappy.android.server.util.ApiResult;
import com.wrappy.android.xmpp.aws.AWSCertificate;

import retrofit2.Response;

public class AuthRepository {

    public enum LoginStatus {
        IN, OUT, CONFLICT
    }

    private MutableLiveData<LoginStatus> mLiveLoginStatus = new MutableLiveData<>();

    private static final String GRANT_PASSWORD = "password";
    private static final String GRANT_REFRESH_TOKEN = "refresh_token";

    public static final String PREF_KEY_USER = "username";
    private static final String PREF_KEY_PASSWORD = "password";

    public static final String PREF_KEY_AWS_SECRET_ID = "aws_secret_id";
    public static final String PREF_KEY_AWS_ACCESS_KEY = "aws_access_key";
    public static final String PREF_KEY_AWS_BUCKET_NAME = "aws_bucket_name";

    private AppDatabase mDatabase;
    private AuthorizationService mAuthService;
    private AppExecutors mAppExecutors;
    private SharedPreferences mSharedPreferences;

    private AuthToken mAuthToken;
    private String mAccountType;
    private Runnable mPendingLogin;

    private String mLocalUsername;
    private String mLocalPassword;
    private AWSCertificate mAwsCertificate;

    public AuthRepository(AppDatabase database,
                          AppExecutors appExecutors,
                          AuthorizationService authService,
                          SharedPreferences sharedPreferences) {
        mDatabase = database;
        mAppExecutors = appExecutors;
        mAuthService = authService;
        mSharedPreferences = sharedPreferences;
        mAppExecutors.diskIO().execute(() -> {
            mAuthToken = mDatabase.authDao().getToken(ServerConstants.ACC_TYPE_WRAPPY);
            if (mAuthToken == null) {
                mAuthToken = mDatabase.authDao().getToken(ServerConstants.ACC_TYPE_SYSTEM);
            }
            if (mAuthToken != null) {
                mAccountType = mAuthToken.getAccountType();
            }
        });
    }

    public String getAccessToken() {
        return mAuthToken != null ? mAuthToken.getAccessToken() : null;
    }

    public String getAccountId() {
        return mAuthToken != null ? mAuthToken.getAccountId() : null;
    }

    public void finishPendingLogin() {
        if (mPendingLogin != null) {
            mAppExecutors.mainThread().execute(mPendingLogin);
        }
    }

    public void storeLocalUsername(String username) {
        mSharedPreferences.edit()
                .putString(PREF_KEY_USER,
                        KeyStoreUtils.encrypt(username))
                .apply();
        mLocalUsername = username;
    }

    public void storeLocalPassword(String password) {
        mSharedPreferences.edit()
                .putString(PREF_KEY_PASSWORD,
                        KeyStoreUtils.encrypt(password))
                .apply();
        mLocalPassword = password;
    }

    public void storeAWSCertificate(AWSCertificate awsCertificate) {
        mSharedPreferences.edit()
                .putString(PREF_KEY_AWS_ACCESS_KEY,
                        KeyStoreUtils.encrypt(awsCertificate.getAccessKey()))
                .putString(PREF_KEY_AWS_SECRET_ID,
                        KeyStoreUtils.encrypt(awsCertificate.getSecretID()))
                .putString(PREF_KEY_AWS_BUCKET_NAME,
                        KeyStoreUtils.encrypt(awsCertificate.getBucketName()))
                .apply();
        mAwsCertificate = awsCertificate;
    }

    public AWSCertificate getAWSCertificate() {
        if (mAwsCertificate == null) {
            mAwsCertificate = new AWSCertificate(
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_AWS_ACCESS_KEY, "")),
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_AWS_SECRET_ID, "")),
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_AWS_BUCKET_NAME, "")));
        }
        return mAwsCertificate;
    }

    public String getLocalUsername() {
        if (mLocalUsername == null) {
            mLocalUsername = KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_USER, ""));
        }
        return mLocalUsername;
    }

    public String getLocalPassword() {
        if (mLocalPassword == null) {
            mLocalPassword = KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_PASSWORD, ""));
        }
        return mLocalPassword;
    }

    private void storeToken(AuthToken token) {
        mAuthToken = token;
        mAccountType = mAuthToken.getAccountType();
        mAppExecutors.diskIO().execute(() -> {
            mDatabase.authDao().addToken(token);
        });
    }

    private void storeAccountInfo(AccountBody accountBody, String password) {
        mAppExecutors.diskIO().execute(() -> {
            AccountInfo accountInfo = new AccountInfo(accountBody);
            accountInfo.getAccountBody().extendedInfo.ejabberdPassword = password;
            mDatabase.accountInfoDao().addAccountInfo(accountInfo);
        });
    }

    private LiveData<ApiResult<AuthResponse>> loginInternal(String username, String password) {
        LiveData<ApiResult<AuthResponse>> loginRequest = mAuthService.requestAuthToken(GRANT_PASSWORD,
                username,
                password,
                ServerConstants.CLIENT_ID);
        mPendingLogin = null;
        return Transformations.map(loginRequest, result -> {
            if (result.isSuccessful() && result.body.getAccessToken() != null) {
                Log.d("Details", username + " " + password);
                storeLocalUsername(username);
                storeLocalPassword(password);

                AuthToken token = new AuthToken(result.body);
                mAuthToken = token;
                mPendingLogin = () -> {
                    storeToken(token);
                    storeAccountInfo(result.body.getAccount(), password);
                    if (token.getAccountType().equals(ServerConstants.ACC_TYPE_WRAPPY)) {
                        mLiveLoginStatus.postValue(LoginStatus.IN);
                    }
                    mPendingLogin = null;
                };
                if (mLiveLoginStatus.getValue() == LoginStatus.IN ||
                        !result.body.getAccount().extendedInfo.patternPasswordFlag) {
                    mAppExecutors.mainThread().execute(mPendingLogin);
                }
            }
            return result;
        });
    }

    public LiveData<Resource<AccountBody>> login(String username, String password) {
        MediatorLiveData<Resource<AccountBody>> mld = new MediatorLiveData<>();
        mld.postValue(Resource.loading(null));
        mld.addSource(loginInternal(username, password), loginResult -> {
            if (loginResult.isSuccessful()) {
                if (loginResult.body.getAccessToken() != null) {
                    mld.postValue(Resource.success(loginResult.body.getAccount()));
                } else {
                    mld.postValue(Resource.serverError(loginResult.errorMessage, null));
                }
            } else {
                mld.postValue(Resource.clientError(loginResult.errorMessage, null));
            }
        });
        return mld;
    }

    /**
     * Checks if access token is present before performing an REST api request.
     * If not, try to login system account and retry.
     *
     * @param apiRequest               the LiveData request performed after checking the access token
     * @param tokenExpiredRetryRequest returns the LiveData request to perform if access token refresh fails
     */
    public <T> LiveData<ApiResult<T>> checkAccessToken(LiveData<ApiResult<T>> apiRequest,
                                                       @Nullable Supplier<LiveData<ApiResult<T>>> tokenExpiredRetryRequest) {
        MediatorLiveData<ApiResult<T>> mld = new MediatorLiveData<>();
        if (getAccessToken() != null) {
            mld.addSource(apiRequest, requestResult -> {
                mld.removeSource(apiRequest);
                // token may be expired and deleted by refreshToken()
                if (getAccessToken() == null) {
                    // try to login and retry request by recursive call
                    if (tokenExpiredRetryRequest != null) {
                        mld.addSource(checkAccessToken(tokenExpiredRetryRequest.get(), null),
                                mld::postValue);
                    } else if (BuildConfig.DEBUG) {
                        mld.postValue(requestResult);
                    }
                } else {
                    // proceed with original api request
                    mld.postValue(requestResult);
                }
            });
        } else {
            // login
            LiveData<ApiResult<AuthResponse>> loginCall;
            if (ServerConstants.ACC_TYPE_WRAPPY.equals(mAccountType)) {
                loginCall = loginInternal(getLocalUsername(), getLocalPassword());
            } else {
                loginCall = loginInternal(ServerConstants.SYSTEM_ACCOUNT_ID, ServerConstants.SYSTEM_ACCOUNT_PASSWORD);
            }

            mld.addSource(loginCall, loginResult -> {
                mld.removeSource(loginCall);
                // proceed with original api request
                if (loginResult.isSuccessful()) {
                    if (loginResult.body.getAccessToken() != null) {
                        mld.addSource(apiRequest, mld::postValue);
                    } else {
                        // login failed due to invalid credentials
                        logout();
                    }
                } else {
                    mld.addSource(apiRequest, mld::postValue);
                }
            });
        }
        return mld;
    }

    private void deleteTokens() {
        mAuthToken = null;
        mAccountType = null;
        mAppExecutors.diskIO().execute(() -> {
            mDatabase.authDao().deleteAll();
            mDatabase.accountInfoDao().deleteAll();
            mDatabase.contactDao().deleteAll();
            mDatabase.messageDao().deleteAll();
            //mDatabase.chatDao().deleteAll();
        });
    }

    private void clearGlide() {
        GlideUtils.resetVersionCache();
        mAppExecutors.mainThread().execute(GlideUtils::clearGlideMemory);
        mAppExecutors.diskIO().execute(GlideUtils::clearGlideDiskCache);
    }

    public void logout() {
        logout(false);
    }

    public void logout(boolean conflict) {
        if (mAuthToken != null && mAuthToken.getAccountType().equals(ServerConstants.ACC_TYPE_WRAPPY)) {
            if(conflict) {
                mLiveLoginStatus.postValue(LoginStatus.CONFLICT);
            } else {
                mLiveLoginStatus.postValue(LoginStatus.OUT);
            }
        }

        mSharedPreferences.edit()
                .remove(PREF_KEY_USER)
                .remove(PREF_KEY_PASSWORD)
                .remove(PREF_KEY_AWS_ACCESS_KEY)
                .remove(PREF_KEY_AWS_SECRET_ID)
                .remove(PREF_KEY_AWS_BUCKET_NAME)
                .apply();
        mLocalPassword = null;
        mLocalUsername = null;
        mAwsCertificate = null;

        deleteTokens();
        clearGlide();
    }

    @WorkerThread
    public synchronized boolean refreshTokenSync(String currentAccessToken) throws IOException {
        if (getAccessToken() == null || currentAccessToken == null || !currentAccessToken.equals(getAccessToken())) {
            // accessToken is null or outdated
            return true;
        }

        Response<AuthResponse> response = mAuthService.refreshAuthToken(GRANT_REFRESH_TOKEN,
                mAuthToken.getRefreshToken(), ServerConstants.CLIENT_ID).execute();

        if (response.isSuccessful()) {
            storeToken(new AuthToken(response.body()));
        } else {
            if (response.errorBody() != null) {
                // trigger re-login
                mAuthToken = null;
                return false;
            }
        }
        return true;
    }

    public LiveData<LoginStatus> getLoginStatus() {
        return mLiveLoginStatus;
    }

    public void checkUserLoginToken() {
        mAppExecutors.diskIO().execute(() -> {
            AuthToken token = mDatabase.authDao().getToken(ServerConstants.ACC_TYPE_WRAPPY);
            if (token != null) {
                mLiveLoginStatus.postValue(LoginStatus.IN);
            } else {
                mLiveLoginStatus.postValue(LoginStatus.OUT);
            }
        });
    }
}
