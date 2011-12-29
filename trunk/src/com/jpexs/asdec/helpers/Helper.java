/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.helpers;

/**
 * Class with helper method
 *
 * @author JPEXS
 */
public class Helper {
    /**
     * Converts array of int values to string
     *
     * @param array Array of int values
     * @return String representation of the array
     */
    public static String intArrToString(int array[]) {
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                s += ",";
            }
            s = s + array[i];
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
    public static String byteArrToString(byte array[]) {
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                s += " ";
            }
            s = s + padZeros(Integer.toHexString(array[i] & 0xff), 2);
        }
        s += "]";
        return s;
    }

    /**
     * Adds zeros to beginning of the number to fill specified length. Returns as string
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
        return padZeros(Long.toHexString(number), 4);
    }

    /**
     * Adds space to text to fill specified width
     *
     * @param text  Text to add spaces to
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
            if (c == '\n') ret += "\\n";
            else if (c == '\r') ret += "\\r";
            else if (c == '\t') ret += "\\t";
            else if (c == '\b') ret += "\\b";
            else if (c == '\t') ret += "\\t";
            else if (c == '\f') ret += "\\f";
            else if (c == '\\') ret += "\\\\";
            else if (c == '"') ret += "\\\"";
            else if (c == '\'') ret += "\\'";
            else ret += c;

        }
        return ret;
    }
}
