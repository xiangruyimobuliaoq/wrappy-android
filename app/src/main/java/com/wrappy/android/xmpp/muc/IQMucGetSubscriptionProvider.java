package com.wrappy.android.xmpp.muc;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.ParserUtils;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;


public class IQMucGetSubscriptionProvider extends IQProvider<IQMucGetSubscriptions> {
    @Override
    public IQMucGetSubscriptions parse(XmlPullParser parser, int initialDepth) throws Exception {
        List<RoomMUCExtend> rooms = null;

        outerloop: while (true) {
            int eventType = parser.next();

            switch (eventType) {

                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("room")) {
                        if (rooms == null) {
                            rooms = new ArrayList<>();
                        }
                        RoomMUCExtend room = new RoomMUCExtend();
                        Jid jid = JidCreate.bareFrom(parser.getAttributeValue("","id"));
                        //Jid nickname = JidCreate.bareFrom(parser.getAttributeValue("", "nickname"));
                        //room.setNickname(nickname);
                        String roomID = parser.getAttributeValue("","roomid");
                        String roomName = parser.getAttributeValue("", "naturalName");
                        room.setRoomJid(jid);
                        room.setRoomID(roomID);
                        room.setRoomName(roomName);
                        rooms.add(room);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;

            }
        }

        IQMucGetSubscriptions subscriptions = new IQMucGetSubscriptions(rooms);
        subscriptions.setType(Type.result);
        return subscriptions;
    }
}
