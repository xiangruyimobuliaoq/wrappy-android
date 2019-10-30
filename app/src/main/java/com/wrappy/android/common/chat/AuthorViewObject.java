package com.wrappy.android.common.chat;

import com.stfalcon.chatkit.commons.models.IUser;

public class AuthorViewObject implements IUser {

    private String Id;
    private String Name;
    private String Avatar;

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getAvatar() {
        return Avatar;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }
}
