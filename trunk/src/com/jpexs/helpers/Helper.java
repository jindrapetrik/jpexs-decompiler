/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.helpers;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.Freed;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with helper method
 *
 * @author JPEXS, Paolo Cancedda
 */
public class Helper {

    /**
     * Converts array of int values to string
     *
     * @param array Array of int values
     * @return String representation of the array
     */
    public static String intArrToString(int[] array) {
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                s += ",";
            }
            s += array[i];
        }
        s += "]";
        return s;
    }

    /**
     * Converts array of byte values to string
     *
     * @param array Array of byte values
     * @return String representation of the array
     */
    public static String byteArrToString(byte[] array) {
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                s += " ";
            }
            s += padZeros(Integer.toHexString(array[i] & 0xff), 2);
        }
        s += "]";
        return s;
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
     * Formats specified address to four numbers xxxx
     *
     * @param number Address to format
     * @return String representation of the address
     */
    public static String formatAddress(long number) {
        if (Configuration.decimalAddress.get()) {
            return padZeros(Long.toString(number), 4);
        }
        return padZeros(Long.toHexString(number), 4);
    }

    /**
     * Adds space to text to fill specified width
     *
     * @param text Text to add spaces to
     * @param width New width
     * @return Text with appended spaces
     */
    public static String padSpaceRight(String text, int width) {
        int oldLen = text.length();
        for (int i = oldLen; i < width; i++) {
            text += " ";
        }
        return text;
    }

    /**
     * Escapes string by adding backslashes
     *
     * @param s String to escape
     * @return Escaped string
     */
    public static String escapeString(String s) {
        String ret = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret += "\\n";
            } else if (c == '\r') {
                ret += "\\r";
            } else if (c == '\t') {
                ret += "\\t";
            } else if (c == '\b') {
                ret += "\\b";
            } else if (c == '\t') {
                ret += "\\t";
            } else if (c == '\f') {
                ret += "\\f";
            } else if (c == '\\') {
                ret += "\\\\";
            } else if (c == '"') {
                ret += "\\\"";
            } else if (c == '\'') {
                ret += "\\'";
            } else {
                ret += c;
            }

        }
        return ret;
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
            indentStr += HilightedTextWriter.INDENT_STRING;
        }
        int indentLength = indentStr.length();
        for (int i = 0; i < prefixLineCount; i++) {
            scanner.nextLine(); // ignore line
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith(indentStr)) {
                sb.append(line.substring(indentLength)).append(HilightedTextWriter.NEW_LINE);
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
        String ret = "" + number;
        while (ret.length() < length) {
            ret = "0" + ret;
        }
        return ret;
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

    public static String joinStrings(List<String> arr, String glue) {
        String ret = "";
        boolean first = true;
        for (String s : arr) {
            if (!first) {
                ret += glue;
            } else {
                first = false;
            }
            ret += s;
        }
        return ret;
    }

    public static String joinStrings(String[] arr, String glue) {
        String ret = "";
        boolean first = true;
        for (String s : arr) {
            if (!first) {
                ret += glue;
            } else {
                first = false;
            }
            ret += s;
        }
        return ret;
    }

    public static Object deepCopy(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(o);
                oos.flush();
            }
            Object copy;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                copy = ois.readObject();
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
                int cnt = 0;
                while ((cnt = fis.read(buf)) > 0) {
                    baos.write(buf, 0, cnt);
                }
            } catch (IOException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return baos.toByteArray();
    }

    public static void writeFile(String file, byte[]... data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (byte[] d : data) {
                fos.write(d);
            }
        } catch (IOException ex) {
            //ignore
        }
    }

    //public static String stripComments(String str) {
    //    return str.replaceAll("<ffdec:hex>[^\r\n]*</ffdec:hex>\r?\n", "");
    //}

    //public static String hexToComments(String str) {
    //    return str.replaceAll("<ffdec:hex>([^\r\n]*)</ffdec:hex>(\r?\n)", "; $1$2");
    //}

    public static String stackToString(Stack<GraphTargetItem> stack, LocalData localData) throws InterruptedException {
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
    private static BitSet fileNameInvalidChars;
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
        str = sb.toString();
        if (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1) + "%20";
        }
        if (str.endsWith(".")) {
            str = str.substring(0, str.length() - 1) + "%2E";
        }
        str = "." + str + ".";
        for (String inv : invalidFilenamesParts) {
            str = Pattern.compile("\\." + Pattern.quote(inv) + "\\.", Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("._" + inv + ".");
        }
        str = str.substring(1, str.length() - 1); //remove dots
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
            try {
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v != null) {
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
                    f.set(obj, null);
                }
            } catch (UnsupportedOperationException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
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
        if (timeH > 0) {
            timeStr += timeH + (timeH > 1 ? " hours" : " hour");
        }
        if (timeM > 0) {
            if (timeStr.length() > 0) {
                timeStr += " and ";
            }
            timeStr += timeM + (timeM > 1 ? " minutes" : " minute");
        }
        if (timeS > 0) {
            if (timeStr.length() > 0) {
                timeStr += " and ";
            }
            timeStr += timeS + (timeS > 1 ? " seconds" : " second");
        }

        // (currently) used only in log, so no localization is required
        return timeStr;
    }

    public static GraphTextWriter byteArrayToHexWithHeader(GraphTextWriter writer, byte[] data) {
        writer.appendNoHilight("#hexdata").newLine().newLine();
        return byteArrayToHex(writer, data, 8, 8, false);
    }
    
    public static GraphTextWriter byteArrayToHex(GraphTextWriter writer, byte[] data, int bytesPerRow, int groupSize, boolean addChars) {

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

        int rowCount = data.length / bytesPerRow;
        if (data.length % bytesPerRow > 0) {
            rowCount++;
        }
        
        for (int row = 0; row < rowCount; row++) {
            if (row > 0) {
                writer.newLine();
            }
            
            for (int i = 0; i < bytesPerRow; i++) {
                int idx = row * bytesPerRow + i;
                if (data.length > idx) {
                    if (i> 0 && i % groupSize == 0) {
                        writer.appendNoHilight(" ");
                    }
                    writer.appendNoHilight(String.format("%02x ", data[idx]));
                } else {
                    if (addChars) {
                        if (i> 0 && i % groupSize == 0) {
                            writer.appendNoHilight(" ");
                        }
                        writer.appendNoHilight("   ");
                    }
                }
            }
            
            if (addChars) {
                writer.appendNoHilight("  ");
                for (int i = 0; i < bytesPerRow; i++) {
                    int idx = row * bytesPerRow + i;
                    if (data.length == idx) {
                        break;
                    }
                    if (i> 0 && i % groupSize == 0) {
                        writer.appendNoHilight(" ");
                    }
                    byte ch = data[idx];
                    if (ch >=0 && ch < 32) {
                        ch = '.';
                    }
                    writer.appendNoHilight((char) ch + "");
                }
            }
        }

        writer.newLine();
        return writer;
    }
    
    public static String byteArrayToHex(byte[] data, int bytesPerRow) {
        HilightedTextWriter writer = new HilightedTextWriter(false);
        byteArrayToHex(writer, data, bytesPerRow, 8, true);
        return writer.toString();
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

    public static boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    public static void saveStream(InputStream is, File output) throws IOException {
        byte[] buf = new byte[1024];
        int cnt;
        try (FileOutputStream fos = new FileOutputStream(output)) {
            while ((cnt = is.read(buf)) > 0) {
                fos.write(buf, 0, cnt);
                fos.flush();
            }
        }
    }
}
