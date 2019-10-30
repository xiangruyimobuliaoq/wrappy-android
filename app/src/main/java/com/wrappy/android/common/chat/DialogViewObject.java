package com.wrappy.android.common.chat;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.List;

public class DialogViewObject implements IDialog<MessageViewObject> {

    private String Id;
    private String DialogPhoto;
    private String DialogName;
    private List<AuthorViewObject> Users;
    private MessageViewObject LastMessage;
    private int UnreadCount;

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getDialogPhoto() {
        return DialogPhoto;
    }

    @Override
    public String getDialogName() {
        return DialogName;
    }

    @Override
    public List<AuthorViewObject> getUsers() {
        return Users;
    }

    @Override
    public MessageViewObject getLastMessage() {
        return LastMessage;
    }

    @Override
    public void setLastMessage(MessageViewObject message) {
        LastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        return UnreadCount;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setDialogPhoto(String dialogPhoto) {
        DialogPhoto = dialogPhoto;
    }

    public void setDialogName(String dialogName) {
        DialogName = dialogName;
    }

    public void setUsers(List<AuthorViewObject> users) {
        Users = users;
    }

    public void setUnreadCount(int unreadCount) {
        UnreadCount = unreadCount;
    }
}
