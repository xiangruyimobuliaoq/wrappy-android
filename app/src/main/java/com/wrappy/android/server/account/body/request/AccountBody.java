package com.wrappy.android.server.account.body.request;


import java.util.ArrayList;
import java.util.List;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

public class AccountBody {
    @Ignore
    public String id;

    @Ignore
    public String createdDate;

    @Ignore
    public String modifiedDate;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @Ignore
    public String middleName;

    @Ignore
    public String lastName;

    @ColumnInfo(name = "nick_name")
    public String nickName;

    @Ignore
    public String dateOfBirth;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "mobile_phone")
    public String mobilePhone;

    @Ignore
    public String homePhone;

    @Ignore
    public String address1;

    @Ignore
    public String address2;

    @Ignore
    public String city;

    @Ignore
    public String state;

    @Ignore
    public String country;

    @Ignore
    public String zip;

    @Embedded(prefix = "ext_")
    public ExtendedInfoBody extendedInfo;

    @ColumnInfo(name = "security_questions")
    public List<SecurityQuestionAnswerBody> securityQuestions;

    public AccountBody() {
        // empty constructor
    }

    public AccountBody(String nickName,
                       String email,
                       String server,
                       List<SecurityQuestionAnswerBody> securityQuestions) {
        this.nickName = nickName;
        this.email = email;
        this.extendedInfo.server = server;

        this.securityQuestions = new ArrayList<>();
        this.securityQuestions.addAll(securityQuestions);
    }
}
