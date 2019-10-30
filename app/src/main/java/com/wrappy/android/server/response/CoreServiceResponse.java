package com.wrappy.android.server.response;

import com.google.gson.annotations.SerializedName;

public class CoreServiceResponse<T> {
    private int code;

    private String message;

    @SerializedName("request_timestamp")
    private String requestTimestamp;

    @SerializedName("response_timestamp")
    private String responseTimestamp;

    private T data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public T getData() {
        return data;
    }

    /**
     * Determines if this response from the core service is a success
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return code > 0;
    }
}
