package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class PresenceExtensionElement implements ExtensionElement {

    private static final String NAMESPACE = "jabber:client";

    private Presence mPresence;

    PresenceExtensionElement(Presence presence) {
        mPresence = presence;
    }

    public Presence getPresence() {
        return mPresence;
    }


    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return mPresence.ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return mPresence.toXML(enclosingNamespace);
    }
}
