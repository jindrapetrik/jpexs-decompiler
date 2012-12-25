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

import java.util.List;

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
    * Adds zeros to beginning of the number to fill specified length. Returns as
    * string
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

   public static String indent(int level, String ss) {
      StringBuilder sb = new StringBuilder();
      for (int ii = 0; ii < level * 2; ii++) {
         sb.append(' ');
      }
      sb.append(ss);
      return sb.toString();
   }

   public static String bytesToHexString(byte bytes[], int start) {
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

   public static String bytesToHexString(int maxByteCountInString, byte bytes[], int start) {
      if (bytes.length - start <= maxByteCountInString) {
         return bytesToHexString(bytes, start);
      }
      byte trailingBytes[] = new byte[maxByteCountInString / 2];
      byte headingBytes[] = new byte[maxByteCountInString - trailingBytes.length];
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

   public static String joinStrings(String arr[], String glue) {
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
}
