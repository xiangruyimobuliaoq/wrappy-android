package com.wrappy.android.common.utils;


import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

public class KeyStoreUtils {
    private static final String KEY_ALIAS = "wrappy_key_pair";
    private static final String CERT_SUBJECT = "CN=Wrappy, O=Wrappy";

    public static void initialize(Context appContext) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            if (!ks.containsAlias(KEY_ALIAS)) {
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = Calendar.getInstance();
                endDate.add(Calendar.YEAR, 30);

                KeyPairGenerator kpGen = KeyPairGenerator.getInstance(
                        "RSA", "AndroidKeyStore");
                if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                    kpGen.initialize(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT |
                                    KeyProperties.PURPOSE_DECRYPT)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setCertificateSubject(new X500Principal(CERT_SUBJECT))
                            .setCertificateNotBefore(startDate.getTime())
                            .setCertificateNotAfter(endDate.getTime())
                            .build());
                } else {
                    kpGen.initialize(new KeyPairGeneratorSpec.Builder(appContext)
                            .setAlias(KEY_ALIAS)
                            .setSubject(new X500Principal(CERT_SUBJECT))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(startDate.getTime())
                            .setEndDate(endDate.getTime())
                            .build());
                }
                kpGen.generateKeyPair();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.ENCRYPT_MODE, ks.getCertificate(KEY_ALIAS).getPublicKey());

            return Base64.encodeToString(c.doFinal(data.getBytes("UTF-8")), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String encryptedData) {
        if (TextUtils.isEmpty(encryptedData)) {
            return "";
        }

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.DECRYPT_MODE, ks.getKey(KEY_ALIAS, null));

            return new String(c.doFinal(Base64.decode(encryptedData,Base64.DEFAULT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
