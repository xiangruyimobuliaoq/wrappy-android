package com.wrappy.android.server.account.body.request;


import android.util.Log;
import com.wrappy.android.server.ServerConstants.ServerLocale;
import com.wrappy.android.server.account.body.request.data.HelperData;

import java.util.Locale;

public class AccountHelperBody<T extends HelperData> {
    public final String type;
    public final T data;
    private String language;

    public AccountHelperBody(T data) {
        type = data.getType().toString();
        this.data = data.hasContents() ? data : null;

        Locale locale = Locale.getDefault();
        if (locale.getLanguage().equals("zh")) {
            if (locale.getCountry().equals("CN")) {
                setLanguage(ServerLocale.CHINESE_SIMPLIFIED);
            } else {
                setLanguage(ServerLocale.CHINESE_TRADITIONAL);
            }
        } else if (locale.getLanguage().equals("ja")) {
            setLanguage(ServerLocale.JAPANESE);
        } else {
            setLanguage(ServerLocale.ENGLISH);
        }
    }

    /**
     * set language，supports values: en, cn, hk, ja
     */
    public void setLanguage(ServerLocale language) {
        this.language = language.code;
    }
}
