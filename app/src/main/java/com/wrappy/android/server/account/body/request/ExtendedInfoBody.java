package com.wrappy.android.server.account.body.request;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import com.wrappy.android.common.utils.Base64ImageFile;

public class ExtendedInfoBody {
    @Ignore
    public String id;

    @Ignore
    public String createdDate;

    @Ignore
    public String modifiedDate;

    @ColumnInfo(name = "username")
    public String username;

    @Ignore
    public String password;

    @ColumnInfo(name = "pattern_password")
    public String patternPassword;

    @ColumnInfo(name = "pattern_password_flag")
    public Boolean patternPasswordFlag;

    @ColumnInfo(name = "ejabberd_password")
    public String ejabberdPassword;

    @Ignore
    public String clearPassword;

    @Ignore
    public Integer status;

    @Ignore
    public String accountType;

    @Ignore
    public String role;

    @Ignore
    public String referralCode;

    @Ignore
    public String language;

    @Ignore
    public Base64ImageFile avatar;

    @Ignore
    public Base64ImageFile backgroundImage;

    /**
     * the domain of eJabberd server
     */
    @Ignore
    public String server;

    @Ignore
    public Integer passwordFailedAttempt;

    @Ignore
    public String passwordChangedDate;
}
