package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.pubsub.UnsubscribeExtension;

public class UnsubscribeExtensionElement implements ExtensionElement {

    private static final String NAMESPACE = "urn:xmpp:mucsub:0";

    UnsubscribeExtension mUnsubscribeExtension;

    public UnsubscribeExtensionElement(UnsubscribeExtension unsubscribeExtension) {
        mUnsubscribeExtension = unsubscribeExtension;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return mUnsubscribeExtension.getElementName();
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return mUnsubscribeExtension.toXML(enclosingNamespace);
    }
}
