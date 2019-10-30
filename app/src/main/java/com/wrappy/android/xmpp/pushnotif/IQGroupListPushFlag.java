package com.wrappy.android.xmpp.pushnotif;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class IQGroupListPushFlag extends IQGroupPushFlags {

    private List<GroupPushFlag> mGroupPushFlags;

    public IQGroupListPushFlag() {
        this(null);
        setType(Type.get);
    }

    public IQGroupListPushFlag(List<GroupPushFlag> groupPushFlags) {
        super();
        if(groupPushFlags == null) {
            mGroupPushFlags = Collections.emptyList();
        } else {
            mGroupPushFlags = Collections.unmodifiableList(groupPushFlags);
        }
    }

    public List<GroupPushFlag> getGroupPushFlags() {
        return mGroupPushFlags;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if(mGroupPushFlags == null || mGroupPushFlags.isEmpty()) {
            xml.rightAngleBracket();
        } else {
            xml.rightAngleBracket();
            for(GroupPushFlag groupPushFlag : mGroupPushFlags) {
                xml.halfOpenElement("roomName");
                xml.attribute("roomid", groupPushFlag.getRoomID());
                xml.attribute("pushflag", groupPushFlag.isPushFlag()? 1 : 0);
                xml.rightAngleBracket();
                xml.append(groupPushFlag.getGroupJid());
                xml.closeElement("roomName");
            }
        }
        return xml;
    }

}
