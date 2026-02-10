package com.jpexs.kmsjarsigner;

import java.security.Provider;

public final class KmsProvider extends Provider {
    public static final String NAME = "KMS";

    public KmsProvider() {
        super(NAME, 1.0, "Google Cloud KMS Signature Provider");
        // We implement exactly what you need:
        // RSA 3072 + PKCS#1 v1.5 + SHA-256 = "SHA256withRSA"
        put("Signature.SHA256withRSA", KmsSha256WithRsaSignatureSpi.class.getName());
    }
}