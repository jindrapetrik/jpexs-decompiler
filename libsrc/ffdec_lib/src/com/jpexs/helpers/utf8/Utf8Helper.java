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
package com.jpexs.helpers.utf8;

import com.jpexs.helpers.utf8.charset.Gb2312;
import com.jpexs.helpers.utf8.charset.ShiftJis;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Utf8Helper {

    public static String charsetName = "UTF-8";

    public static Charset charset = Charset.forName("UTF-8");
    
    private static List<String> allowedVariableLengthCharsets = Arrays.asList(
            "GB2312", "Shift_JIS", "UTF-8", "UTF-16", "UTF16-BE", "UTF-16-LE", "UTF-32", "UTF-32LE", "UTF-32BE");
    
    /**
     * Allowed charsets. They are limited to single byte charsets + allowedVariableLengthCharsets
     */
    public static List<String> allowedCharsets = new ArrayList<>();

    static {
        Map<String, Charset> charsets = Charset.availableCharsets();
        for (String s : charsets.keySet()) {
            Charset charset = charsets.get(s);
            int maxLen = 0;
            int minLen = Integer.MAX_VALUE;
            try {
                for (int i = 0; i < 65536; i++) {

                    ByteBuffer buf = charset.encode("" + (char) i);
                    int len = buf.remaining();
                    if (len > maxLen) {
                        maxLen = len;
                    }
                    if (len < minLen) {
                        minLen = len;
                    }
                }
                if ((minLen == maxLen && minLen == 1) || allowedVariableLengthCharsets.contains(s)) {
                    allowedCharsets.add(s);
                }                    
            } catch (UnsupportedOperationException ex) {
                //System.out.println(s + " ... ERROR");
            }

        }
    }

    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new Error(ex);
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new Error(ex);
        }
    }

    public static byte[] getBytes(String string) {
        return string.getBytes(charset);
    }

    public static int getBytesLength(String string) {
        // todo: make it faster without actually writing it to an array
        return string.getBytes(charset).length;
    }

    public static char codePointToChar(int codePoint, String charsetName) {
        int newCodePoint;
        switch (charsetName) {
            case "GB2312":
                newCodePoint = new Gb2312().toUnicode(codePoint);
                break;
            case "Shift_JIS":
                newCodePoint = new ShiftJis().toUnicode(codePoint);
                break;
            case "UTF-8":
            case "UTF-16":
            case "UTF-16BE":
            case "UTF-16LE":
            case "UTF-32":
            case "UTF-32BE":
            case "UTF-32LE":
                newCodePoint = codePoint;
                break;
            default: {
                //Assuming single byte - ANSI
                newCodePoint = -1;
                try {
                    newCodePoint = new String(new byte[]{(byte) codePoint}, charsetName).codePointAt(0);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Utf8Helper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        if (newCodePoint >= 0) {
            return (char) newCodePoint;
        }
        return '?';
    }

    public static int charToCodePoint(char character, String charsetName) {
        int unicodeCodePoint = (int) character;
        int codePoint;
        switch (charsetName) {
            case "GB2312":
                codePoint = new Gb2312().fromUnicode(unicodeCodePoint);
                break;
            case "Shift_JIS":
                codePoint = new ShiftJis().fromUnicode(unicodeCodePoint);
                break;
            case "UTF-8":
            case "UTF-16":
            case "UTF-16BE":
            case "UTF-16LE":
            case "UTF-32":
            case "UTF-32BE":
            case "UTF-32LE":
                codePoint = unicodeCodePoint;
                break;
            default: {
                codePoint = -1;
                try {
                    //assuming single byte ANSI
                    codePoint = ("" + character).getBytes(charsetName)[0] & 0xff;
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Utf8Helper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return codePoint;
    }
}
