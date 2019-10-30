package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "contact_table")
public class Contact implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "contact_id")
    private String contactId;

    @NonNull
    @ColumnInfo(name = "contact_presence")
    private String contactPresence;

    @NonNull
    @ColumnInfo(name = "contact_name")
    private String contactName;

    @ColumnInfo(name = "contact_avatar")
    private String contactAvatar;

    @ColumnInfo(name = "contact_banner")
    private String contactBanner;

    @NonNull
    @ColumnInfo(name = "contact_Type")
    private int contactType;

    @NonNull
    @ColumnInfo(name = "contact_isBlocked")
    private boolean contactIsBlocked;

    public Contact(@NonNull String contactId, @NonNull String contactPresence, @NonNull String contactName, @NonNull int contactType) {
        this.contactId = contactId;
        this.contactPresence = contactPresence;
        this.contactName = contactName;
        this.contactType = contactType;

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) ||
                (obj instanceof Contact && ((Contact) obj).getContactId().equalsIgnoreCase(contactId));
    }

    @NonNull
    public String getContactId() {
        return contactId;
    }

    public void setContactId(@NonNull String contactId) {
        this.contactId = contactId;
    }

    @NonNull
    public String getContactPresence() {
        return contactPresence;
    }

    public void setContactPresence(@NonNull String contactPresence) {
        this.contactPresence = contactPresence;
    }

    @NonNull
    public String getContactName() {
        return contactName;
    }

    public void setContactName(@NonNull String contactName) {
        this.contactName = contactName;
    }

    public String getContactAvatar() {
        return contactAvatar;
    }

    public void setContactAvatar(String contactAvatar) {
        this.contactAvatar = contactAvatar;
    }

    public String getContactBanner() {
        return contactBanner;
    }

    public void setContactBanner(String contactBanner) {
        this.contactBanner = contactBanner;
    }

    @NonNull
    public int getContactType() {
        return contactType;
    }

    public void setContactType(@NonNull int contactType) {
        this.contactType = contactType;
    }

    @NonNull
    public boolean isContactIsBlocked() {
        return contactIsBlocked;
    }

    public void setContactIsBlocked(@NonNull boolean contactIsBlocked) {
        this.contactIsBlocked = contactIsBlocked;
    }

    public boolean isEqualToChat(Chat chat) {
        return chat.getChatId().equalsIgnoreCase(contactId);
    }

    public boolean queryContact(String userId) {
        return contactName.contains(userId);
    }

}
