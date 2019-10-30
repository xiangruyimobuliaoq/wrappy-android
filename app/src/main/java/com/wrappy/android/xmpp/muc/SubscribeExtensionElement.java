package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.pubsub.SubscribeExtension;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.packet.PubSub;

public class SubscribeExtensionElement implements ExtensionElement {

    private static final String NAMESPACE = "urn:xmpp:mucsub:0";

    SubscribeExtension mSubscribeExtension;

    public SubscribeExtensionElement(SubscribeExtension subscribeExtension) {
        mSubscribeExtension = subscribeExtension;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return mSubscribeExtension.getElementName();
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return mSubscribeExtension.toXML(enclosingNamespace);
    }
}
