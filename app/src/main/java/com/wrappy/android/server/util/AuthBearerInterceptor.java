package com.wrappy.android.server.util;


import java.io.IOException;
import java.util.Locale;

import android.net.Uri;
import com.wrappy.android.BuildConfig;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.server.AuthRepository;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthBearerInterceptor implements Interceptor {
    private AuthRepository mAuthRepository;

    public AuthBearerInterceptor(AuthRepository authRepository) {
        mAuthRepository = authRepository;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!request.url().host().equals(
                ServerConstants.CORE_SERVER_URL_HOST)) {
            return chain.proceed(request);
        }

        String accessToken = mAuthRepository.getAccessToken();
        request = setAuthHeader(request, accessToken);

        Response response = chain.proceed(request);
        if (response.code() == ServerConstants.RESPONSE_CODE_UNAUTHORIZED) {
            synchronized (this) {
                if (mAuthRepository.refreshTokenSync(accessToken)) {
                    accessToken = mAuthRepository.getAccessToken();
                    if (accessToken != null) {
                        request = setAuthHeader(request, accessToken);
                        response = chain.proceed(request);
                    }
                }
            }
        }
        return response;
    }

    private Request setAuthHeader(Request request, String accessToken) {
        return request.newBuilder()
                .header("Authorization", String.format("Bearer %s", accessToken))
                .header("Accept-Language", Locale.getDefault().getLanguage())
                .build();
    }
}
