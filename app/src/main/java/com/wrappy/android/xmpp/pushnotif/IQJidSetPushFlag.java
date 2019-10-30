package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

/**
 * Created by Dan Chua on 20/03/2019
 */
public class IQJidSetPushFlag extends IQJidPushFlags {

    private int mPushFlag;

    public IQJidSetPushFlag(int pushFlag, Jid to) {
        super();
        mPushFlag = pushFlag;
        setType(Type.set);
        setTo(to);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.openElement("pushflag");
        xml.append(String.valueOf(mPushFlag));
        xml.closeElement("pushflag");
        return xml;
    }

    public int getPushFlag() {
        return mPushFlag;
    }
}
