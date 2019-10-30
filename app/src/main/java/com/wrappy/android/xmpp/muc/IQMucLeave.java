package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

public class IQMucLeave extends IQ {
    private static final String ELEMENT = "unsubscribe";
    private static final String NAMESPACE = "urn:xmpp:mucsub:0";

    private String mOtherUser;

    /**
     * Create an IQ to unsubscribe from an MUC.
     */
    public IQMucLeave() {
        this(null);
    }

    /**
     * Create an IQ to unsubscribe another user from the MUC.
     * Sender must be a room moderator.
     *
     * @param otherUser
     */
    public IQMucLeave(String otherUser) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        mOtherUser = otherUser;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        // subscribe element
        xml.optAttribute("jid", mOtherUser);
        xml.setEmptyElement();
        return xml;
    }
}
