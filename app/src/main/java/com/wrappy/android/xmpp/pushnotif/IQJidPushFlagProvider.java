package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan Chua on 14/03/2019
 */
public class IQJidPushFlagProvider extends IQProvider<IQJidPushFlags> {
    @Override
    public IQJidPushFlags parse(XmlPullParser parser, int initialDepth) throws Exception {
        List<JidPushFlag> jidPushFlags = null;
        boolean pushResult = false;
        boolean isList = false;
        IQJidPushFlags iqJidPushFlags;
        outerloop : while(true) {
            int eventType = parser.next();

            switch(eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals("jid")) {
                        isList = true;
                        if(jidPushFlags==null) {
                            jidPushFlags = new ArrayList<>();
                        }
                        JidPushFlag jidPushFlag = new JidPushFlag();
                        jidPushFlag.setPushFlag(parser.getAttributeValue("","pushflag").equals("1")? true : false);
                        jidPushFlag.setJid(JidCreate.bareFrom(parser.nextText()));
                        jidPushFlags.add(jidPushFlag);
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
            iqJidPushFlags = new IQJidListPushFlag(jidPushFlags);
        } else {
            iqJidPushFlags = new IQJidResultPushFlag(pushResult);
        }
        iqJidPushFlags.setType(IQ.Type.result);
        return iqJidPushFlags;
    }
}
