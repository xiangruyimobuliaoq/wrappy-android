package com.wrappy.android.xmpp.muc;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.IQResultReplyFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.pubsub.Subscription;

/**
 * Created by vanlesterlim on 06/08/2018.
 */

public class IQMucFilter extends IQResultReplyFilter {

    public IQMucFilter(IQ iqPacket, XMPPConnection conn) {
        super(iqPacket, conn);

    }

}
