package com.wrappy.android.xmpp.muc;

import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

public class IQMucGetSubscriptions extends IQ {
    public static final String ELEMENT = "query";
    public static final String NAMESPACE = "im:iq:group";

    private List<RoomMUCExtend> mSubscriptions;

    /**
     * Create an IQ to query subscribed MUCs
     */
    public IQMucGetSubscriptions() {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
    }

    public IQMucGetSubscriptions(List<RoomMUCExtend> subscriptions) {
        super(ELEMENT, NAMESPACE);
        if (subscriptions == null) {
            mSubscriptions = Collections.emptyList();
        } else {
            mSubscriptions = Collections.unmodifiableList(subscriptions);
        }
    }

    public List<RoomMUCExtend> getSubscriptions() {
        return mSubscriptions;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (mSubscriptions == null || mSubscriptions.isEmpty()) {
            xml.setEmptyElement();
        } else {
            xml.rightAngleBracket();

            for (RoomMUCExtend jid : mSubscriptions) {
                xml.halfOpenElement("room");
                //xml.attribute("nickname", jid.getNickname());
                xml.attribute("roomid", jid.getRoomID());
                xml.attribute("id", jid.getRoomJid());
                xml.attribute("naturalName", jid.getRoomName());
                xml.closeEmptyElement();
            }
        }
        return xml;
    }
}
