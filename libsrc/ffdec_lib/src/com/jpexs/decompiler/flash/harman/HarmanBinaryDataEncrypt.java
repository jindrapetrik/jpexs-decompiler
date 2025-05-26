/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.harman;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Harman binary data encryption/decryption.
 */
public class HarmanBinaryDataEncrypt {

    private static final String GLOBAL_KEY = "Adobe AIR SDK (c) 2021 HARMAN Internation Industries Incorporated";

    /**
     * Constructor.
     */
    private HarmanBinaryDataEncrypt() {

    }

    /**
     * Encrypts data.
     *
     * @param data Data to encrypt
     * @param key Key
     * @return Encrypted data
     */
    public static byte[] encrypt(byte[] data, String key) {

        byte[] customKey = null;

        if (key != null) {
            customKey = new byte[16];
            int keyLen = key.length();

            for (int i = 0; i < 16; ++i) {
                customKey[i] = (byte) key.charAt(i % keyLen);
            }
        }

        byte[] result;
        try {
            SecureRandom random = new SecureRandom();
            int dataLen = data.length;
            int encryptedDataLen = (dataLen + 0x1F) & ~0x1F;
            int resultLen = 32 + encryptedDataLen;
            byte[] hashBytes = new byte[4];
            byte[] randomBytes1 = new byte[4];
            byte[] randomBytes2 = new byte[4];
            byte[] keyBytes = new byte[16];
            random.nextBytes(hashBytes);
            random.nextBytes(randomBytes1);
            random.nextBytes(randomBytes2);
            random.nextBytes(keyBytes);
            long random1 = unpack(randomBytes1, 0);
            long random2 = unpack(randomBytes2, 0);

            byte[] ivBytes = getIv(hashBytes, random1, random2);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec keySpec = new SecretKeySpec(customKey != null ? customKey : keyBytes, "AES");
            byte[] dataPadded = new byte[(int) encryptedDataLen];
            random.nextBytes(dataPadded);
            System.arraycopy(data, 0, dataPadded, 0, data.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(dataPadded);
            result = new byte[resultLen];
            long hash = unpack(hashBytes, 0);
            long resultLenXorHash = resultLen ^ hash;
            System.arraycopy(pack(resultLenXorHash), 0, result, 0, 4);
            long dataLenXorRandom1 = dataLen ^ random1;
            System.arraycopy(pack(dataLenXorRandom1), 0, result, 4, 4);
            System.arraycopy(randomBytes1, 0, result, 8, 4);
            System.arraycopy(randomBytes2, 0, result, 12, 4);

            addBytes(keyBytes, 0, randomBytes2, 1);
            addBytes(keyBytes, 4, randomBytes2, -1);
            addBytes(keyBytes, 8, randomBytes2, 1);
            addBytes(keyBytes, 12, randomBytes2, -1);

            System.arraycopy(keyBytes, 0, result, 16, 16);
            System.arraycopy(encryptedBytes, 0, result, 32, encryptedDataLen);
        } catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException
                | NoSuchPaddingException ex) {
            result = null;
        }
        return result;
    }

    private static long unpack(byte[] data, int start) {
        return (data[start] & 0xff)
                + ((long) (data[start + 1] & 0xff) << 8)
                + ((long) (data[start + 2] & 0xff) << 16)
                + ((long) (data[start + 3] & 0xff) << 24);
    }

    private static void addBytes(byte[] toChangeBytes, int offset, byte[] data, int multiplier) {
        for (int i = 0; i < 4; i++) {
            toChangeBytes[offset + i] = (byte) (toChangeBytes[offset + i] + multiplier * data[i]);
        }
    }

    private static byte[] getIv(byte[] hashBytes, long random1, long random2) throws UnsupportedEncodingException {
        byte[] ivBytes = new byte[16];
        int offset = 0;

        int offset2;
        for (offset2 = 0; offset2 < 4; offset2++) {
            offset += hashBytes[offset2] & 0xff;
        }

        offset2 = offset % GLOBAL_KEY.length();
        String hashString = GLOBAL_KEY.substring(offset2) + GLOBAL_KEY.substring(0, offset2) + " EncryptSWF " + offset;
        byte[] hashStringBytes = hashString.getBytes("UTF-8");
        long hashCodeString = 0L;

        for (int i = 0; i < hashStringBytes.length; ++i) {
            hashCodeString = 31 * hashCodeString + (hashStringBytes[i] & 0xff);
        }
        System.arraycopy(pack(hashCodeString), 0, ivBytes, 0, 4);
        System.arraycopy(hashBytes, 0, ivBytes, 4, 4);
        long hashSum = unpack(hashBytes, 0) + hashCodeString;
        long sumXorRandom1 = random1 ^ hashSum;
        long sumXorRandom2 = random2 ^ hashSum;
        System.arraycopy(pack(sumXorRandom1), 0, ivBytes, 8, 4);
        System.arraycopy(pack(sumXorRandom2), 0, ivBytes, 12, 4);
        return ivBytes;
    }

    private static byte[] pack(long value) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (value & 0xff);
        ret[1] = (byte) ((value >> 8) & 0xff);
        ret[2] = (byte) ((value >> 16) & 0xff);
        ret[3] = (byte) ((value >> 24) & 0xff);
        return ret;
    }

    /**
     * Decrypts data.
     *
     * @param data Encrypted data
     * @param key
     * @return Decrypted data
     */
    public static byte[] decrypt(byte[] data, String key) {
        if (data.length < 32) {
            return null;
        }

        byte[] customKey = null;

        if (key != null) {
            customKey = new byte[16];
            int keyLen = key.length();

            for (int i = 0; i < 16; ++i) {
                customKey[i] = (byte) key.charAt(i % keyLen);
            }
        }

        long encryptedLen = data.length;
        long encryptedLenXorHash = unpack(data, 0);
        long decryptedLenXorRandom1 = unpack(data, 4);

        long random1 = unpack(data, 8);
        long random2 = unpack(data, 12);
        byte[] random2Bytes = Arrays.copyOfRange(data, 12, 16);
        byte[] keyBytes = Arrays.copyOfRange(data, 16, 32);
        byte[] encryptedBytes = Arrays.copyOfRange(data, 32, data.length);

        long decryptedLen = decryptedLenXorRandom1 ^ random1;
        long hash = encryptedLen ^ encryptedLenXorHash;
        byte[] hashBytes = pack(hash);
        addBytes(keyBytes, 0, random2Bytes, -1);
        addBytes(keyBytes, 4, random2Bytes, 1);
        addBytes(keyBytes, 8, random2Bytes, -1);
        addBytes(keyBytes, 12, random2Bytes, 1);

        try {
            byte[] ivBytes = getIv(hashBytes, random1, random2);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            SecretKeySpec keySpec = new SecretKeySpec(customKey != null ? customKey : keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            byte[] ret = new byte[(int) decryptedLen];
            System.arraycopy(decryptedBytes, 0, ret, 0, (int) decryptedLen);
            return ret;
        } catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException
                | NoSuchPaddingException ex) {
            return null;
        }
    }

    /**
     * Test encryption/decryption.
     *
     * @param args Command line arguments
     * @throws IOException On I/O error
     */
    public static void main(String[] args) throws IOException {
        byte[] data = new byte[]{'A', 'B', 'C'};
        byte[] encrypted = encrypt(data, null);
        byte[] decrypted = decrypt(encrypted, null);
        if (!Arrays.equals(data, decrypted)) {
            throw new RuntimeException("Cannot encrypt/decrypt");
        }
    }
}
