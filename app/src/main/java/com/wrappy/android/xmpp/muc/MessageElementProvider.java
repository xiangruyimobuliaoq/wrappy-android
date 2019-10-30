package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

public class MessageElementProvider extends ExtensionElementProvider<MessageExtensionElement> {

    @Override
    public MessageExtensionElement parse(XmlPullParser parser, int initialDepth) throws Exception {
        Message message = (Message) PacketParserUtils.parseStanza(parser);
        return new MessageExtensionElement(message);
    }
}
