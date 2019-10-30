package com.wrappy.android.xmpp.pushnotif;

/**
 * Created by Dan Chua on 22/03/2019
 */
public class IQGroupResultPushFlag extends IQGroupPushFlags {

    private boolean mResult;

    public IQGroupResultPushFlag(boolean result) {
        super();
        this.mResult = result;
    }

    public boolean getStatusResult() {
        return mResult;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.openElement("updateStatus");
        xml.append(String.valueOf(mResult));
        xml.closeElement("updateStatus");
        return xml;
    }

}
