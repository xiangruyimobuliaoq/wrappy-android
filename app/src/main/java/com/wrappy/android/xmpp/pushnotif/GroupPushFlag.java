package com.wrappy.android.xmpp.pushnotif;

import org.jxmpp.jid.Jid;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class GroupPushFlag {

    private Jid mGroupJid;
    private int mRoomID;
    private boolean mPushFlag;


    public Jid getGroupJid() {
        return mGroupJid;
    }

    public void setGroupJid(Jid mGroupJid) {
        this.mGroupJid = mGroupJid;
    }

    public int getRoomID() {
        return mRoomID;
    }

    public void setRoomID(int mRoomID) {
        this.mRoomID = mRoomID;
    }

    public boolean isPushFlag() {
        return mPushFlag;
    }

    public void setPushFlag(boolean mPushFlag) {
        this.mPushFlag = mPushFlag;
    }
}
