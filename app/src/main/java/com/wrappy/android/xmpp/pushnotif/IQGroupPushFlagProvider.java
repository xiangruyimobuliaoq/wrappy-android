package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class IQGroupPushFlagProvider extends IQProvider<IQGroupPushFlags> {
    @Override
    public IQGroupPushFlags parse(XmlPullParser parser, int initialDepth) throws Exception {
        List<GroupPushFlag> groupPushFlags = null;
        boolean pushResult = false;
        boolean isList = false;
        IQGroupPushFlags iqGroupPushFlags;
        outerloop : while(true) {
            int eventType = parser.next();

            switch(eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals("roomName")) {
                        isList = true;
                        if(groupPushFlags==null) {
                            groupPushFlags = new ArrayList<>();
                        }
                        GroupPushFlag groupPushFlag = new GroupPushFlag();
                        groupPushFlag.setRoomID(Integer.parseInt(parser.getAttributeValue("", "roomid")));
                        groupPushFlag.setPushFlag(parser.getAttributeValue("","pushflag").equals("1")? true : false);
                        groupPushFlag.setGroupJid(JidCreate.bareFrom(parser.nextText()));
                        groupPushFlags.add(groupPushFlag);
                    } else if (parser.getName().equals("updateStatus")) {
                        isList = false;
                        pushResult = Boolean.valueOf(parser.nextText());
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;
            }

        }
        if(isList) {
            iqGroupPushFlags = new IQGroupListPushFlag(groupPushFlags);
        } else {
            iqGroupPushFlags = new IQGroupResultPushFlag(pushResult);
        }
        iqGroupPushFlags.setType(IQ.Type.result);
        return iqGroupPushFlags;
    }
}
