package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.IQ;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dan Chua on 14/03/2019
 */
public class IQJidPushFlags extends IQ {

    public static final String ELEMENT = "query";
    public static final String NAMESPACE = "urn:xmpp:pushflag";

    public IQJidPushFlags() {
        super(ELEMENT, NAMESPACE);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }
}
