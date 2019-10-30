package com.wrappy.android.server.account.body.response;


public class AccountStatusResponse {
    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_NOT_EXIST = "NOT_EXIST";
    public static final String STATUS_LOCKED = "LOCKED";

    public String username;
    public String status;
    public int count;
    public long lockLeftSeconds;
}
