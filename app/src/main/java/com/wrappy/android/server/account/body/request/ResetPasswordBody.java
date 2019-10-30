package com.wrappy.android.server.account.body.request;


public class ResetPasswordBody {
    /**
     * secret key get form such as validate security question
     */
    String secretKey;

    /**
     * new password
     */
    String newPassword;

    /**
     * new pattern
     */
    String newPatternPassword;

    public ResetPasswordBody(String secretKey, String newPassword, String newPatternPassword) {
        this.secretKey = secretKey;
        this.newPassword = newPassword;
        this.newPatternPassword = newPatternPassword;
    }
}
