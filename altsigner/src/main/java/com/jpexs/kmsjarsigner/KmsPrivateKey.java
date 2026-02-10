package com.jpexs.kmsjarsigner;

import java.security.PrivateKey;

public final class KmsPrivateKey implements PrivateKey {
    private final String cryptoKeyVersion; // projects/.../cryptoKeyVersions/1

    public KmsPrivateKey(String cryptoKeyVersion) {
        this.cryptoKeyVersion = cryptoKeyVersion;
    }

    public String cryptoKeyVersion() {
        return cryptoKeyVersion;
    }

    @Override public String getAlgorithm() { return "RSA"; }
    @Override public String getFormat() { return null; }     // not exportable
    @Override public byte[] getEncoded() { return null; }    // not exportable
}