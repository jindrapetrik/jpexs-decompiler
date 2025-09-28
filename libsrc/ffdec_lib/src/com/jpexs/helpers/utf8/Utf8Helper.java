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
package com.jpexs.helpers.utf8;

import com.jpexs.helpers.utf8.charset.Gb2312;
import com.jpexs.helpers.utf8.charset.ShiftJis;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for UTF-8 charset.
 *
 * @author JPEXS
 */
public class Utf8Helper {

    public static String charsetName = "UTF-8";

    public static Charset charset = Charset.forName("UTF-8");

    private static List<String> allowedVariableLengthCharsets = Arrays.asList(
            "GB2312", "Shift_JIS", "UTF-8", "UTF-16", "UTF16-BE", "UTF-16-LE", "UTF-32", "UTF-32LE", "UTF-32BE");

    private static List<String> allowedCharsets = null;

    /**
     * Get a list of allowed charsets. They are limited to single byte charsets
     * + allowedVariableLengthCharsets
     */
    public static List<String> getAllowedCharsets() {
        if (allowedCharsets != null) {
            return allowedCharsets;
        }

        allowedCharsets = new ArrayList<>();

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

        return allowedCharsets;
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

    public static String stripEscapes(String string) {
        return string.replaceAll("\\{invalid_utf8=[0-9]+\\}", "")
                .replaceAll("\\{\\+(\\+*invalid_utf8=[0-9]+)\\}", "{$1}");
    }

    public static byte[] getBytes(String string) {
        if (!string.contains("invalid_utf8")) {
            return string.getBytes(charset);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Pattern invPattern = Pattern.compile("^(\\{invalid_utf8[=:]([0-9]+)\\}).*", Pattern.DOTALL);
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (c == '{') {
                    String subStr = string.substring(i);
                    if (!subStr.isEmpty() && subStr.charAt(0) == '+') {
                        baos.write(("" + c).getBytes(charset));
                        i++;
                        continue;
                    }

                    Matcher m = invPattern.matcher(subStr);
                    if (m.matches()) {
                        int v = Integer.parseInt(m.group(2));
                        baos.write(v);
                        i += m.group(1).length();
                        i--;
                        continue;
                    }

                }
                baos.write(("" + c).getBytes(charset));
            }
        } catch (IOException iex) {
            //should not happen
        }

        return baos.toByteArray();
    }

    public static int getBytesLength(String string) {
        // todo: make it faster without actually writing it to an array
        return getBytes(string).length;
    }
    
    public static int getBytesLength(String string, String charset) {
        if (charset.toUpperCase().equals(charsetName)) {
            return getBytesLength(string);
        }
        try {
            return string.getBytes(charset).length;
        } catch (UnsupportedEncodingException ex) {
            return 0; //Should not happen
        }
    }

    private static String escapeInvalidUtf8Char(int v) {
        //Note: for writing the string "{invalid_utf8=xxx}" itself, you can escape it with "{+invalid_utf8=xxx}"
        return "{invalid_utf8=" + v + "}";
    }

    public static String decode(byte[] data) {
        return decode(data, 0, data.length);
    }

    public static String decode(byte[] data, int start, int length) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (int i = 0; i < length; i++) {
                int v = data[i] & 0xff;
                int numNextBytes = 0;
                if ((v & 0x80) == 0) { //0xxxxxxx
                    baos.write(v);
                } else if ((v & 0xC0) == 0x80) { //10xxxxxx
                    baos.write(escapeInvalidUtf8Char(v).getBytes("UTF-8"));
                } else if ((v & 0xE0) == 0xC0) { //110xxxxx
                    numNextBytes = 1;
                } else if ((v & 0xF0) == 0xE0) { //1110xxxx
                    numNextBytes = 2;
                } else if ((v & 0xF8) == 0xF0) { //11110xxx
                    numNextBytes = 3;
                } else {
                    baos.write(escapeInvalidUtf8Char(v).getBytes("UTF-8"));
                }
                if (numNextBytes > 0) {
                    if (i + numNextBytes >= length) {
                        baos.write(escapeInvalidUtf8Char(v).getBytes("UTF-8"));
                        continue;
                    }
                    boolean validNextBytes = true;
                    for (int j = 0; j < numNextBytes; j++) {
                        int v2 = data[i + 1 + j] & 0xff;
                        if ((v2 & 0xC0) != 0x80) { //must be 10xxxxxx

                            if ((v2 & 0x80) == 0) { //0xxxxxxx
                                numNextBytes = j;
                            }
                            validNextBytes = false;
                            break;
                        }
                    }
                    if (!validNextBytes) {
                        baos.write(escapeInvalidUtf8Char(v).getBytes("UTF-8"));
                        for (int j = 0; j < numNextBytes; j++) {
                            int v2 = data[i + 1 + j] & 0xff;
                            baos.write(escapeInvalidUtf8Char(v2).getBytes("UTF-8"));
                        }
                    } else {
                        baos.write(v);
                        for (int j = 0; j < numNextBytes; j++) {
                            int v2 = data[i + 1 + j] & 0xff;
                            baos.write(v2);
                        }
                    }
                    i += numNextBytes;
                }
            }
        } catch (IOException ex) {
            //ignored
        }
        return new String(baos.toByteArray(), Utf8Helper.charset);
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
