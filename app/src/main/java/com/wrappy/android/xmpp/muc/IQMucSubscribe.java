package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

/**
 * IQ class for subscribing to an MUC as specified by eJabberd MUC/Sub proposed extension
 *
 * MUC/Sub
 * https://docs.ejabberd.im/developer/xmpp-clients-bots/proposed-extensions/muc-sub/
 */
public class IQMucSubscribe extends IQ {
    private static final String ELEMENT = "subscribe";
    private static final String NAMESPACE = "urn:xmpp:mucsub:0";

    private String mNickname;
    private String mOtherUser;

    /**
     * Create an IQ to subscribe to an MUC.
     *
     * @param nick
     */
    public IQMucSubscribe(String nick) {
        this(null, nick);
    }

    /**
     * Create an IQ to subscribe another user to the MUC.
     * Sender must be a room moderator.
     *
     * @param otherUser
     * @param otherNick
     */
    public IQMucSubscribe(String otherUser, String otherNick) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        mOtherUser = otherUser;
        mNickname = otherNick;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        // subscribe element
        xml.optAttribute("jid", mOtherUser);
        xml.optAttribute("nick", mNickname);
        xml.rightAngleBracket();

        /*  urn:xmpp:mucsub:nodes:presence
            urn:xmpp:mucsub:nodes:messages
            urn:xmpp:mucsub:nodes:affiliations
            urn:xmpp:mucsub:nodes:subscribers
            urn:xmpp:mucsub:nodes:config
            urn:xmpp:mucsub:nodes:subject
            urn:xmpp:mucsub:nodes:system */

        addEventNodeElement(xml, "urn:xmpp:mucsub:nodes:messages");
        addEventNodeElement(xml, "urn:xmpp:mucsub:nodes:subscribers");
        return xml;
    }

    private void addEventNodeElement(IQChildElementXmlStringBuilder xml, String value) {
        xml.halfOpenElement("event");
        xml.attribute("node", value);
        xml.closeEmptyElement();
    }
}
