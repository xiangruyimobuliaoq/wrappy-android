package com.wrappy.android.xmpp.muc;

import org.jxmpp.jid.Jid;

import java.util.List;

public class RoomMUCExtend {

    private String roomID;
    private Jid roomJid;
    private Jid nickname;
    private String roomName;
    private String roomStatus;


    public Jid getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(Jid roomJid) {
        this.roomJid = roomJid;
    }

    public Jid getNickname() {
        return nickname;
    }

    public void setNickname(Jid nickname) {
        this.nickname = nickname;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
}
