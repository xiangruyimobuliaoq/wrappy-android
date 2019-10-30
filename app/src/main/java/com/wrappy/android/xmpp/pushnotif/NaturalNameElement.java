package com.wrappy.android.xmpp.pushnotif;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * Created by Dan Chua on 01/04/2019
 */
public class NaturalNameElement implements ExtensionElement {

    public static final String ELEMENT = "naturalName";

    private String mNaturalName;

    public NaturalNameElement(String naturalName) {
        this.mNaturalName = naturalName;
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
        xml.append(mNaturalName);
        xml.closeElement(ELEMENT);
        return xml;
    }
}
