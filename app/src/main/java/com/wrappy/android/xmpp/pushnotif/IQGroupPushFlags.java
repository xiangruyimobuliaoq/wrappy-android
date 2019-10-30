package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class IQGroupPushFlags extends IQ {

    public static final String ELEMENT = "query";
    public static final String NAMESPACE = "urn:xmpp:groupPushflag";

    public IQGroupPushFlags() {
        super(ELEMENT, NAMESPACE);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }
}
