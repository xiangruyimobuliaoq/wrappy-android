package com.wrappy.android.server.util;


import java.io.IOException;
import java.util.Locale;

import android.net.Uri;
import com.wrappy.android.BuildConfig;
import com.wrappy.android.server.ServerConstants;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthBasicInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!request.url().host().equals(
                ServerConstants.CORE_SERVER_URL_HOST)) {
            return chain.proceed(request);
        }

        request = setAuthHeader(request, ServerConstants.CLIENT_SECRET);
        return chain.proceed(request);
    }

    private Request setAuthHeader(Request request, String clientCredential) {
        return request.newBuilder()
                .header("Authorization", String.format("Basic %s", clientCredential))
                .header("Accept-Language", Locale.getDefault().getLanguage())
                .build();
    }
}
