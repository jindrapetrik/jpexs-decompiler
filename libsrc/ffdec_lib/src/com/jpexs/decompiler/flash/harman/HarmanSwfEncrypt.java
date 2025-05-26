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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * Harman SWF encryption/decryption.
 *
 * @author JPEXS
 * <p>
 * Based on swfdecrypt.py by NathaanTFM
 */
public class HarmanSwfEncrypt {

    private static final String GLOBAL_KEY = "Adobe AIR SDK (c) 2021 HARMAN Internation Industries Incorporated";

    /**
     * Constructor.
     */
    private HarmanSwfEncrypt() {

    }

    private static int sum(byte[] data) {
        int s = 0;
        for (int i = 0; i < data.length; i++) {
            s += data[i] & 0xff;
        }
        return s;
    }

    private static long getkey(byte[] data) {
        int dsum = sum(data);
        int dmod = dsum % GLOBAL_KEY.length();
        String s = GLOBAL_KEY.substring(dmod) + GLOBAL_KEY.substring(0, dmod);
        s += " EncryptSWF ";
        s += "" + dsum;

        long ret = 0;
        for (int i = 0; i < s.length(); i++) {
            int code = s.charAt(i);
            ret *= 31;
            ret += code;
        }

        return ret & 0xffffffffL;
    }

    private static long unpack(byte[] data, int start) {
        return (data[start] & 0xff)
                + ((long) (data[start + 1] & 0xff) << 8)
                + ((long) (data[start + 2] & 0xff) << 16)
                + ((long) (data[start + 3] & 0xff) << 24);
    }

    private static byte[] pack(long value) {
        return new byte[]{
            (byte) (value & 0xff),
            (byte) ((value >> 8) & 0xff),
            (byte) ((value >> 16) & 0xff),
            (byte) ((value >> 24) & 0xff)
        };
    }

    private static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // velikost bufferu, můžeš ji upravit podle potřeby

        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    /**
     * Encrypts data.
     *
     * @param data Data to encrypt
     * @return Encrypted data
     * @throws IOException On I/O error
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws NoSuchPaddingException On invalid padding
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] encrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(new ByteArrayInputStream(data));
    }

    /**
     * Encrypts data.
     *
     * @param is Data to encrypt
     * @return Encrypted data
     * @throws IOException On I/O error
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws NoSuchPaddingException On invalid padding
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] encrypt(InputStream is) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] header = new byte[8];
        DataInputStream dais = new DataInputStream(is);
        dais.readFully(header);
        return encrypt(is, header);
    }

    /**
     * Encrypts data.
     *
     * @param is Data to encrypt
     * @param header Header
     * @return Encrypted data
     * @throws IOException On I/O error
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws NoSuchPaddingException On invalid padding
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] encrypt(InputStream is, byte[] header) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long key = getkey(header);

        byte[] data = readStream(is);

        int decryptedLength = data.length;
        long encryptedLength = decryptedLength ^ key;
        byte[] encryptedLengthBytes = pack(encryptedLength);

        int paddedLength = (int) (decryptedLength + 0x1F) & ~0x1F;

        byte[] dataPadded = new byte[paddedLength];
        System.arraycopy(data, 0, dataPadded, 0, data.length);

        byte[] aesIV = new byte[16];
        System.arraycopy(header, 0, aesIV, 0, header.length); //header
        System.arraycopy(encryptedLengthBytes, 0, aesIV, 8, 4); //encrypted length
        aesIV[12] = (byte) (key & 0xff);
        aesIV[13] = (byte) ((key >> 8) & 0xff);
        aesIV[14] = (byte) ((key >> 16) & 0xff);
        aesIV[15] = (byte) ((key >> 24) & 0xff);

        for (int i = 0; i < 16; i++) {
            aesIV[i] ^= GLOBAL_KEY.charAt(i);
        }

        byte[] aesKey = new byte[32];

        SecureRandom random = new SecureRandom();
        random.nextBytes(aesKey);

        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(aesIV));
        byte[] encryptedData = cipher.doFinal(dataPadded);
        header[0] += 32; //to lowercase

        baos.write(header);
        baos.write(pack(encryptedLength));
        baos.write(encryptedData, 0, paddedLength);

        byte[] aesKeyData = new byte[32];
        for (int i = 0; i < 32; i += 4) {
            long value = unpack(aesKey, i);
            if ((i & 4) == 4) {
                value += key;
            } else {
                value -= key;
            }
            aesKeyData[i] = (byte) (value & 0xff);
            aesKeyData[i + 1] = (byte) ((value >> 8) & 0xff);
            aesKeyData[i + 2] = (byte) ((value >> 16) & 0xff);
            aesKeyData[i + 3] = (byte) ((value >> 24) & 0xff);
        }

        baos.write(aesKeyData);

        return baos.toByteArray();
    }

    /**
     * Decrypts data.
     *
     * @param data Data to decrypt
     * @return Decrypted data
     * @throws IOException On I/O error
     * @throws NoSuchPaddingException On invalid padding
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] decrypt(byte[] data) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(new ByteArrayInputStream(data));
    }

    /**
     * Decrypts data.
     *
     * @param is Data to decrypt
     * @return Decrypted data
     * @throws IOException On I/O error
     * @throws NoSuchPaddingException On invalid padding
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] decrypt(InputStream is) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        DataInputStream dais = new DataInputStream(is);
        byte[] header = new byte[8];
        dais.readFully(header);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] dec = decrypt(is, header);
        baos.write(header);
        baos.write(dec);
        return baos.toByteArray();
    }

    /**
     * Decrypts data.
     *
     * @param is Data to decrypt
     * @param header Header
     * @return Decrypted data
     * @throws IOException On I/O error
     * @throws NoSuchPaddingException On invalid padding
     * @throws NoSuchAlgorithmException On invalid algorithm
     * @throws InvalidKeyException On invalid key
     * @throws InvalidAlgorithmParameterException On invalid algorithm parameter
     * @throws IllegalBlockSizeException On illegal block size
     * @throws BadPaddingException On bad padding
     */
    public static byte[] decrypt(InputStream is, byte[] header) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        header[0] -= 32; // to uppercase

