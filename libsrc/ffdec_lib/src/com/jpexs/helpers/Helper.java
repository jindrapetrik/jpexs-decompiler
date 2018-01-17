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

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.Freed;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Component;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

/**
 * Class with helper method
 *
 * @author JPEXS, Paolo Cancedda
 */
public class Helper {

    public static String newLine = System.getProperty("line.separator");

    public static String hexData = "#hexdata";

    public static String constants = "#constants";

    public static String decompilationErrorAdd = null;

    private static final Map<BitSet, Area> shapeCache = new HashMap<>();

    private static final String[] hexStringCache;

    static {
        hexStringCache = new String[256];
        for (int i = 0; i < hexStringCache.length; i++) {
            hexStringCache[i] = String.format("%02x", i);
        }
    }

    /**
     * Converts array of int values to string
     *
     * @param array Array of int values
     * @return String representation of the array
     */
    public static String intArrToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        intArrToStringBuilder(array, sb);
        return sb.toString();
    }

    public static void intArrToStringBuilder(int[] array, StringBuilder sb) {
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
    }

    /**
     * Converts array of byte values to string
     *
     * @param array Array of byte values
     * @return String representation of the array
     */
    public static String byteArrToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hexStringCache[array[i] & 0xff]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Adds zeros to beginning of the number to fill specified length. Returns
     * as string
     *
     * @param number Number as string
     * @param length Length of new string
     * @return Number with added zeros
     */
    public static String padZeros(String number, int length) {
        int count = length - number.length();
        for (int i = 0; i < count; i++) {
            number = "0" + number;
        }
        return number;
    }

    /**
     * Formats specified address to four numbers xxxx (or five numbers when
     * showing decimal addresses)
     *
     * @param number Address to format
     * @return String representation of the address
     */
    public static String formatAddress(long number) {
        return formatAddress(number, Configuration.decimalAddress.get());
    }

    /**
     * Formats specified address to four numbers xxxx (or five numbers when
     * showing decimal addresses)
     *
     * @param number Address to format
     * @param decimal Use decimal format
     * @return String representation of the address
     */
    public static String formatAddress(long number, boolean decimal) {
        if (decimal) {
            return String.format("%05d", number);
        }
        return String.format("%04x", number);
    }

    /**
     * Escapes string by adding backslashes
     *
     * @param s String to escape
     * @return Escaped string
     */
    public static String escapeString(String s) {
        StringBuilder ret = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret.append("\\n");
            } else if (c == '\r') {
                ret.append("\\r");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\b') {
                ret.append("\\b");
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '\\') {
                ret.append("\\\\");
            } else if (c < 32) {
                ret.append("\\x").append(byteToHex((byte) c));
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Escapes string by adding backslashes
     *
     * @param s String to escape
     * @return Escaped string
     */
    public static String escapeActionScriptString(String s) {
        StringBuilder ret = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret.append("\\n");
            } else if (c == '\r') {
                ret.append("\\r");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\b') {
                ret.append("\\b");
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '\\') {
                ret.append("\\\\");
            } else if (c == '"') {
                ret.append("\\\"");
            } else if (c == '\'') {
                ret.append("\\'");
            } else if (c < 32) {
                ret.append("\\x").append(byteToHex((byte) c));
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Escapes string by adding backslashes
     *
     * @param s String to escape
     * @return Escaped string
     */
    public static String escapeJavaString(String s) {
        StringBuilder ret = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret.append("\\n");
            } else if (c == '\r') {
                ret.append("\\r");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\b') {
                ret.append("\\b");
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '\\') {
                ret.append("\\\\");
            } else if (c == '"') {
                ret.append("\\\"");
            } else if (c < 32) {
                // \\x is not available in Java string, we should use \\u instead
                ret.append("\\u00").append(byteToHex((byte) c));
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode
     * character.</li>
     * </ul>
     *
     * @param st A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    public static String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }

                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }

                i++;
            }

            sb.append(ch);
        }

        return sb.toString();
    }

    public static String getValidHtmlId(String text) {
        // ID and NAME tokens must begin with a letter ([A-Za-z]) and
        // may be followed by any number of letters, digits ([0-9]),
        // hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (i > 0 && ((ch >= '0' && ch <= '9')
                    || ch == '-' || ch == '_' || ch == ':' || ch == '.'))) {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private final static String SPACES12 = "            ";

    private final static String ZEROS8 = "00000000";

    public static String formatHex(int value, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(value));
        if (width > sb.length()) {
            sb.insert(0, ZEROS8, 0, width - sb.length());
        }
        return sb.toString();
    }

    public static String formatHex(long value, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(value));
        if (width > sb.length()) {
            sb.insert(0, ZEROS8, 0, width - sb.length());
        }
        return sb.toString();
    }

    public static String formatInt(int value, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        if (width > sb.length()) {
            sb.insert(0, SPACES12, 0, width - sb.length());
        }
        return sb.toString();
    }

    public static String indent(int level, String ss, String indentStr) {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < level; ii++) {
            sb.append(indentStr);
        }
        sb.append(ss);
        return sb.toString();
    }

    public static String indentRows(int level, String ss, String indentStr) {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < level; ii++) {
            sb.append(indentStr);
        }
        ss = ss.replaceAll("(\r\n|\r|\n)", "\r\n");
        ss = "\r\n" + ss;
        String repl = "\r\n" + sb.toString();
        ss = ss.replace("\r\n", repl);
        if (ss.endsWith(repl)) {
            ss = ss.substring(0, ss.length() - sb.toString().length());
        }
        ss = ss.substring(2);
        return ss;
    }

    public static String unindentRows(int prefixLineCount, int level, String text) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(text);
        String indentStr = "";
        for (int i = 0; i < level; i++) {
            indentStr += Configuration.getCodeFormatting().indentString;
        }
        int indentLength = indentStr.length();
        for (int i = 0; i < prefixLineCount; i++) {
            scanner.nextLine(); // ignore line
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith(indentStr)) {
                sb.append(line.substring(indentLength)).append(Configuration.getCodeFormatting().newLineChars);
            } else {
                return sb.toString();
            }
        }
        return sb.toString();
    }

    public static int getLineCount(String s) {
        if (s.endsWith("\r\n")) {
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("\r")) {
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] parts = s.split("(\r\n|\r|\n)");
        return parts.length;
    }

    public static String padZeros(long number, int length) {
        String ret = Long.toString(number);
        while (ret.length() < length) {
            ret = "0" + ret;
        }
        return ret;
    }

    public static String byteToHex(byte b) {
        return hexStringCache[b & 0xff];
    }

    public static String byteArrayToHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(hexStringCache[b & 0xff]);
        }

        return sb.toString();
    }

    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, 0);
    }

    public static String bytesToHexString(byte[] bytes, int start) {
        StringBuilder sb = new StringBuilder();
        if (start < bytes.length) {
            for (int ii = start; ii < bytes.length; ii++) {
                sb.append(formatHex(bytes[ii] & 0xff, 2));
                sb.append(' ');
            }
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String bytesToHexString(int maxByteCountInString, byte[] bytes, int start) {
        if (bytes.length - start <= maxByteCountInString) {
            return bytesToHexString(bytes, start);
        }
        byte[] trailingBytes = new byte[maxByteCountInString / 2];
        byte[] headingBytes = new byte[maxByteCountInString - trailingBytes.length];
        System.arraycopy(bytes, start, headingBytes, 0, headingBytes.length);
        int startOfTrailingBytes = bytes.length - trailingBytes.length;
        System.arraycopy(bytes, startOfTrailingBytes, trailingBytes, 0, trailingBytes.length);
        StringBuilder sb = new StringBuilder();
        sb.append(bytesToHexString(headingBytes, 0));
        if (trailingBytes.length > 0) {
            sb.append(" ... ");
            sb.append(bytesToHexString(trailingBytes, 0));
        }
        return sb.toString();
    }

    public static String format(String str, int len) {
        if (len <= str.length()) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        for (int ii = str.length(); ii < len; ii++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String joinStrings(Iterable<?> arr, String glue) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (Object s : arr) {
            if (!first) {
                ret.append(glue);
            } else {
                first = false;
            }
            ret.append(s);
        }
        return ret.toString();
    }

    public static String joinStrings(String[] arr, String glue) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (String s : arr) {
            if (!first) {
                ret.append(glue);
            } else {
                first = false;
            }
            ret.append(s);
        }
        return ret.toString();
    }

    public static String joinStrings(List<String> arr, String formatString, String glue) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (String s : arr) {
            if (!first) {
                ret.append(glue);
            } else {
                first = false;
            }
            ret.append(String.format(formatString, s));
        }
        return ret.toString();
    }

    public static String joinStrings(String[] arr, String formatString, String glue) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (String s : arr) {
            if (!first) {
                ret.append(glue);
            } else {
                first = false;
            }
            ret.append(String.format(formatString, s));
        }
        return ret.toString();
    }

    @SuppressWarnings("unchecked")
    public static <E> E deepCopy(E o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(o);
                oos.flush();
            }
            E copy;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                copy = (E) ois.readObject();
            }
            return copy;
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, "Copy error", ex);
            return null;
        }
    }

    public static List<Object> toList(Object... rest) {
        List<Object> ret = new ArrayList<>();
        ret.addAll(Arrays.asList(rest));
        return ret;
    }

    public static int[] toIntArray(Collection<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (int i2 : list) {
            ret[i++] = i2;
        }

        return ret;
    }

    public static ByteArrayInputStream getInputStream(byte[]... data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            for (byte[] d : data) {
                baos.write(d);
            }
        } catch (IOException iex) {
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static byte[] readFile(String... file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String f : file) {
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] buf = new byte[4096];
                int cnt;
                while ((cnt = fis.read(buf)) > 0) {
                    baos.write(buf, 0, cnt);
                }
            } catch (IOException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return baos.toByteArray();
    }

    public static byte[] readFileEx(String... file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String f : file) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);

                byte[] buf = new byte[4096];
                int cnt;
                while ((cnt = fis.read(buf)) > 0) {
                    baos.write(buf, 0, cnt);
                }
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    public static String readTextFileEx(String... file) throws IOException {
        byte[] data = readFileEx(file);
        if (data.length > 1 && data[0] == (byte) 0xef && data[1] == (byte) 0xbb && data[2] == (byte) 0xbf) {
            // remove UTF-8 BOM
            return new String(data, 3, data.length - 3, Utf8Helper.charset);
        }

        return new String(data, Utf8Helper.charset);
    }

    public static String readTextFile(String... file) {
        byte[] data = readFile(file);
        if (data.length > 1 && data[0] == (byte) 0xef && data[1] == (byte) 0xbb && data[2] == (byte) 0xbf) {
            // remove UTF-8 BOM
            return new String(data, 3, data.length - 3, Utf8Helper.charset);
        }

        return new String(data, Utf8Helper.charset);
    }

    public static byte[] readStream(InputStream is) {
        if (is instanceof MemoryInputStream) {
            return ((MemoryInputStream) is).getAllRead();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos, Long.MAX_VALUE);
        return baos.toByteArray();
    }

    public static void copyStream(InputStream is, OutputStream os) {
        try {
            final int bufSize = 4096;
            byte[] buf = new byte[bufSize];
            int cnt = 0;
            while ((cnt = is.read(buf)) > 0) {
                os.write(buf, 0, cnt);
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void copyStream(InputStream is, OutputStream os, long maxLength) {
        try {
            final int bufSize = 4096;
            byte[] buf = new byte[bufSize];
            int cnt = 0;
            while ((cnt = is.read(buf)) > 0) {
                os.write(buf, 0, cnt);
                maxLength -= cnt;

                // last chunk is smaller
                if (maxLength < bufSize) {
                    buf = new byte[(int) maxLength];
                }
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void appendFile(String file, byte[]... data) {
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            for (byte[] d : data) {
                fos.write(d);
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void writeFile(String file, byte[]... data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (byte[] d : data) {
                fos.write(d);
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void writeFile(String file, InputStream stream) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            copyStream(stream, fos);
        } catch (IOException ex) {
            // ignore
        }
    }

    public static String stackToString(TranslateStack stack, LocalData localData) throws InterruptedException {
        String ret = "[";
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (i < stack.size() - 1) {
                ret += ", ";
            }
            ret += stack.get(i).toString(localData);
        }
        ret += "]";
        return ret;
    }

    public static File fixDialogFile(File f) {
        Pattern pat = Pattern.compile("\"([^\"]+)\"");
        String name = f.getAbsolutePath();
        Matcher m = pat.matcher(name);
        if (m.find()) {
            f = new File(m.group(1));
        }
        return f;
    }

    private static final BitSet fileNameInvalidChars;

    private static final List<String> invalidFilenamesParts;

    static {
        BitSet toEncode = new BitSet(256);

        for (int i = 0; i < 32; i++) {
            toEncode.set(i);
        }

        toEncode.set('\\');
        toEncode.set('/');
        toEncode.set(':');
        toEncode.set('*');
        toEncode.set('?');
        toEncode.set('"');
        toEncode.set('<');
        toEncode.set('>');
        toEncode.set('|');

        fileNameInvalidChars = toEncode;

        //windows reserved filenames:
        invalidFilenamesParts = new ArrayList<>();
        invalidFilenamesParts.add("CON");
        invalidFilenamesParts.add("PRN");
        invalidFilenamesParts.add("AUX");
        invalidFilenamesParts.add("CLOCK$");
        invalidFilenamesParts.add("NUL");
        for (int i = 1; i <= 9; i++) {
            invalidFilenamesParts.add("COM" + i);
            invalidFilenamesParts.add("LPT" + i);
        }
    }

    public static String makeFileName(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            int ch = (int) str.charAt(i);
            if (ch < 256 && fileNameInvalidChars.get(ch)) {
                sb.append("%").append(String.format("%02X", ch));
            } else {
                sb.append((char) ch);
            }
        }

        char lastChar = sb.charAt(sb.length() - 1);
        if (lastChar == ' ') {
            sb.setLength(sb.length() - 1);
            sb.append("%20");
        } else if (lastChar == '.') {
            sb.setLength(sb.length() - 1);
            sb.append("%2E");
        }

        str = sb.toString();
        for (String inv : invalidFilenamesParts) {
            if (str.startsWith(inv)) {
                if (str.equals(inv) || str.startsWith(".")) {
                    str = "_" + str;
                }
            }
        }

        if (str.isEmpty()) {
            str = "unnamed";
        }
        return str;
    }

    public static String strToHex(String s) {
        byte[] bs = Utf8Helper.getBytes(s);
        String sn = "";
        for (int i = 0; i < bs.length; i++) {
            sn += "0x" + Integer.toHexString(bs[i] & 0xff) + " ";
        }
        return sn;
    }

    public static void emptyObject(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC
                    || f.getType().isPrimitive()) {
                continue;
            }
            try {
                f.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

                Object v = f.get(obj);
                if (v != null) {
                    try {
                        if (v instanceof Collection) {
                            ((Collection) v).clear();
                        }
                        if (v instanceof Component) {
                            if (((Component) v).getParent() != null) {
                                ((Component) v).getParent().remove((Component) v);
                            }
                        }
                        if (v instanceof Freed) {
                            Freed freed = ((Freed) v);
                            if (!freed.isFreeing()) {
                                ((Freed) v).free();
                            }
                        }
                    } catch (Throwable t) {
                    }

                    f.set(obj, null);
                }
            } catch (UnsupportedOperationException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
                throw new Error(ex);
            }
        }
    }

    public static String formatTimeSec(long timeMs) {
        long timeS = timeMs / 1000;
        timeMs %= 1000;
        long timeM = timeS / 60;
        timeS %= 60;
        long timeH = timeM / 60;
        timeM %= 60;
        String timeStr = "";
        if (timeH > 0) {
            timeStr += Helper.padZeros(timeH, 2) + ":";
        }
        timeStr += Helper.padZeros(timeM, 2) + ":";
        timeStr += Helper.padZeros(timeS, 2) + "." + Helper.padZeros(timeMs, 3);
        return timeStr;
    }

    public static String formatFileSize(long fileSizeLong) {
        double fileSize = fileSizeLong;
        if (fileSize < 1024) {
            return String.format("%d bytes", fileSizeLong);
        }
        fileSize /= 1024;
        if (fileSize < 1024) {
            return String.format("%.2f KB", fileSize);
        }
        fileSize /= 1024;
        return String.format("%.2f MB", fileSize);
    }

    public static void freeMem() {
        Cache.clearAll();
        System.gc();
    }

    public static String formatTimeToText(int timeS) {
        long timeM = timeS / 60;
        timeS %= 60;
        long timeH = timeM / 60;
        timeM %= 60;

        String timeStr = "";
        String strAnd = AppResources.translate("timeFormat.and");
        String strHour = AppResources.translate("timeFormat.hour");
        String strHours = AppResources.translate("timeFormat.hours");
        String strMinute = AppResources.translate("timeFormat.minute");
        String strMinutes = AppResources.translate("timeFormat.minutes");
        String strSecond = AppResources.translate("timeFormat.second");
        String strSeconds = AppResources.translate("timeFormat.seconds");

        if (timeH > 0) {
            timeStr += timeH + " " + (timeH > 1 ? strHours : strHour);
        }
        if (timeM > 0) {
            if (timeStr.length() > 0) {
                timeStr += " " + strAnd + " ";
            }
            timeStr += timeM + " " + (timeM > 1 ? strMinutes : strMinute);
        }
        if (timeS > 0) {
            if (timeStr.length() > 0) {
                timeStr += " " + strAnd + " ";
            }
            timeStr += timeS + " " + (timeS > 1 ? strSeconds : strSecond);
        }

        // (currently) used only in log, so no localization is required
        return timeStr;
    }

    public static GraphTextWriter byteArrayToHexWithHeader(GraphTextWriter writer, byte[] data) {
        writer.appendNoHilight(hexData).newLine().newLine();
        return byteArrayToHex(writer, data, 8, 8, false, false);
    }

    public static GraphTextWriter byteArrayToHex(GraphTextWriter writer, byte[] data, int bytesPerRow, int groupSize, boolean addChars, boolean showAddress) {

        /* // hex data from decompiled actions
         Scanner scanner = new Scanner(srcWithHex);
         while (scanner.hasNextLine()) {
         String line = scanner.nextLine().trim();
         if (line.startsWith(";")) {
         result.append(line.substring(1).trim()).append(nl);
         } else {
         result.append(";").append(line).append(nl);
         }
         }*/
        int length = data.length;

        int rowCount = length / bytesPerRow;
        if (length % bytesPerRow > 0) {
            rowCount++;
        }

        long address = 0;
        for (int row = 0; row < rowCount; row++) {
            if (row > 0) {
                writer.newLine();
            }

            if (showAddress) {
                writer.appendNoHilight("0x" + String.format("%08x ", address));
            }

            for (int i = 0; i < bytesPerRow; i++) {
                int idx = row * bytesPerRow + i;
                if (length > idx) {
                    if (i > 0 && i % groupSize == 0) {
                        writer.appendNoHilight(" ");
                    }
                    writer.appendNoHilight(byteToHex(data[idx])).appendNoHilight(" ");
                } else if (addChars) {
                    if (i > 0 && i % groupSize == 0) {
                        writer.appendNoHilight(" ");
                    }
                    writer.appendNoHilight("   ");
                }
                address += bytesPerRow;
            }

            if (addChars) {
                writer.appendNoHilight("  ");
                for (int i = 0; i < bytesPerRow; i++) {
                    int idx = row * bytesPerRow + i;
                    if (length == idx) {
                        break;
                    }
                    if (i > 0 && i % groupSize == 0) {
                        writer.appendNoHilight(" ");
                    }
                    byte ch = data[idx];
                    if (ch >= 0 && ch < 32) {
                        ch = '.';
                    }
                    writer.appendNoHilight((char) ch + "");
                }
            }
        }

        writer.newLine();
        return writer;
    }

    public static byte[] getBytesFromHexaText(String text) {
        Scanner scanner = new Scanner(text);
        scanner.nextLine(); // ignore first line
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith(";")) {
                continue;
            }
            line = line.replace(" ", "");
            for (int i = 0; i < line.length() / 2; i++) {
                String hexStr = line.substring(i * 2, (i + 1) * 2);
                byte b = (byte) Integer.parseInt(hexStr, 16);
                baos.write(b);
            }
        }
        byte[] data = baos.toByteArray();
        return data;
    }

    public static List<List<String>> getConstantPoolsFromText(String text) {
        Scanner scanner = new Scanner(text);
        scanner.nextLine(); // ignore first line
        List<List<String>> result = new ArrayList<>();
        List<String> cPool = new ArrayList<>();
        result.add(cPool);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("---")) {
                cPool = new ArrayList<>();
                result.add(cPool);
            }

            String[] parts = line.split("\\|", 2);
            if (parts.length >= 2) {
                cPool.add(unescapeJavaString(parts[1]));
            }
        }

        return result;
    }

    public static boolean contains(int[] array, int value) {
        if (array == null) {
            return false;
        }

        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    public static void saveStream(InputStream is, File output) throws IOException {
        byte[] buf = new byte[4096];
        int cnt;
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(output))) {
            while ((cnt = is.read(buf)) > 0) {
                fos.write(buf, 0, cnt);
                fos.flush();
            }
        }
    }

    public static String getDecompilationSkippedComment() {
        return "// " + AppResources.translate("decompilation.skipped");
    }

    public static void appendTimeoutCommentAs2(GraphTextWriter writer, int timeout, int actionCount) {
        writer.appendNoHilight("/*").newLine();
        writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError")).newLine();
        writer.appendNoHilight(" * ").appendNoHilight(MessageFormat.format(AppResources.translate("decompilationError.timeout"), Helper.formatTimeToText(timeout))).newLine();
        if (actionCount > 0) {
            writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError.actionCount") + " " + actionCount).newLine();
        }

        writer.appendNoHilight(" */").newLine();
        writer.appendNoHilight("throw new Error(\"").
                appendNoHilight(AppResources.translate("decompilationError.timeout.description")).
                appendNoHilight("\");").newLine();
    }

    public static void appendTimeoutCommentAs3(GraphTextWriter writer, int timeout, int instructionCount) {
        writer.appendNoHilight("/*").newLine();
        writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError")).newLine();
        writer.appendNoHilight(" * ").appendNoHilight(MessageFormat.format(AppResources.translate("decompilationError.timeout"), Helper.formatTimeToText(timeout))).newLine();
        if (instructionCount > 0) {
            writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError.instructionCount") + " " + instructionCount).newLine();
        }

        writer.appendNoHilight(" */").newLine();
        writer.appendNoHilight("throw new flash.errors.IllegalOperationError(\"").
                appendNoHilight(AppResources.translate("decompilationError.timeout.description")).
                appendNoHilight("\");").newLine();
    }

    public static void appendErrorComment(GraphTextWriter writer, Throwable ex) {
        writer.appendNoHilight("/*").newLine();
        writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError")).newLine();
        writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError.obfuscated")).newLine();
        if (decompilationErrorAdd != null) {
            writer.appendNoHilight(" * ").appendNoHilight(decompilationErrorAdd).newLine();
        }
        writer.appendNoHilight(" * ").appendNoHilight(AppResources.translate("decompilationError.errorType")).
                appendNoHilight(": " + ex.getClass().getSimpleName() + " (" + ex.getMessage() + ")").newLine();
        writer.appendNoHilight(" */").newLine();
        writer.appendNoHilight("throw new flash.errors.IllegalOperationError(\"").
                appendNoHilight(AppResources.translate("decompilationError.error.description")).
                appendNoHilight("\");").newLine();
    }

    public static String escapeHTML(String text) {
        String[] from = new String[]{"&", "<", ">", "\"", "'", "/", "\r\n", "\r", "\n"};
        String[] to = new String[]{"&amp;", "&lt;", "&gt;", "&quot;", "&#x27;", "&#x2F;", "&#xD;", "&#xD;", "&#xD;"};
        for (int i = 0; i < from.length; i++) {
            text = text.replace(from[i], to[i]);
        }
        return text;
    }

    public static boolean containsInvalidXMLCharacter(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!isCharacterValidInXml(ch)) {
                return true;
            }
        }

        return false;
    }

    public static String removeInvalidXMLCharacters(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (isCharacterValidInXml(ch)) {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    private static boolean isCharacterValidInXml(char ch) {
        return (ch > 31 && ch < 0xd800)
                || ch == 9 || ch == 10 || ch == 13
                || (ch >= 0xe000 && ch <= 0xfffd);
    }

    public static Shape imageToShapeOld(BufferedImage image) {
        Area area = new Area();
        Rectangle rectangle = new Rectangle();
        int y1, y2;
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imgData;
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB_PRE || type == BufferedImage.TYPE_INT_RGB) {
            imgData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        } else {
            imgData = image.getRGB(0, 0, width, height, null, 0, width);
        }

        BitSet bs = new BitSet(width * height);
        bs.set(type);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int idx = width * y + x;
                if ((imgData[idx] >>> 24) > 0) {
                    bs.set(idx);
                }
            }
        }

        if (shapeCache.containsKey(bs)) {
            return shapeCache.get(bs);
        }

        for (int x = 0; x < width; x++) {
            y1 = Integer.MAX_VALUE;
            y2 = -1;
            for (int y = 0; y < height; y++) {
                int rgb = imgData[width * y + x];
                rgb = rgb >>> 24;
                if (rgb > 0) {
                    if (y1 == Integer.MAX_VALUE) {
                        y1 = y;
                        y2 = y;
                    }
                    if (y > (y2 + 1)) {
                        rectangle.setBounds(x, y1, 1, y2 - y1 + 1);
                        area.add(new Area(rectangle));
                        y1 = y;
                    }
                    y2 = y;
                }
            }
            if ((y2 - y1) >= 0) {
                rectangle.setBounds(x, y1, 1, y2 - y1 + 1);
                area.add(new Area(rectangle));
            }
        }

        shapeCache.put(bs, area);
        return area;
    }

    public static Shape imageToShape(BufferedImage image) {
        Area area = new Area();
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imgData;
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB_PRE || type == BufferedImage.TYPE_INT_RGB) {
            imgData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        } else {
            imgData = image.getRGB(0, 0, width, height, null, 0, width);
        }

        BitSet bs = new BitSet(width * height);
        bs.set(type);
        int pixelCount = width * height;
        for (int i = 0; i < pixelCount; i++) {
            if ((imgData[i] >>> 24) > 0) {
                bs.set(i);
            }
        }

        if (shapeCache.containsKey(bs)) {
            return shapeCache.get(bs);
        }

        BitSet bsArea = new BitSet(width * height);
        boolean modified = true;

        List<Integer> leftCoordsX = new ArrayList<>();
        List<Integer> leftCoordsY = new ArrayList<>();
        List<Integer> rightCoordsX = new ArrayList<>();
        List<Integer> rightCoordsY = new ArrayList<>();
        while (modified) {
            modified = false;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = width * y + x;
                    if ((imgData[idx] >>> 24) > 0 && !bsArea.get(idx)) {
                        leftCoordsX.clear();
                        leftCoordsY.clear();
                        rightCoordsX.clear();
                        rightCoordsY.clear();
                        int leftX = x;
                        int rightX = findRight(imgData, x, y, width);
                        leftCoordsX.add(leftX);
                        leftCoordsY.add(y);
                        rightCoordsX.add(rightX);
                        rightCoordsY.add(y);
                        setBitSet(bsArea, leftX, rightX, y, width);
                        int y2 = y + 1;
                        while (y2 < height) {
                            leftCoordsX.add(leftX);
                            leftCoordsY.add(y2);
                            rightCoordsX.add(rightX);
                            rightCoordsY.add(y2);

                            int leftX2 = findFirst(imgData, leftX, rightX, y2, width);
                            if (leftX2 == -1) {
                                break;
                            }

                            int rightX2 = findRight(imgData, leftX2, y2, width);

                            if (leftX2 != leftX) {
                                leftCoordsX.add(leftX2);
                                leftCoordsY.add(y2);
                            }

                            if (rightX2 != rightX) {
                                rightCoordsX.add(rightX2);
                                rightCoordsY.add(y2);
                            }

                            leftX = leftX2;
                            rightX = rightX2;

                            setBitSet(bsArea, leftX, rightX, y2, width);
                            y2++;
                        }

                        int cnt = leftCoordsX.size() + rightCoordsX.size();
                        int[] xCoords = new int[cnt];
                        int[] yCoords = new int[cnt];
                        for (int i = 0; i < rightCoordsX.size(); i++) {
                            xCoords[i] = rightCoordsX.get(i);
                            yCoords[i] = rightCoordsY.get(i);
                        }

                        int offset = rightCoordsX.size();
                        for (int i = 0; i < leftCoordsX.size(); i++) {
                            int idx2 = leftCoordsX.size() - i - 1;
                            xCoords[i + offset] = leftCoordsX.get(idx2);
                            yCoords[i + offset] = leftCoordsY.get(idx2);
                        }

                        Area area2 = new Area(new Polygon(xCoords, yCoords, xCoords.length));
                        area.add(area2);
                        modified = true;
                    }
                }
            }
        }

        shapeCache.put(bs, area);
        return area;
    }

    private static void setBitSet(BitSet bitSet, int x1, int x2, int y, int width) {
        int idx = width * y + x1;
        int idx2 = width * y + x2;
        for (; idx < idx2; idx++) {
            bitSet.set(idx);
        }
    }

    private static int findFirst(int[] imgData, int x1, int x2, int y, int width) {
        int idx = width * y + x1;
        if ((imgData[idx] >>> 24) > 0) {
            while (x1 > 0 && (imgData[idx - 1] >>> 24) > 0) {
                x1--;
                idx--;
            }
            return x1;
        }

        int idx2 = width * y + x2;
        for (; idx < idx2; idx++) {
            if ((imgData[idx] >>> 24) > 0) {
                return x1;
            }

            x1++;
        }

        return -1;
    }

    private static int findRight(int[] imgData, int x, int y, int width) {
        int result = x;
        int idx = width * y + x;
        while (result < width && (imgData[idx] >>> 24) > 0) {
            result++;
            idx++;
        }

        return result;
    }

    public static void clearShapeCache() {
        shapeCache.clear();
    }

    public static String byteArrayToBase64String(byte[] data) {
        return DatatypeConverter.printBase64Binary(data);
    }

    public static byte[] base64StringToByteArray(String base64) {
        return DatatypeConverter.parseBase64Binary(base64);
    }

    /**
     * Formats double value (removes .0 from end)
     *
     * @param d
     * @return String
     */
    public static String doubleStr(double d) {
        String ret = Double.toString(d);
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    public static String byteCountStr(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String getNextId(String str, Map<String, Integer> lastIds) {
        return getNextId(str, lastIds, false);
    }

    public static String getNextId(String str, Map<String, Integer> lastIds, boolean addFirst) {
        Integer a = lastIds.get(str);
        if (a == null) {
            lastIds.put(str, 1);
            if (addFirst) {
                str += "_1";
            }

            return str;
        }

        a++;
        lastIds.put(str, a);
        return str + "_" + a;
    }

    public static boolean is64BitJre() {
        String prop = System.getProperty("sun.arch.data.model");
        return prop != null && prop.contains("64");

    }

    public static boolean is64BitOs() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

        if (arch == null) {
            return false;
        }

        return arch.endsWith("64")
                || wow64Arch != null && wow64Arch.endsWith("64");
    }

    public static byte[] downloadUrl(String urlString) throws IOException {
        String proxyAddress = Configuration.updateProxyAddress.get();
        URL url = new URL(urlString);

        URLConnection uc;
        if (proxyAddress != null && !proxyAddress.isEmpty()) {
            int port = 8080;
            if (proxyAddress.contains(":")) {
                String[] parts = proxyAddress.split(":");
                port = Integer.parseInt(parts[1]);
                proxyAddress = parts[0];
            }

            uc = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, port)));
        } else {
            uc = url.openConnection();
        }
        uc.setRequestProperty("User-Agent", ApplicationInfo.shortApplicationVerName);

        uc.connect();

        return Helper.readStream(uc.getInputStream());
    }

    public static String downloadUrlString(String url) throws IOException {
        byte[] data = downloadUrl(url);
        String text = new String(data, Utf8Helper.charset);
        return text;
    }
}
