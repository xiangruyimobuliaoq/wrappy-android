package com.wrappy.android.xmpp.pushnotif;

import org.jxmpp.jid.Jid;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class IQGroupSetPushFlag extends IQGroupPushFlags {

    private Jid mGroupJid;
    private int mPushFlag;

    public IQGroupSetPushFlag(Jid groupJid, int pushFlag) {
        this.mGroupJid = groupJid;
        this.mPushFlag = pushFlag;
        setType(Type.set);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.openElement("pushflag");
        xml.append(String.valueOf(mPushFlag));
        xml.closeElement("pushflag");
        xml.openElement("roomName");
        xml.append(mGroupJid.toString());
        xml.closeElement("roomName");
        return xml;
    }
}
