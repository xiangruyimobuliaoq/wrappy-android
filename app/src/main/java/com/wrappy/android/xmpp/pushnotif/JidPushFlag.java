package com.wrappy.android.xmpp.pushnotif;

import org.jxmpp.jid.Jid;

/**
 * Created by Dan Chua on 14/03/2019
 */
public class JidPushFlag {

    private Jid mJid;
    private boolean mPushFlag;


    public Jid getJid() {
        return mJid;
    }

    public void setJid(Jid mJid) {
        this.mJid = mJid;
    }

    public boolean isPushFlag() {
        return mPushFlag;
    }

    public void setPushFlag(boolean mPushFlag) {
        this.mPushFlag = mPushFlag;
    }
}
