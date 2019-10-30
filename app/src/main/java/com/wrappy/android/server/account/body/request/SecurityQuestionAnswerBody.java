package com.wrappy.android.server.account.body.request;


import android.os.Parcel;
import android.os.Parcelable;

public class SecurityQuestionAnswerBody implements Parcelable {
    public String id;
    public String createdDate;
    public String modifiedDate;
    public String accountId;
    public String language;
    public String code;
    public String question;
    public String answer;

    public SecurityQuestionAnswerBody() {
        // empty constructor
    }

    public SecurityQuestionAnswerBody(String code, String answer) {
        this(code, null, answer);
    }

    public SecurityQuestionAnswerBody(String code, String question, String answer) {
        this.code = code;
        this.question = question;
        this.answer = answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityQuestionAnswerBody) {
            SecurityQuestionAnswerBody other = (SecurityQuestionAnswerBody) obj;
            return code.regionMatches(0, other.code, 0, 4);
        }
        return super.equals(obj);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable implementation
    public SecurityQuestionAnswerBody(Parcel in) {
        id = in.readString();
        createdDate = in.readString();
        modifiedDate = in.readString();
        accountId = in.readString();
        language = in.readString();
        code = in.readString();
        question = in.readString();
        answer = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(createdDate);
        dest.writeString(modifiedDate);
        dest.writeString(accountId);
        dest.writeString(language);
        dest.writeString(code);
        dest.writeString(question);
        dest.writeString(answer);
    }

    public static final Parcelable.Creator<SecurityQuestionAnswerBody> CREATOR
            = new Parcelable.Creator<SecurityQuestionAnswerBody>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public SecurityQuestionAnswerBody createFromParcel(Parcel in) {
            return new SecurityQuestionAnswerBody(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public SecurityQuestionAnswerBody[] newArray(int size) {
            return new SecurityQuestionAnswerBody[size];
        }
    };
}
