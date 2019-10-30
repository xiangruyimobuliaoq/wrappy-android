package com.wrappy.android.server.account.body.request;


public class ChangePasswordBody {
    String oldPassword;
    String newPassword;

    public ChangePasswordBody(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
