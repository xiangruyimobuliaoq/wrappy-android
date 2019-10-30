package com.wrappy.android.xmpp.aws;

import com.wrappy.android.xmpp.pushnotif.GroupPushFlag;

import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

/**
 * Created by Dan Chua on 24/04/2019
 */
public class IQAWSCertificateProvider extends IQProvider<IQAWSCertificate> {
    @Override
    public IQAWSCertificate parse(XmlPullParser parser, int initialDepth) throws Exception {
        AWSCertificate awsCertificate = null;
        String googleTranslateKey = "";
        String aesKey = "";
        outerloop : while(true) {
            int eventType = parser.next();

            switch(eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals("certificate")) {
                        awsCertificate = new AWSCertificate(
                                parser.getAttributeValue("", "AWS_S3_ACCESS_KEY"),
                                parser.getAttributeValue("", "AWS_S3_SECRET_ID"),
                                parser.getAttributeValue("", "AWS_S3_BUCKET_NAME"));
                        googleTranslateKey = parser.getAttributeValue("", "GOOGLE_TRANSLATE_KEY");
                        aesKey = parser.getAttributeValue("", "AES_256_KEY");
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;
            }

        }
        return new IQAWSCertificate(awsCertificate, googleTranslateKey, aesKey);
    }
}
