/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author JPEXS
 *
 * Based on swfdecrypt.py by NathaanTFM
 */
public class HarmanDecryption {

    private static final String GLOBAL_KEY = "Adobe AIR SDK (c) 2021 HARMAN Internation Industries Incorporated";

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

    private static long unpack(byte data[], int start) {
        return (data[start] & 0xff)
                + ((long) (data[start + 1] & 0xff) << 8)
                + ((long) (data[start + 2] & 0xff) << 16)
                + ((long) (data[start + 3] & 0xff) << 24);
    }

    public InputStream decrypt(InputStream is, byte[] header) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        header[0] -= 32; // to uppercase

        long key = getkey(header);

        //get the length
        DataInputStream dais = new DataInputStream(is);

        byte encryptedLengthBytes[] = new byte[4];
        dais.readFully(encryptedLengthBytes);
        long encryptedLength = unpack(encryptedLengthBytes, 0);
        int decryptedLength = (int) (encryptedLength ^ key);

        //padded length
        int paddedLength = (int) (decryptedLength + 0x1F) & ~0x1F;

        //aes iv
        byte aesIV[] = new byte[16];
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
        byte aesKey[] = new byte[32];        
        byte data[] = new byte[paddedLength];
        dais.readFully(data);

        byte aesKeyData[] = new byte[32];
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

        byte decryptedData[] = cipher.doFinal(data);
        return new ByteArrayInputStream(Arrays.copyOfRange(decryptedData, 0, decryptedLength));
    }
}
