package com.wrappy.android.xmpp.aws;

import java.io.Serializable;

/**
 * Created by Dan Chua on 24/04/2019
 */
public class AWSCertificate implements Serializable {

    private String mAccessKey;
    private String mSecretID;
    private String mBucketName;

    public AWSCertificate(String accessKey, String secretID, String bucketName) {
        this.mAccessKey = accessKey;
        this.mSecretID = secretID;
        this.mBucketName = bucketName;
    }

    public String getAccessKey() {
        return mAccessKey;
    }

    public String getSecretID() {
        return mSecretID;
    }

    public String getBucketName() {
        return mBucketName;
    }
}
