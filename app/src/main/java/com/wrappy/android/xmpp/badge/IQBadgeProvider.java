package com.wrappy.android.xmpp.badge;

import com.wrappy.android.server.ServerConstants;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan Chua on 01/04/2019
 */
public class IQBadgeProvider extends IQProvider<IQBadge> {

    @Override
    public IQBadge parse(XmlPullParser parser, int initialDepth) throws Exception {

        List<Badge> badges = new ArrayList<>();

        outerloop:
        while (true) {
            int eventType = parser.next();

            switch (eventType) {

                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("class")) {
                        Badge badge = new Badge();
                        String category = parser.getAttributeValue("", "category");
                        parser.nextTag();
                        int badgeCount = Integer.parseInt(parser.nextText());
                        parser.nextTag();
                        BareJid jid = JidCreate.bareFrom(parser.nextText()).asBareJid();
                        parser.nextTag();
                        String name = parser.nextText();
                        parser.nextTag();
                        String id = parser.nextText();
                        badge.setChatJid(jid);
                        badge.setBadgeCount(badgeCount);
                        badge.setCategory(category);
                        badge.setName(name);
                        badge.setID(id);
                        badges.add(badge);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;

            }
        }
        IQBadge iqBadge = new IQBadge(badges);
        iqBadge.setType(IQ.Type.result);
        return iqBadge;
    }

}
