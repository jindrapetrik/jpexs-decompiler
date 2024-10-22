/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.ecma;

import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * ECMA script functions.
 *
 * @author JPEXS
 */
public class EcmaScript {

    /**
     * Converts value to a number.
     * @param o Value to convert
     * @return Number
     */
    public static Double toNumber(Object o) {
        if (o == null) {
            return 0.0;
        }
        if (o == Undefined.INSTANCE) {
            return Double.NaN;
        }
        if (o == Null.INSTANCE) {
            return 0.0;
        }
        if (o instanceof Boolean) {
            return (Boolean) o ? 1.0 : 0.0;
        }
        if (o instanceof Float) {
            o = (double) (float) (Float) o;
        }
        if (o instanceof Float4) {
            return Double.NaN;
        }        
        if (o instanceof Double) {
            return (Double) o;
        }

        if (o instanceof Long) {
            return (double) (long) (Long) o;
        }
        if (o instanceof Integer) {
            return (double) (int) (Integer) o;
        }
        if (o instanceof String) {
            String str = (String) o;
            if (str.isEmpty()) {
                return 0.0;
            }

            try {
                return Double.valueOf(str);
            } catch (NumberFormatException nfe) {
                return Double.NaN;
            }
        }

        return toNumber(toPrimitive(o, "Number"));
    }

    /**
     * Converts value to a primitive.
     * @param o Value to convert
     * @param prefferedType Preferred type
     * @return Primitive value
     */
    public static Object toPrimitive(Object o, String prefferedType) {
        if (o == Undefined.INSTANCE) {
            return o;
        }
        if (o == Null.INSTANCE) {
            return o;
        }
        if (o == Boolean.TRUE || o == Boolean.FALSE) {
            return o;
        }
        if (o instanceof Number) {
            return o;
        }
        if (o instanceof String) {
            return o;
        }
        if (o instanceof ObjectType) {
            return object_defaultValue((ObjectType) o, prefferedType);
        }
        return Undefined.INSTANCE; //??
    }

    /**
     * Object.get.
     * @param o Object
     * @param p Property
     * @return Value
     */
    public static Object object_get(ObjectType o, String p) {
        //TODO: isDataDescriptor, etc. ECMA 8.12.3
        return object_getProperty(o, p);
    }

    /**
     * Object.getProperty.
     * @param o Object
     * @param p Property
     * @return Value
     */
    public static Object object_getProperty(ObjectType o, String p) {
        //TODO: getownproperty, etc... ECMA 8.12.2
        return o.getAttribute(p);
    }

    /**
     * Object.defaultvalue.
     * @param o Object
     * @return Default value
     */
    public static Object object_defaultValue(ObjectType o) {
        return object_defaultValue(o, "Number");
    }

    /**
     * Object.defaultvalue.
     * @param o Object
     * @param hint Hint
     * @return Default value
     */
    public static Object object_defaultValue(ObjectType o, String hint) {
        switch (hint) {
            case "String":
                //TODO: logic similar to 8.12.8
                return o.call("toString", new ArrayList<>());
            case "Number":
                //TODO: logic similar to 8.12.8
                return o.call("valueOf", new ArrayList<>());
            default:
                return o.toPrimitive();
        }

    }

    /**
     * Converts value to a number. AS2 version.
     * @param o Value to convert
     * @return Number
     */
    public static Double toNumberAs2(Object o) {
        if (o == Null.INSTANCE) {
            return Double.NaN;
        }

        if (o instanceof String && ((String) o).isEmpty()) {
            return Double.NaN;
        }

        return toNumber(o);
    }

