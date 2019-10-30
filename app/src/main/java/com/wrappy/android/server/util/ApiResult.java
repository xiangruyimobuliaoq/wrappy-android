package com.wrappy.android.server.util;

import java.io.IOException;

import android.util.Log;
import com.wrappy.android.common.utils.ErrorUtils;
import retrofit2.Response;

public class ApiResult<T> {

    public final int code;

    /**
     * Body of the network response
     */
    public final T body;

    public final String errorMessage;

    public ApiResult(Throwable error) {
        code = 500;
        body = null;
        errorMessage = ErrorUtils.getErrorMessage(error);
    }

    public ApiResult(Response<T> response) {
        code = response.code();
        if (response.isSuccessful()) {
            body = response.body();
            errorMessage = null;
        } else {
            String message = null;
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody().string();
                    Log.e("ApiError", response.errorBody().toString());
                } catch (IOException ignored) {
                    Log.e("ApiResult", "error parsing result", ignored);
                }
            }
            if (message == null || message.trim().length() == 0) {
                message = response.message();
            }
            errorMessage = message;
            body = null;
        }
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
}
