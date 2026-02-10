package com.jpexs.kmsjarsigner;

import jdk.security.jarsigner.JarSigner;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.zip.ZipFile;

public final class SignJar {

    // Usage:
    //   java -jar target/kms-jar-signer-1.0.0.jar input.jar output.jar chain.pem KMS_KEY_VERSION TSA_URL
    //
    // chain.pem: PEM with leaf + intermediate(s). Root optional (usually omit).
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: SignJar <in.jar> <out.jar> <cert-chain.pem> <kmsKeyVersion> <tsaUrl>");
            System.exit(2);
        }
        Path inJar = Path.of(args[0]);
        Path outJar = Path.of(args[1]);
        Path chainPath = Path.of(args[2]);
        String kmsKeyVersion = args[3];
        URI tsa = URI.create(args[4]);

        Provider kmsProvider = new KmsProvider();
        Security.addProvider(kmsProvider);

        PrivateKey kmsKey = new KmsPrivateKey(kmsKeyVersion);
        CertPath certPath = loadPemCertPath(chainPath);

        JarSigner signer = new JarSigner.Builder(kmsKey, certPath)
                .digestAlgorithm("SHA-256")
                .signatureAlgorithm("SHA256withRSA", kmsProvider)
                .tsa(tsa) // timestamp via RFC3161 TSA
                .build();

        try (ZipFile zip = new ZipFile(inJar.toFile());
             OutputStream os = Files.newOutputStream(outJar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            signer.sign(zip, os);
        }

        System.out.println("Signed: " + outJar);
    }

    private static CertPath loadPemCertPath(Path pemPath) throws IOException, CertificateException {
        byte[] pem = Files.readAllBytes(pemPath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends java.security.cert.Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(pem));
        if (certs.isEmpty()) throw new CertificateException("No certificates found in " + pemPath);

        List<java.security.cert.Certificate> ordered = new ArrayList<>(certs);
        // JarSigner expects a CertPath; order typically leaf->intermediate... works fine.
        return cf.generateCertPath(ordered);
    }
}