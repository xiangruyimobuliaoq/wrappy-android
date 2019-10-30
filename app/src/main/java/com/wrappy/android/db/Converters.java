package com.wrappy.android.db;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class Converters {

    @Inject
    Gson gson;

    public Converters() {
        WrappyApp.getInstance().getInjector().inject(this);
    }

    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public List<SecurityQuestionAnswerBody> stringToSecurityQuestionList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<SecurityQuestionAnswerBody>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public String securityQuestionListToString(List<SecurityQuestionAnswerBody> someObjects) {
        return gson.toJson(someObjects);
    }
}
