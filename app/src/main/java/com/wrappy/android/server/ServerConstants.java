package com.wrappy.android.server;


import android.net.Uri;
import com.wrappy.android.BuildConfig;

public class ServerConstants {
    public static final String CLIENT_ID = "wrappy-internal";
    public static final String CLIENT_SECRET = "d3JhcHB5LWludGVybmFsOjZFMDdFRDEyRUFDODQ4QTk2REJCRjUwMjBEM0ZGNEY2";
    public static final String SYSTEM_ACCOUNT_ID = "admin";
    public static final String SYSTEM_ACCOUNT_PASSWORD = "not4u2know";

    public static final int RESPONSE_CODE_UNAUTHORIZED = 401;

    public static final String ACC_TYPE_SYSTEM = "SYSTEM";
    public static final String ACC_TYPE_WRAPPY = "WRAPPY";

    public static final String CORE_SERVER_URL = BuildConfig.SERVER_URL_CORE_SERVICE;
    public static final String CORE_SERVER_URL_HOST = Uri.parse(CORE_SERVER_URL).getHost();

    public static final String XMPP_SERVER = BuildConfig.SERVER_URL_OPENFIRE;

    public static final String XMPP_DOMAIN = "@ejabberd-test.newsupplytech.com";

    public static final String XMPP_MUC_DOMAIN = BuildConfig.SERVER_URL_OPENFIRE_MUC;

    public enum ServerLocale {
        ENGLISH("en"),
        JAPANESE("jp"),
        CHINESE_SIMPLIFIED("cn"),
        CHINESE_TRADITIONAL("hk");

        public final String code;

        ServerLocale(String code) {
            this.code = code;
        }
    }
}