        long key = getkey(header);

        DataInputStream dais = new DataInputStream(is);

        byte[] encryptedLengthBytes = new byte[4];
        dais.readFully(encryptedLengthBytes);
        long encryptedLength = unpack(encryptedLengthBytes, 0);
        int decryptedLength = (int) (encryptedLength ^ key);

        int paddedLength = (int) (decryptedLength + 0x1F) & ~0x1F;

        byte[] aesIV = new byte[16];
        System.arraycopy(header, 0, aesIV, 0, header.length); //header
        System.arraycopy(encryptedLengthBytes, 0, aesIV, 8, 4); //encrypted length
        aesIV[12] = (byte) (key & 0xff);
        aesIV[13] = (byte) ((key >> 8) & 0xff);
        aesIV[14] = (byte) ((key >> 16) & 0xff);
        aesIV[15] = (byte) ((key >> 24) & 0xff);

        for (int i = 0; i < 16; i++) {
            aesIV[i] ^= GLOBAL_KEY.charAt(i);
        }

        // aes key
        // this one is stored at the end of the file
        byte[] aesKey = new byte[32];
        byte[] data = new byte[paddedLength];
        dais.readFully(data);

        byte[] aesKeyData = new byte[32];
        dais.readFully(aesKeyData);

        for (int i = 0; i < 32; i += 4) {
            long value = unpack(aesKeyData, i);
            if ((i & 4) == 4) {
                value -= key;
            } else {
                value += key;
            }
            aesKey[i] = (byte) (value & 0xff);
            aesKey[i + 1] = (byte) ((value >> 8) & 0xff);
            aesKey[i + 2] = (byte) ((value >> 16) & 0xff);
            aesKey[i + 3] = (byte) ((value >> 24) & 0xff);
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(aesIV));

        byte[] decryptedData = cipher.doFinal(data);
        return Arrays.copyOfRange(decryptedData, 0, decryptedLength);
    }

    /**
     * Encrypts/decrypts data.
     *
     * @param args Command line arguments
     * @throws Exception On error
     */
    public static void main(String[] args) throws Exception {
        byte[] data = new byte[]{'C', 'W', 'S', 0x32, 0x01, 0x02, 0x03, 0x04, 0x41, 0x42, 0x43, 0xd, 0xa};
        byte[] encrypted = encrypt(data);
        byte[] decrypted = decrypt(encrypted);

        if (!Arrays.equals(data, decrypted)) {
            throw new RuntimeException("Cannot encrypt/decrypt");
        }
    }
}
