package com.wrappy.android.server;

import android.arch.lifecycle.LiveData;
import com.wrappy.android.server.response.AuthResponse;
import com.wrappy.android.server.util.ApiResult;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthorizationService {
    @POST("/api/oauth/token")
    @FormUrlEncoded
    LiveData<ApiResult<AuthResponse>> requestAuthToken(
            @Field("grant_type") String grantType,
            @Field("username") String username,
            @Field("password") String password,
            @Field("client_id") String clientId);

    @POST("/api/oauth/token")
    @FormUrlEncoded
    Call<AuthResponse> refreshAuthToken(
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Field("client_id") String clientId);
}
