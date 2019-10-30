package com.wrappy.android.xmpp.aws;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by Dan Chua on 24/04/2019
 */
public class IQAWSCertificate extends IQ {

    public static final String ELEMENT_NAME = "certificate";
    public static final String NAMESPACE = "iq:certificate:query";

    private AWSCertificate mAWSCertificate;
    private String mGoogleTranslateKey;
    private String mAESKey;

    public IQAWSCertificate() {
        super(ELEMENT_NAME, NAMESPACE);
        this.setType(Type.get);
    }

    public IQAWSCertificate(AWSCertificate awsCertificate, String googleTranslateKey, String aesKey) {
        super(ELEMENT_NAME, NAMESPACE);
        this.mAWSCertificate = awsCertificate;
        this.mGoogleTranslateKey = googleTranslateKey;
        this.mAESKey = aesKey;
        this.setType(Type.result);
    }

    public AWSCertificate getAWSCertificate() {
        return mAWSCertificate;
    }

    public String getGoogleTranslateKey() {
        return mGoogleTranslateKey;
    }

    public String getAESKey() {
        return mAESKey;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if(mAWSCertificate != null) {
            xml.rightAngleBracket();
            xml.halfOpenElement("certificate");
            xml.attribute("AWS_S3_ACCESS_KEY", mAWSCertificate.getAccessKey());
            xml.attribute("AWS_S3_SECRET_ID", mAWSCertificate.getSecretID());
            xml.attribute("AWS_S3_BUCKET_NAME", mAWSCertificate.getBucketName());
            xml.attribute("GOOGLE_TRANSLATE_KEY", mGoogleTranslateKey);
            xml.attribute("AES_256_KEY", mAESKey);
            xml.rightAngleBracket();
            xml.closeElement("certificate");
        } else {
            xml.rightAngleBracket();
        }
        return xml;
    }
}
