package com.wrappy.android.server.account.body.request;


public class SendRecoveryEmailBody {
    public static final String TYPE_PASSWORD = "reset_password";
    public static final String TYPE_PATTERN = "reset_pattern";

    private String email;

    private String type;

    public SendRecoveryEmailBody(String email, String type) {
        this.email = email;
        this.type = type;
    }
}
