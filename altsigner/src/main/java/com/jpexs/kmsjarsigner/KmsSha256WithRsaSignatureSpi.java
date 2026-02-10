package com.jpexs.kmsjarsigner;

import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.Digest;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

public final class KmsSha256WithRsaSignatureSpi extends SignatureSpi {
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private KmsPrivateKey key;

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        throw new InvalidKeyException("Verify not implemented in this provider (JarSigner does not need it).");
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (!(privateKey instanceof KmsPrivateKey)) {
            throw new InvalidKeyException("Expected KmsPrivateKey, got: " + privateKey.getClass());
        }
        this.key = (KmsPrivateKey) privateKey;
        buf.reset();
    }

    @Override protected void engineUpdate(byte b) { buf.write(b); }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) {
        buf.write(b, off, len);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        if (key == null) throw new SignatureException("Not initialized for signing");

        try {
            byte[] data = buf.toByteArray();
            byte[] digestBytes = MessageDigest.getInstance("SHA-256").digest(data);

            CryptoKeyVersionName kv = CryptoKeyVersionName.parse(key.cryptoKeyVersion());
            Digest digest = Digest.newBuilder().setSha256(ByteString.copyFrom(digestBytes)).build();

            AsymmetricSignRequest req = AsymmetricSignRequest.newBuilder()
                    .setName(kv.toString())
                    .setDigest(digest)
                    .build();

            try (KeyManagementServiceClient kms = KeyManagementServiceClient.create()) {
                return kms.asymmetricSign(req).getSignature().toByteArray();
            }
        } catch (Exception e) {
            throw new SignatureException("KMS AsymmetricSign failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        throw new SignatureException("Verify not implemented");
    }

    @Override
    protected void engineSetParameter(String param, Object value) { /* ignored */ }

    @Override
    protected Object engineGetParameter(String param) { return null; }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) {
        // For SHA256withRSA (PKCS#1 v1.5) no params expected
    }

    @Override
    protected AlgorithmParameters engineGetParameters() { return null; }
}