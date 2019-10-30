package com.wrappy.android.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.wrappy.android.common.Resource.Status.*;

/**
 * Taken from
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample
 *
 * A generic class that holds a value with its loading status.
 * Often used with LiveData.
 *
 * @param <T>
 */
public class Resource<T> {

    public enum Status {
        SUCCESS,
        LOADING,
        CLIENT_ERROR,
        SERVER_ERROR
    }

    @NonNull
    public final Status status;

    @Nullable
    public final String message;

    @Nullable
    public final T data;

    public Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> serverError(String msg, @Nullable T data) {
        return new Resource<>(SERVER_ERROR, data, msg);
    }

    public static <T> Resource<T> clientError(String msg, @Nullable T data) {
        return new Resource<>(CLIENT_ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource<?> resource = (Resource<?>) o;

        if (status != resource.status) {
            return false;
        }
        if (message != null ? !message.equals(resource.message) : resource.message != null) {
            return false;
        }
        return data != null ? data.equals(resource.data) : resource.data == null;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
