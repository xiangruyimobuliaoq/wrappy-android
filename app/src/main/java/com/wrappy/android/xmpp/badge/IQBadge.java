package com.wrappy.android.xmpp.badge;

import org.jivesoftware.smack.packet.IQ;

import java.util.List;

/**
 * Created by Dan Chua on 01/04/2019
 */
public class IQBadge extends IQ {

    public static String ELEMENT = "query";
    public static String NAMESPACE = "urn:xmpp:badge";

    private List<Badge> mBadgeList;

    public IQBadge() {
        this(null);
        setType(Type.get);
    }

    public IQBadge(List<Badge> badgeList) {
        super(ELEMENT, NAMESPACE);
        this.mBadgeList = badgeList;
        setType(Type.result);
    }

    public List<Badge> getBadgeList() {
        return mBadgeList;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if(mBadgeList==null || mBadgeList.isEmpty()) {
            xml.rightAngleBracket();
        } else {
            xml.rightAngleBracket();
            for(Badge badge : mBadgeList) {
                xml.halfOpenElement("class");
                xml.attribute("category", badge.getCategory());
                xml.rightAngleBracket();
                xml.openElement("badge");
                xml.append(String.valueOf(badge.getBadgeCount()));
                xml.closeElement("badge");
                xml.openElement("jid");
                xml.append(badge.getChatJid().toString());
                xml.closeElement("jid");
                xml.openElement("name");
                xml.append(badge.getName());
                xml.closeElement("name");
                xml.openElement("id");
                xml.append(badge.getID());
                xml.closeElement("id");
            }
        }
        return xml;
    }
}
