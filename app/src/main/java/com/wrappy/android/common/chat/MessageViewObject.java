package com.wrappy.android.common.chat;

import android.support.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class MessageViewObject implements IMessage, MessageContentType.Image, MessageContentType {

    private String Id;
    private String Text;
    private int Type;
    private AuthorViewObject User;
    private Date CreatedAt;
    private String Subject;
    private String TranslatedText;

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getText() {
        return Text;
    }

    @Override
    public AuthorViewObject getUser() {
        return User;
    }

    @Override
    public Date getCreatedAt() {
        return CreatedAt;
    }

    public String getSubject() {
        return Subject;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setText(String text) {
        Text = text;
    }

    public void setUser(AuthorViewObject user) {
        User = user;
    }

    public void setCreatedAt(Date createdAt) {
        CreatedAt = createdAt;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return null;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public String getTranslatedText() {
        return TranslatedText;
    }

    public void setTranslatedText(String translatedText) {
        TranslatedText = translatedText;
    }
}