    /**
     * Gets type of value.
     * @param o Value
     * @return Type
     */
    public static EcmaType type(Object o) {
        if (o == null) {
            return EcmaType.NULL;
        }
        if (o.getClass() == String.class) {
            return EcmaType.STRING;
        }
        if (o.getClass() == Integer.class) {
            return EcmaType.NUMBER;
        }
        if (o.getClass() == Double.class) {
            return EcmaType.NUMBER;
        }
        if (o.getClass() == Long.class) {
            return EcmaType.NUMBER;
        }
        if (o.getClass() == Boolean.class) {
            return EcmaType.BOOLEAN;
        }
        if (o.getClass() == Null.class) {
            return EcmaType.NULL;
        }
        if (o.getClass() == Undefined.class) {
            return EcmaType.UNDEFINED;
        }
        if (o.getClass() == Float.class) {
            return EcmaType.FLOAT;
        }
        if (o.getClass() == Float4.class) {
            return EcmaType.FLOAT4;
        }
        return EcmaType.OBJECT;
    }

    /**
     * Converts value to a type string.
     * @param o Value
     * @return Type string
     */
    public static String typeString(Object o) {
        EcmaType type = EcmaScript.type(o);
        String typeStr;
        switch (type) {
            case STRING:
                typeStr = "string";
                break;
            case BOOLEAN:
                typeStr = "boolean";
                break;
            case NUMBER:
                typeStr = "number";
                break;
            case FLOAT:
                typeStr = "float";
                break;
            case FLOAT4:
                typeStr = "float4";
                break;
            case OBJECT:
                typeStr = "object";
                break;
            case UNDEFINED:
                typeStr = "undefined";
                break;
            case NULL:
                // note: null is object in AS3
                typeStr = "object";
                break;
            default:
                // todo: function,movieclip,xml
                typeStr = "object";
                break;
        }

        return typeStr;
    }

    /**
     * Compares two values.
     * @param x Value 1
     * @param y Value 2
     * @return Comparison result
     */
    public static Object compare(Object x, Object y) {
        return compare(x, y, false);
    }

