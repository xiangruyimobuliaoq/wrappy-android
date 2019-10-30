package com.wrappy.android.common.utils;


public class ErrorUtils {
    public static final String ERROR_NO_INTERNET = "No internet connection.";
    public static final String ERROR_CLIENT = "A problem has occured. Please try again.";

    public static String getErrorMessage(Throwable error) {
        return ERROR_NO_INTERNET;
    }
}
