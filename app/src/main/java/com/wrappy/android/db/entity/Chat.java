package com.wrappy.android.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "chat_table")
public class Chat implements Parcelable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "chat_id")
    private String chatId;

    @NonNull
    @ColumnInfo(name = "chat_name")
    private String chatName;

    @NonNull
    @ColumnInfo(name = "chat_whois")
    private String chatWhoIs;

    @NonNull
    @ColumnInfo(name = "chat_type")
    private String chatType;

    @ColumnInfo(name = "chat_avatar")
    private String chatAvatar;

    @ColumnInfo(name = "chat_last_message")
    private String chatLastMessage;

    @ColumnInfo(name = "chat_last_created")
    private Date chatLastDate;

    @Ignore
    private List<Contact> chatParticipants;

    @NonNull
    @ColumnInfo(name = "chat_unread_count")
    private int chatUnreadCount;

    @NonNull
    @ColumnInfo(name = "chat_notification")
    private boolean chatNotification;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Chat &&
                ((Chat) obj).getChatId().equalsIgnoreCase(chatId));
    }

    public Chat() {
        // EMPTY
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(@NonNull String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(@NonNull String chatName) {
        this.chatName = chatName;
    }

    public String getChatAvatar() {
        return chatAvatar;
    }

    public void setChatAvatar(String chatAvatar) {
        this.chatAvatar = chatAvatar;
    }

    public String getChatLastMessage() {
        return chatLastMessage;
    }

    public void setChatLastMessage(String chatLastMessage) {
        this.chatLastMessage = chatLastMessage;
    }

    public List<Contact> getChatParticipants() {
        return chatParticipants;
    }

    public void setChatParticipants(@NonNull List<Contact> chatParticipants) {
        this.chatParticipants = chatParticipants;
    }

    public int getChatUnreadCount() {
        return chatUnreadCount;
    }

    public void setChatUnreadCount(int chatUnreadCount) {
        this.chatUnreadCount = chatUnreadCount;
    }

    @NonNull
    public String getChatType() {
        return chatType;
    }

    public void setChatType(@NonNull String chatType) {
        this.chatType = chatType;
    }

    @NonNull
    public boolean isChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(@NonNull boolean chatNotification) {
        this.chatNotification = chatNotification;
    }

    public Date getChatLastDate() {
        return chatLastDate;
    }

    public void setChatLastDate(Date chatLastDate) {
        this.chatLastDate = chatLastDate;
    }

    public boolean isEqualToContact(Contact contact) {
        return contact.getContactId().equalsIgnoreCase(chatId);
    }

    @NonNull
    public String getChatWhoIs() {
        return chatWhoIs;
    }

    public void setChatWhoIs(@NonNull String chatWhoIs) {
        this.chatWhoIs = chatWhoIs;
    }

    // PARCELABLE implementation

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(chatUnreadCount);
        dest.writeInt(chatNotification ? 1 : 0);
        dest.writeLong(chatLastDate.getTime());
        dest.writeString(chatId);
        dest.writeString(chatName);
        dest.writeString(chatWhoIs);
        dest.writeString(chatType);
        dest.writeString(chatAvatar);
        dest.writeString(chatLastMessage);
        dest.writeSerializable((ArrayList<Contact>) chatParticipants);
    }

    public static final Parcelable.Creator<Chat> CREATOR
            = new Parcelable.Creator<Chat>() {
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    private Chat(Parcel in) {
        chatUnreadCount = in.readInt();
        chatNotification = in.readInt() == 1;
        chatLastDate = new Date(in.readLong());
        chatId = in.readString();
        chatName = in.readString();
        chatWhoIs = in.readString();
        chatType = in.readString();
        chatAvatar = in.readString();
        chatLastMessage = in.readString();
        chatParticipants = (List<Contact>) in.readSerializable();
    }
}
