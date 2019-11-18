package com.wrappy.android.server.account;


import java.util.List;

import android.arch.lifecycle.LiveData;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.server.account.body.request.*;
import com.wrappy.android.server.account.body.request.data.*;
import com.wrappy.android.server.account.body.response.AccountStatusResponse;
import com.wrappy.android.server.account.body.response.GetVersionResponse;
import com.wrappy.android.server.account.body.response.SendSmsCodeResponse;
import com.wrappy.android.server.account.body.response.VCardInfoResponse;
import com.wrappy.android.server.account.body.response.ValidatePhoneResponse;
import com.wrappy.android.server.response.CoreServiceResponse;
import com.wrappy.android.server.util.ApiResult;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AccountService {

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<String>>> getTermsAndConditions(@Body AccountHelperBody<GetTermsAndConditions> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<GetVersionResponse>>> getVersion(@Body AccountHelperBody<GetVersion> body);

    /**
     * Validate Mobile Phone，Check formatter and whether mobile has been used.
     */
    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<ValidatePhoneResponse>>> validatePhone(@Body AccountHelperBody<ValidatePhone> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<SendSmsCodeResponse>>> sendSmsCode(@Body AccountHelperBody<SendSmsCode> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<Object>>> validateSmsCode(@Body AccountHelperBody<ValidateSmsCode> body);

    /**
     * Validate Email Address，Check formatter and whether email has been used.
     */
    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<String>>> validateEmail(@Body AccountHelperBody<ValidateEmail> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<String>>> validateUsername(@Body AccountHelperBody<ValidateUsername> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<List<String>>>> validatePasswordFormat(@Body AccountHelperBody<ValidatePassword> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<List<SecurityQuestionAnswerBody>>>> getSecurityQuestions(@Body AccountHelperBody<GetSecurityQuestions> body);

    /**
     * Create both XMPP user AND N-Wrappy account AND send registration email
     */
    @POST("/api/accounts")
    LiveData<ApiResult<CoreServiceResponse<AccountBody>>> createAccount(@Body AccountBody accountInfo);

    @PUT("/api/accounts")
    LiveData<ApiResult<CoreServiceResponse<AccountBody>>> updateAccount(@Body AccountBody accountInfo);

    /**
     * Save avatar & background picture to the current login user
     */
    @PUT("/api/accounts/userFile")
    LiveData<ApiResult<CoreServiceResponse<Object>>> saveUserFile(@Body FileBody body);

    @GET("/api/accounts/userFile/{type}")
    LiveData<ApiResult<CoreServiceResponse<Base64ImageFile>>> getUserImage(@Path(value = "type") String type);

    /**
     * Find account by phone number & email address
     */
    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<String>>> findUserAccount(@Body AccountHelperBody<FindUserAccount> body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<AccountStatusResponse>>> getAccountStatus(@Body AccountHelperBody<AccountStatus> body);

    @GET("/api/accounts")
    LiveData<ApiResult<CoreServiceResponse<AccountBody>>> getAccountInfo();

    @POST("/api/accounts/lockouts")
    LiveData<ApiResult<CoreServiceResponse<AccountBody>>> lockAccount();

    @POST("/api/accounts/validatePassword/{password}")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> validateTextPassword(@Path(value = "password") String password);

    @POST("/api/accounts/validatePatternPassword/{patternPassword}")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> validatePatternPassword(@Path(value = "patternPassword") String pattern);

    @GET("/api/accounts/securityQuestions/{username}")
    LiveData<ApiResult<CoreServiceResponse<List<SecurityQuestionAnswerBody>>>> findUserSecurityQuestions(@Path(value = "username") String body, @Query("count") int count);

    @POST("/api/accounts/securityQuestions/{username}/validate")
    LiveData<ApiResult<CoreServiceResponse<String>>> validateUserSecurityQuestionAnswer(@Path(value = "username") String username, @Body List<SecurityQuestionAnswerBody> body);

    @PUT("/api/accounts/setPatternPasswordFlag/{flag}")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> setPatternPasswordFlag(@Path(value = "flag") boolean flag);

    @GET("/api/accounts/getPatternPasswordFlag")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> getPatternPasswordFlag();

    @PUT("/api/accounts/updatePassword")
    LiveData<ApiResult<CoreServiceResponse<List<String>>>> updateUserPassword(@Body UpdatePasswordBody body);

    @PUT("/api/accounts/updatePatternPassword")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> updatePatternPassword(@Body UpdatePasswordBody body);

    @POST("/api/accounts/helper")
    LiveData<ApiResult<CoreServiceResponse<String>>> getShareWrappyContent(@Body AccountHelperBody<GetShareContent> body);

    @POST("/api/accounts/securityQuestionsWithAnswer")
    LiveData<ApiResult<CoreServiceResponse<List<SecurityQuestionAnswerBody>>>> getSecurityQuestionsWithAnswer(@Body TextPasswordBody body);

    @PUT("/api/accounts/updateSecurityQuestions")
    LiveData<ApiResult<CoreServiceResponse<Boolean>>> updateUserSecurityQuestions(@Body List<SecurityQuestionAnswerBody> body);

    @GET("/api/accounts/vCard/{username}")
    LiveData<ApiResult<CoreServiceResponse<VCardInfoResponse>>> getVCardInfo(@Path(value = "username") String username);

    @PUT("/api/accounts/resetPassword")
    LiveData<ApiResult<CoreServiceResponse<Object>>> resetUserPassword(@Body ResetPasswordBody body);

    @POST("/api/accounts/sendRecoveryEmail")
    LiveData<ApiResult<CoreServiceResponse<Object>>> sendRecoveryEmail(@Body SendRecoveryEmailBody body);

    @PUT("/api/accounts/groupFile")
    LiveData<ApiResult<CoreServiceResponse<Object>>> saveGroupFile(@Body GroupFileBody body);
}
