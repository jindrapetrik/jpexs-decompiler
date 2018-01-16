/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.helpers;

import com.jpexs.helpers.utf8.Utf8Helper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * MD5 crypt - based on passlib.hash.md5_crypt
 *
 * @author JPEXS
 */
public class MD5Crypt {

    private static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    private static final String HASH64_CHARS = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String MAGIC = "$1$";

    public static final String MAGIC_APACHE = "$apr1$";

    public static boolean checkPassword(String password, String hash) {
        String magic;

        if (hash.startsWith(MAGIC)) {
            magic = MAGIC;
        } else if (hash.startsWith(MAGIC_APACHE)) {
            magic = MAGIC_APACHE;
        } else {
            return false;
        }

        String checksum = hash.substring(magic.length());
        String salt = "";
        if (checksum.contains("$")) {
            salt = checksum.substring(0, checksum.indexOf('$'));
        }
        return hash.equals(crypt(password, salt, magic));
    }

    public static String generateSalt(int length) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(SALT_CHARS.charAt(rnd.nextInt(SALT_CHARS.length())));
        }
        return sb.toString();
    }

    public static String crypt(String password, int saltLength, String magic) {
        return crypt(password, generateSalt(saltLength), magic);
    }

    public static String crypt(String password, int saltLength) {
        return crypt(password, generateSalt(saltLength), MAGIC);
    }

    public static String cryptApache(String password, int saltLength) {
        return crypt(password, generateSalt(saltLength), MAGIC_APACHE);
    }

    public static String crypt(String password, String salt) {
        return crypt(password, salt, MAGIC);
    }

    public static String cryptApache(String password, String salt) {
        return crypt(password, salt, MAGIC_APACHE);
    }

    private static String crypt(String password, String salt, String magic) {

        if (salt.startsWith(magic)) {
            salt = salt.substring(magic.length());
        }
        if (salt.length() > 8) {
            salt = salt.substring(0, 8);
        }

        byte[] passwordBytes = password.getBytes(Utf8Helper.charset);
        byte[] saltBytes = salt.getBytes(Utf8Helper.charset);
        byte[] constBytes = magic.getBytes(Utf8Helper.charset);

        MessageDigest b;
        try {
            b = MessageDigest.getInstance("MD5"); //Start MD5 digest B
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
        b.update(passwordBytes);   //Add the password to digest B
        b.update(saltBytes);//Add the salt to digest B
        b.update(passwordBytes);   //Add the password to digest B
        byte[] digest_b = b.digest();   //Finish MD5 digest B

        MessageDigest a;
        try {
            a = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
        a.update(passwordBytes);   //Add the password to digest A
        a.update(constBytes);  //Add the constant string $1$ to digest A
        a.update(saltBytes);   //Add the salt to digest A

        //For each block of 16 bytes in the password string, add digest B to digest A
        for (int i = passwordBytes.length; i > 0; i -= 16) {
            if (i >= 16) {
                a.update(digest_b);
            } else { //For the remaining N bytes of the password string, add the first N bytes of digest B to digest A
                a.update(digest_b, 0, i);
            }
        }

        /*
         For each bit in the binary representation of the length of the password string; starting with the lowest value bit, up to and including the largest-valued bit that is set to 1:

         If the current bit is set 0 (!! not 1), add the first character of the password to digest A.
         Otherwise, add a NULL character to digest A.

         (If the password is the empty string, step 14 is omitted entirely).
         */
        for (int i = passwordBytes.length; i > 0; i = i >>> 1) {
            if ((i & 1) == 0) {
                a.update(passwordBytes, 0, 1);
            } else {
                a.update((byte) 0);
            }
        }

        byte[] digest_a = a.digest(); //Finish MD5 digest A
        byte[] round_result = null;

        //For 1000 rounds (round values 0..999 inclusive)
        for (int round = 0; round < 1000; round++) {
            MessageDigest c;
            try {
                c = MessageDigest.getInstance("MD5"); //Start MD5 digest C
            } catch (NoSuchAlgorithmException ex) {
                return null;
            }

            //If the round is odd, add the password to digest C
            if (round % 2 == 1) {
                c.update(passwordBytes);
            }

            //If the round is even, add the previous round’s result to digest C (for round 0, add digest A instead).
            if (round % 2 == 0) {
                if (round == 0) {
                    c.update(digest_a);
                } else {
                    c.update(round_result);
                }
            }
            //If the round is not a multiple of 3, add the salt to digest C.
            if (round % 3 != 0) {
                c.update(saltBytes);
            }

            //If the round is not a multiple of 7, add the password to digest C.
            if (round % 7 != 0) {
                c.update(passwordBytes);
            }

            //If the round is even, add the password to digest C.
            if (round % 2 == 0) {
                c.update(passwordBytes);
            }

            //If the round is odd, add the previous round’s result to digest C (for round 0, add digest A instead).
            if (round % 2 == 1) {
                if (round == 0) {
                    c.update(digest_a);
                } else {
                    c.update(round_result);
                }
            }

            //Use the final value of MD5 digest C as the result for this round.
            round_result = c.digest();
        }

        //Transpose the 16 bytes of the final round’s result in the following order:
        //12,6,0,13,7,1,14,8,2,15,9,3,5,10,4,11
        byte[] transposed = new byte[]{
            round_result[12],
            round_result[6],
            round_result[0],
            round_result[13],
            round_result[7],
            round_result[1],
            round_result[14],
            round_result[8],
            round_result[2],
            round_result[15],
            round_result[9],
            round_result[3],
            round_result[5],
            round_result[10],
            round_result[4],
            round_result[11]};

        //Encode the resulting 16 byte string into a 22 character hash64-encoded string
        //(the 2 msb bits encoded by the last hash64 character are used as 0 padding).
        StringBuilder result = new StringBuilder();

        int dstCharRem = 22;
        for (int srcBytePos = 0; srcBytePos < transposed.length; srcBytePos += 3, dstCharRem -= 4) {
            long v = (transposed[srcBytePos] & 0xff);
            if (srcBytePos + 1 < transposed.length) {
                v |= ((transposed[srcBytePos + 1] & 0xff) << 8);
            }
            if (srcBytePos + 2 < transposed.length) {
                v |= ((transposed[srcBytePos + 2] & 0xff) << 16);
            }
            for (int j = 0; j < (dstCharRem >= 4 ? 4 : dstCharRem); j++) {
                result.append(HASH64_CHARS.charAt((int) (v & 0x3f)));
                v >>>= 6;
            }
        }
        return magic + salt + "$" + result.toString();
    }
}
