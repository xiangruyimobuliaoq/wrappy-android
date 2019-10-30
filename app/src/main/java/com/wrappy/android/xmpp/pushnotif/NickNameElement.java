package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * Created by Dan Chua on 01/04/2019
 */
public class NickNameElement implements ExtensionElement {

    public static final String ELEMENT = "nickName";

    private String mNickName;

    public NickNameElement(String nickName) {
        this.mNickName = nickName;
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();
        xml.append(mNickName);
        xml.closeElement(ELEMENT);
        return xml;
    }
}
