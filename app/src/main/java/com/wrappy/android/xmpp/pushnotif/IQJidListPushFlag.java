package com.wrappy.android.xmpp.pushnotif;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dan Chua on 20/03/2019
 */
public class IQJidListPushFlag extends IQJidPushFlags {

    private List<JidPushFlag> mJidPushFlags;

    public IQJidListPushFlag() {
        this(null);
        setType(Type.get);
    }

    public IQJidListPushFlag(List<JidPushFlag> jidPushFlags) {
        super();
        if(jidPushFlags == null) {
            mJidPushFlags = Collections.emptyList();
        } else {
            mJidPushFlags = Collections.unmodifiableList(jidPushFlags);
        }
    }

    public List<JidPushFlag> getJidPushFlags() {
        return mJidPushFlags;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if(mJidPushFlags == null || mJidPushFlags.isEmpty()) {
            xml.rightAngleBracket();
        } else {
            xml.rightAngleBracket();
            for(JidPushFlag jidPushFlag : mJidPushFlags) {
                xml.halfOpenElement("jid");
                xml.attribute("pushflag", jidPushFlag.isPushFlag()? 1 : 0);
                xml.rightAngleBracket();
                xml.append(jidPushFlag.getJid());
                xml.closeElement("jid");
            }
        }
        return xml;
    }

}