    /**
     * Compares two values.
     * @param x Value 1
     * @param y Value 2
     * @param as2 AS2 mode
     * @return Comparison result
     */
    public static Object compare(Object x, Object y, boolean as2) {
        Object px = x;
        Object py = y;
        /*if (leftFirst) {
         px = x;  //toPrimitive
         py = y;  //toPrimitive
         } else {
         py = y;  //toPrimitive
         px = x;  //toPrimitive
         }*/
        if (type(px) != EcmaType.STRING || type(py) != EcmaType.STRING) {
            Double nx = as2 ? toNumberAs2(px) : toNumber(px);
            Double ny = as2 ? toNumberAs2(py) : toNumber(py);
            if (nx.isNaN() || ny.isNaN()) {
                return Undefined.INSTANCE;
            }
            if (nx.compareTo(ny) == 0) {
                return 0;
            }
            if (Double.compare(nx, -0.0) == 0 && Double.compare(ny, 0.0) == 0) {
                return 0;
            }
            if (Double.compare(nx, 0.0) == 0 && Double.compare(ny, -0.0) == 0) {
                return 0;
            }
            if (nx.isInfinite() && nx > 0) {
                return 1;
            }
            if (ny.isInfinite() && ny > 0) {
                return -1;
            }
            if (nx.isInfinite() && nx < 0) {
                return 1;
            }
            if (ny.isInfinite() && ny < 0) {
                return -1;
            }
            if (nx.compareTo(ny) < 0) {
                return -1;
            }
            return 1;
        } else { //Both are STRING
            String sx = (String) px;
            String sy = (String) py;

            if (sx.equals(sy)) {
                return 0;
            }
            if (as2) {
                // in AS2 an empty string is greater than a non-empty string...
                if (sx.isEmpty()) {
                    return 1;
                }
                if (sy.isEmpty()) {
                    return -1;
                }
            }
            if (sx.startsWith(sy)) {
                return 1;
            }
            if (sy.startsWith(sx)) {
                return -1;
            }
            int len = sx.length() > sy.length() ? sx.length() : sy.length();
            for (int k = 0; k < len; k++) {
                int m = 0;
                int n = 0;
                if (sx.length() > k) {
                    m = sx.charAt(k);
                }
                if (sy.length() > k) {
                    n = sy.charAt(k);
                }
                if (m != n) {
                    if (m < n) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            return 0;
        }
    }

    /**
     * Compares two values strictly.
     * @param x Value 1
     * @param y Value 2
     * @return Comparison result
     */
    public static boolean strictEquals(Object x, Object y) {
        return strictEquals(false, x, y);
    }

    /**
     * Compares two values strictly.
     * @param as2 AS2 mode
     * @param x Value 1
     * @param y Value 2
     * @return Comparison result
     */
    public static boolean strictEquals(boolean as2, Object x, Object y) {
        if (type(x) != type(y)) {
            return false;
        }

        return equals(as2, x, y);
    }

    /**
     * Checks if two values are equal.
     * @param x Value 1
     * @param y Value 2
     * @return True if values are equal
     */
    public static boolean equals(Object x, Object y) {
        return equals(false, x, y);
    }

    /**
     * Checks if two values are equal.
     * @param as2 AS2 mode
     * @param x Value 1
     * @param y Value 2
     * @return True if values are equal
     */
    public static boolean equals(boolean as2, Object x, Object y) {
        EcmaType typeX = type(x);
        EcmaType typeY = type(y);
        if (typeX == typeY) {
            if (typeX == null) {
                return true;
            }
            if (typeX == EcmaType.NULL) {
                return true;
            }
            if (typeX == EcmaType.UNDEFINED) {
                return true;
            }
            if (typeX == EcmaType.NUMBER) {
                if (x instanceof Integer) {
                    x = Double.valueOf((Integer) x);
                }
                if (x instanceof Long) {
                    x = Double.valueOf((Long) x);
                }
                if (y instanceof Integer) {
                    y = Double.valueOf((Integer) y);
                }
                if (y instanceof Long) {
                    y = Double.valueOf((Long) y);
                }
                if (((Double) x).isNaN()) {
                    return false;
                }
                if (((Double) y).isNaN()) {
                    return false;
                }
                if (((Double) x).compareTo((Double) y) == 0) {
                    return true;
                }
                if ((Double.compare((Double) x, -0.0) == 0) && (Double.compare((Double) y, 0.0) == 0)) {
                    return true;
                }
                if ((Double.compare((Double) x, 0.0) == 0) && (Double.compare((Double) y, -0.0) == 0)) {
                    return true;
                }
                return false;
            }
            if (typeX == EcmaType.STRING) {
                return ((String) x).equals((String) y);
            }
            if (typeX == EcmaType.BOOLEAN) {
                return x == y;
            }
            return x == y;
        }
        if ((typeX == EcmaType.NULL) && (typeY == EcmaType.UNDEFINED)) {
            return true;
        }
        if ((typeX == EcmaType.UNDEFINED) && (typeY == EcmaType.NULL)) {
            return true;
        }

        if ((typeX == EcmaType.NUMBER) && (typeY == EcmaType.STRING)) {
            return equals(as2, x, as2 ? toNumberAs2(y) : toNumber(y));
        }
        if ((typeX == EcmaType.STRING) && (typeY == EcmaType.NUMBER)) {
            return equals(as2, as2 ? toNumberAs2(x) : toNumber(x), y);
        }
        if (typeX == EcmaType.BOOLEAN) {
            return equals(as2, as2 ? toNumberAs2(x) : toNumber(x), y);
        }
        if (typeY == EcmaType.BOOLEAN) {
            return equals(as2, x, as2 ? toNumberAs2(y) : toNumber(y));
        }
        if (typeX == EcmaType.STRING || typeX == EcmaType.NUMBER) {
            //y is object
            //return ecmaEquals(ecmaToPrimitive(x), y);
        }
        if (typeY == EcmaType.STRING || typeY == EcmaType.NUMBER) {
            //x is object
            //return ecmaEquals(x, ecmaToPrimitive(y));
        }
        return false;
    }

    /**
     * Converts value to a boolean.
     * @param o Value
     * @return Boolean
     */
    public static boolean toBoolean(Object o) {
        if (o == null) {
            return false;
        }
        if (o == Undefined.INSTANCE) {
            return false;
        }
        if (o == Null.INSTANCE) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Long) {
            return ((Long) o) != 0;
        }
        if (o instanceof Integer) {
            return ((Integer) o) != 0;
        }
        if (o instanceof Float) {
            o = (double) (float) (Float) o;
        }

        if (o instanceof Double) {
            Double d = (Double) o;
            if (d.isNaN()) {
                return false;
            }
            if (Double.compare(d, 0) == 0) {
                return false;
            }
            return true;
        }
        if (o instanceof String) {
            String s = (String) o;
            return !s.isEmpty();
        }
        return true; //other Object
    }

    /**
     * Converts value to an int32.
     * @param o Value
     * @return Int32
     */
    public static int toInt32(Object o) {
        return (int) toUint32(o);
    }

    /**
     * Converts value to an uint32.
     * @param o Value
     * @return Uint32
     */
    public static long toUint32(Object o) {
        Double n = toNumber(o);
        if (n.isNaN()) {
            return 0L;
        }
        if (Double.compare(n, 0.0) == 0) {
            return 0L;
        }
        if (Double.compare(n, -0.0) == 0) {
            return 0L;
        }
        if (Double.isInfinite(n)) {
            return 0L;
        }
        long posInt = (long) (double) (Math.signum(n) * Math.floor(Math.abs(n)));
        posInt &= 0xffffffffL;
        return posInt;
    }

    /**
     * Converts value to string.
     * @param o Value
     * @return String
     */
    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }

