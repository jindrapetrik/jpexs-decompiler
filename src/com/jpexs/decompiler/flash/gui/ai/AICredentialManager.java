/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.jpexs.decompiler.flash.configuration.AppDirectoryProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class AICredentialManager {

    private static final String STORE_NAME = "ai-credentials.jceks";
    private static KeyStore keyStore;
    private static char[] storePassword;
    private static File storeFile;

    private AICredentialManager() {
    }

    public static synchronized boolean isUnlocked() {
        return keyStore != null;
    }

    public static synchronized boolean hasStore() {
        return new File(AppDirectoryProvider.getFFDecHome(), STORE_NAME).isFile();
    }

    public static synchronized void unlock(char[] password) throws IOException {
        if (password == null || password.length == 0) {
            throw new IOException("Enter the credential-store password.");
        }
        try {
            KeyStore loadedStore = KeyStore.getInstance("JCEKS");
            File file = new File(AppDirectoryProvider.getFFDecHome(), STORE_NAME);
            if (file.isFile()) {
                try (FileInputStream input = new FileInputStream(file)) {
                    loadedStore.load(input, password);
                }
            } else {
                if (password.length < 12) {
                    throw new IOException("Use a credential-store password with at least 12 characters.");
                }
                loadedStore.load(null, password);
            }
            keyStore = loadedStore;
            storeFile = file;
            clearPassword();
            storePassword = password.clone();
        } catch (GeneralSecurityException ex) {
            throw new IOException("Cannot unlock the encrypted credential store.", ex);
        }
    }

    public static synchronized String getCredential(String providerId) throws IOException {
        ensureUnlocked();
        try {
            KeyStore.Entry entry = keyStore.getEntry(providerId,
                    new KeyStore.PasswordProtection(storePassword));
            if (!(entry instanceof KeyStore.SecretKeyEntry)) {
                return "";
            }
            byte[] encoded = ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded();
            try {
                return new String(encoded, StandardCharsets.UTF_8);
            } finally {
                java.util.Arrays.fill(encoded, (byte) 0);
            }
        } catch (GeneralSecurityException ex) {
            throw new IOException("Cannot read the encrypted API credential.", ex);
        }
    }

    public static synchronized void saveCredential(String providerId, char[] credential)
            throws IOException {
        ensureUnlocked();
        if (credential == null || credential.length == 0) {
            throw new IOException("Enter an API key before saving.");
        }
        byte[] bytes = new String(credential).getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(bytes, "AES");
        try {
            keyStore.setEntry(providerId, new KeyStore.SecretKeyEntry(key),
                    new KeyStore.PasswordProtection(storePassword));
            saveStore();
        } catch (GeneralSecurityException ex) {
            throw new IOException("Cannot encrypt the API credential.", ex);
        } finally {
            java.util.Arrays.fill(bytes, (byte) 0);
        }
    }

    public static synchronized void removeCredential(String providerId) throws IOException {
        ensureUnlocked();
        try {
            if (keyStore.containsAlias(providerId)) {
                keyStore.deleteEntry(providerId);
                saveStore();
            }
        } catch (GeneralSecurityException ex) {
            throw new IOException("Cannot remove the API credential.", ex);
        }
    }

    private static void ensureUnlocked() throws IOException {
        if (keyStore == null) {
            throw new IOException("Unlock the encrypted credential store in AI Settings first.");
        }
    }

    private static void saveStore() throws IOException, GeneralSecurityException {
        File parent = storeFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Cannot create the AI credential-store directory.");
        }
        Path target = storeFile.toPath();
        Path temporary = Files.createTempFile(parent.toPath(), "ai-credentials-", ".tmp");
        try {
            try (FileOutputStream output = new FileOutputStream(temporary.toFile())) {
                keyStore.store(output, storePassword);
            }
            setOwnerOnlyPermissions(temporary);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            setOwnerOnlyPermissions(target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void setOwnerOnlyPermissions(Path file) throws IOException {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        try {
            Files.setPosixFilePermissions(file, permissions);
        } catch (UnsupportedOperationException ex) {
            // Windows protects files through the user's profile ACL instead.
        }
    }

    private static void clearPassword() {
        if (storePassword != null) {
            java.util.Arrays.fill(storePassword, '\0');
        }
        storePassword = null;
    }
}
