package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

public class MessageExtensionElement implements ExtensionElement {
    private static final String NAMESPACE = "jabber:client";

    private Message mMessage;

    MessageExtensionElement(Message message) {
        mMessage = message;
    }

    public Message getMessage() {
        return mMessage;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return Message.ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return mMessage.toXML(enclosingNamespace);
    }
}