        if (o instanceof Number) {
            // http://www.ecma-international.org/ecma-262/5.1/#sec-9.8.1
            Number n = (Number) o;
            double dn = n.doubleValue();
            if ((int) dn == dn) { //isRepresentableAsInt
                return Integer.toString((int) dn);
            }

            if (dn == Double.POSITIVE_INFINITY) {
                return "Infinity";
            }

            if (dn == Double.NEGATIVE_INFINITY) {
                return "-Infinity";
            }

            if (Double.isNaN(dn)) {
                return "NaN";
            }
            return EcmaNumberToString.stringFor(dn);
        }

        return o.toString();
    }

    /**
     * Converts value to string.
     * @param o Value
     * @param constantPool Constant pool
     * @return String
     */
    public static String toString(Object o, List<String> constantPool) {
        if (o instanceof ConstantIndex) {
            int index = ((ConstantIndex) o).index;
            if (constantPool != null && index < constantPool.size()) {
                return constantPool.get(index);
            }
        }
        return toString(o);
    }

    /**
     * Parses float value.
     * @param string String
     * @return Float value
     */
    public static Double parseFloat(Object string) {
        String inputString = toString(string);
        int startPos = 0;
        String trimmedString = "";
        for (; startPos < inputString.length(); startPos++) {
            char c = inputString.charAt(startPos);
            if (!Character.isWhitespace(c)) {
                trimmedString = inputString.substring(startPos);
                break;
            }
        }
        try {
            return Double.parseDouble(trimmedString); //Is this the same?
        } catch (NumberFormatException nfe) {
            return Double.NaN;
        }

    }

    /**
     * Checks if value is NaN.
     * @param number Value
     * @return True if value is NaN
     */
    public static Boolean isNaN(Object number) {
        return Double.isNaN(toNumber(number));
    }

    /**
     * Checks if value is finite.
     * @param number Value
     * @return True if value is finite
     */
    public static Boolean isFinite(Object number) {
        return Double.isFinite(toNumber(number));
    }

    /**
     * Parses int value.
     * @param string String
     * @param radix Radix
     * @return Int value
     */
    public static Object parseInt(Object string, Object radix) {
        String inputString = toString(string);
        int startPos = 0;
        String s = "";
        for (; startPos < inputString.length(); startPos++) {
            char c = inputString.charAt(startPos);
            if (!Character.isWhitespace(c)) {
                s = inputString.substring(startPos);
                break;
            }
        }
        int sign = 1;
        if (!s.isEmpty() && s.charAt(0) == '-') {
            sign = -1;
        }
        if (!s.isEmpty() && (s.charAt(0) == '+' || s.charAt(0) == '-')) {
            s = s.substring(1);
        }
        int r = toInt32(radix);
        boolean stripPrefix = true;
        if (r != 0) {
            if (r < 2 || r > 36) {
                return Double.NaN;
            }
            if (r != 16) {
                stripPrefix = false;
            }
        } else {
            r = 10;
        }
        if (stripPrefix) {
            if (s.length() >= 2) {
                if (s.substring(0, 2).toLowerCase().equals("0x")) {
                    s = s.substring(2);
                    r = 16;
                }
            }
        }

        String allDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String allowedDigits = allDigits.substring(0, r);
        String z = s;
        for (int i = 0; i < s.length(); i++) {
            if (("" + s.charAt(i)).matches("[" + allowedDigits + "]")) {
                if (i == 0) {
                    z = "";
                    break;
                }
                z = s.substring(0, i);
                break;
            }
        }
        if (z.isEmpty()) {
            return Double.NaN;
        }
        Long number = Long.parseLong(z, r);
        return sign * number;
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static String simpleCustomEncode(String input, String additionalValidChars) {
        String alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String num = "0123456789";
        String alphaCases = alphas + alphas.toLowerCase();
        String alphanum = alphaCases + num;
        return customEncode(input, alphanum + additionalValidChars);
    }

    private static String simpleCustomDecode(String input, String additionalValidChars) {
        String alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String num = "0123456789";
        String alphaCases = alphas + alphas.toLowerCase();
        String alphanum = alphaCases + num;
        return customDecode(input, alphanum + additionalValidChars);
    }

    private static String customEncode(String input, String validChars) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (!validChars.contains("" + ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static String customDecode(String input, String reservedSet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            String s;
            if (ch == '%' && i + 2 < input.length()) {
                try {
                    int k = i;
                    int b = Integer.parseInt(input.substring(k + 1, k + 2 + 1), 16);
                    int msb = (b >> 15) & 1;
                    if (msb == 0) {
                        char c = (char) b;
                        if (!reservedSet.contains("" + c)) {
                            baos.write(c);
                        } else {
                            baos.write(Utf8Helper.getBytes(input.substring(k, k + 3)));
                        }
                    } else {
                        //here continues some multibyte character
                        //FIXME: is this working?
                        for (; msb == 1 && k < input.length() && input.charAt(k) == '%'; k += 3) {
                            b = Integer.parseInt(input.substring(k + 1, k + 2 + 1), 16);
                            msb = (b >> 15) & 1;
                            baos.write(b);
                        }
                        //throw error is msb=1
                    }
                } catch (NumberFormatException nfe) {
                    //throw URIEx
                } catch (IOException ex) {
                    //ignored
                }
            }
        }
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    /**
     * Encodes URI component.
     * @param s String
     * @return Encoded URI component
     */
    public static String encodeUriComponent(Object s) {
        return simpleCustomEncode(toString(s), "-_.!~*'()");
    }

    /**
     * Encodes URI.
     * @param s String
     * @return Encoded URI
     */
    public static String encodeUri(Object s) {
        return simpleCustomEncode(toString(s), ";/?:@&=+$,#-_.!~*'()");
    }

    /**
     * Escapes string.
     * @param s String
     * @return Escaped string
     */
    public static String escape(Object s) {
        return simpleCustomEncode(toString(s), "@-_.*+/");
    }

    /**
     * Decodes URI component.
     * @param s String
     * @return Decoded URI component
     */
    public static String decodeUriComponent(Object s) {
        return simpleCustomDecode(toString(s), "-_.!~*'()");
    }

    /**
     * Decodes URI.
     * @param s String
     * @return Decoded URI
     */
    public static String decodeUri(Object s) {
        return simpleCustomDecode(toString(s), ";/?:@&=+$,#-_.!~*'()");
    }

    /**
     * Unescapes string.
     * @param s String
     * @return Unescaped string
     */
    public static String unescape(Object s) {
        return simpleCustomDecode(toString(s), "@-_.*+/");
    }
}
