package com.wrappy.android.xmpp.badge;

import org.jxmpp.jid.Jid;

/**
 * Created by Dan Chua on 01/04/2019
 */
public class Badge {

    private Jid mChatJid;
    private String mCategory;
    private int mBadgeCount;
    private String mName;
    private String mID;

    public Jid getChatJid() {
        return mChatJid;
    }

    public void setChatJid(Jid mChatJid) {
        this.mChatJid = mChatJid;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public int getBadgeCount() {
        return mBadgeCount;
    }

    public void setBadgeCount(int mBadgeCount) {
        this.mBadgeCount = mBadgeCount;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }
}
