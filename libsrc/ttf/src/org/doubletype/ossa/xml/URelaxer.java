/*
 * The Relaxer artifact
 * Copyright (c) 2000-2004, ASAMI Tomoharu, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.doubletype.ossa.xml;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.w3c.dom.*;

/**
 * URelaxer
 *
 * @since   Jan. 19, 2000
 * @version Mar. 12, 2004
 * @author  ASAMI, Tomoharu (asami@relaxer.org)
 */
public final class URelaxer {
    // String type
    public static String getString(String value) {
        return value;
    }

    public static String getString(String[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(List values) {
        if (values == null) {
            return null;
        }
        int size = values.size();
        StringBuffer buffer = new StringBuffer();
        if (size > 0) {
            buffer.append(getString(values.get(0)));
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(getString(values.get(i)));
            }
        }
        return new String(buffer);
    }

    public static String[] getStringArrayObject(Object value) {
        if (value instanceof String[]) {
            return (String[])value;
        } else if (value instanceof Object[]) {
            Object[] values = (Object[])value;
            String[] result = new String[values.length];
            for (int i = 0;i < values.length;i++) {
                result[i] = values[i].toString();
            }
            return result;
        } else {
            return getStringArray(value.toString());
        }
    }

    public static String[] getStringArray(String value) {
        return getStringList(value);
    }

    public static String[] getStringList(String value) { // XXX : Array and List?
        StringTokenizer st = new StringTokenizer(value);
        String[] list = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            list[i++] = st.nextToken();
        }
        return list;
    }

    public static List<String> makeStringList(String string) {
        List<String> list = new ArrayList<>();
        if (string != null) {
            StringTokenizer st = new StringTokenizer(string);
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
        return list;
    }

    public static String escape(String string) {
        if (string.indexOf('<') == -1 &&
            string.indexOf('>') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('"') == -1 &&
            string.indexOf('\'') == -1 &&
            string.indexOf('\r') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '>') {
                buffer.append("&gt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '"') {
                buffer.append("&quot;");
            } else if (c == '\'') {
                buffer.append("&apos;");
            } else if (c == '\r') {
                buffer.append("&#xD;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeEntityQuot(String string) {
        if (string.indexOf('%') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('"') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '%') {
                buffer.append("&---;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '"') {
                buffer.append("&quot;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeEntityApos(String string) {
        if (string.indexOf('%') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('\'') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '%') {
                buffer.append("&#x25;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeAttrQuot(String string) {
        if (string.indexOf('<') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('"') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '"') {
                buffer.append("&quot;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeAttrApos(String string) {
        if (string.indexOf('<') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('\'') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeSystemQuot(String string) {
        if (string.indexOf('"') == -1) {
            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '"') {
                buffer.append("&quot;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeSystemApos(String string) {
        if (string.indexOf('\'') == -1) {
            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String escapeCharData(String string) {
        if (string == null) {
            return "";
        }
        if (string.indexOf('<') == -1 &&
            string.indexOf('&') == -1 &&
            string.indexOf('>') == -1 &&
            string.indexOf('\r') == -1) {

            return string;
        }
        StringBuffer buffer = new StringBuffer();
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '>') {
                buffer.append("&gt;");
            } else if (c == '\r') {
                buffer.append("&#xD;");
            } else {
                buffer.append(c);
            }
        }
        return new String(buffer);
    }

    public static String getElementPropertyAsString(
        Element element
    ) {
        return element2Text(element);
    }

    public static List<String> getElementPropertyAsStringDataList(
        Element element
    ) {
        return makeStringList(element2Text(element));
    }

    public static String getElementPropertyAsString(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Text(property);
        return text;
    }

    public static List getElementPropertyAsStringDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Text(property);
        return makeStringList(text);
    }

    public static List<String> getElementPropertyAsStringList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<String> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            list.add(element2Text(nodes[i]));
        }
        return list;
    }

    public static List<List<String>> getElementPropertyAsStringListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<String>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            list.add(makeStringList(element2Text(nodes[i])));
        }
        return list;
    }

    public static String getElementPropertyAsStringByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return element2Text(property);
    }

    public static List getElementPropertyAsStringDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return makeStringList(element2Text(property));
    }

    public static List<String> getElementPropertyAsStringListByStack(
        RStack stack,
        String name
    ) {
        List<String> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            list.add(element2Text(property));
        }
        return list;
    }

    public static List<List<String>> getElementPropertyAsStringListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<String>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            list.add(makeStringList(element2Text(property)));
        }
        return list;
    }

    public static String getAttributePropertyAsString(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return value;
        }
    }

    public static List getAttributePropertyAsStringList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        return makeStringList(value);
    }

    public static void setElementPropertyByString(
        Element element,
        String value
    ) {
        Document doc = element.getOwnerDocument();
        if (value != null) {
            Text text = doc.createTextNode(value);
            element.appendChild(text);
        }
    }

    public static void setElementPropertyByStringDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        if (value != null) {
            Text text = doc.createTextNode(value);
            element.appendChild(text);
        }
    }

    public static void setElementPropertyByString(
        Element element,
        String name,
        String value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByStringDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByStringList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByStringListDataList(
        Element element,
        String name,
        List values
    ) {
        String value = getString(values);
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setAttributePropertyByString(
        Element element,
        String name,
        String value
    ) {
        if (value == null) { // by horst.fiedler@tifff.com
            if (getAttribute(element, name) != null) {
                element.removeAttribute(name);
            }
        } else {
            element.setAttribute(name, value);
        }
    }

    public static void setAttributePropertyByStringList(
        Element element,
        String name,
        List values
    ) {
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // boolean type
    public static String getString(boolean value) {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    public static String getString(Boolean value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(boolean[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            if (values[0]) {
                buffer.append("true");
            } else {
                buffer.append("false");
            }
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                if (values[i]) {
                    buffer.append("true");
                } else {
                    buffer.append("false");
                }
            }
        }
        return new String(buffer);
    }

    public static String getString(Boolean[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static boolean getBooleanValue(Object value) {
        Boolean object;
        if (value instanceof Boolean) {
            object = (Boolean)value;
            return object.booleanValue();
        } else {
            String string = value.toString();
            return "true".equals(string) || "1".equals(string);
        }
    }

    public static Boolean getBooleanObject(String text) {
        switch (text) {
            case "true":
            case "1":
                return Boolean.TRUE;
            case "false":
            case "0":
                return Boolean.FALSE;
            default:
                return _invalidBooleanObject(text);
        }
    }

    public static Boolean[] getBooleanObjectList(String text) {
        String[] strings = getStringList(text);
        Boolean[] list = new Boolean[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getBooleanObject(strings[i]);
        }
        return list;
    }

    public static boolean getElementPropertyAsBoolean(
        Element element
    ) {
        String text = element2Data(element);
        return "true".equals(text) || "1".equals(text);
    }

    public static Boolean getElementPropertyAsBooleanObject(
        Element element
    ) {
        String text = element2Data(element);
        return getBooleanObject(text);
    }

// g1u
    public static List<Boolean> getElementPropertyAsBooleanDataList(
        Element element
    ) {
        List<Boolean> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getBooleanObject(strings.get(i)));
        }
        return result;
    }

    public static boolean getElementPropertyAsBoolean(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBoolean(property);
    }

// g2a
    public static List getElementPropertyAsBooleanDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBooleanDataList(property);
    }

    public static List<Boolean> getElementPropertyAsBooleanList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Boolean> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Boolean value = getElementPropertyAsBooleanObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsBooleanListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Boolean>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Boolean> values = getElementPropertyAsBooleanDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Boolean getElementPropertyAsBooleanByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBooleanObject(property);
    }

// g4a
    public static List getElementPropertyAsBooleanDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBooleanDataList(property);
    }

    public static List<Boolean> getElementPropertyAsBooleanListByStack(
        RStack stack,
        String name
    ) {
        List<Boolean> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Boolean value = getElementPropertyAsBooleanObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Boolean>> getElementPropertyAsBooleanListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Boolean>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Boolean> value = getElementPropertyAsBooleanDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static boolean getAttributePropertyAsBoolean(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return false;
        }
        return "true".equals(value);
    }

    public static Boolean getAttributePropertyAsBooleanObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        return getBooleanObject(value);
    }

    public static List<Boolean> getAttributePropertyAsBooleanList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Boolean> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            if ("true".equals(data) || "1".equals(data)) {
                result.add(Boolean.TRUE);
            } else {
                result.add(Boolean.FALSE);
            }
        }
        return result;
    }

    public static void setElementPropertyByBoolean(
        Element element,
        String name,
        boolean value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(getString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByBoolean(
        Element element,
        String name,
        Boolean value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1
    public static void setElementPropertyByBooleanDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByBooleanList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByBooleanListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString()); // XXX
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByBoolean(
        Element element,
        boolean value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByBoolean(
        Element element,
        Boolean value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(value));
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByBooleanDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByBoolean(
        Element element,
        String name,
        boolean value
    ) {
        element.setAttribute(name, getString(value));
    }

    public static void setAttributePropertyByBoolean(
        Element element,
        String name,
        Boolean value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByBooleanList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // byte type
    public static String getString(byte value) {
        return Byte.toString(value);
    }

    public static String getString(Byte value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(byte[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Byte[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static byte getByteValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Byte(value.toString());
            }
            return object.byteValue();
        } catch (Exception e) {
            return _invalidByteValue(e);
        }
    }

    public static Byte getByteObject(String text) {
        try {
            return new Byte(text);
        } catch (Exception e) {
            return _invalidByteObject(e);
        }
    }

    public static Byte[] getByteObjectList(String text) {
        String[] strings = getStringList(text);
        Byte[] list = new Byte[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getByteObject(strings[i]);
        }
        return list;
    }

    public static byte getElementPropertyAsByte(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Byte.parseByte(text);
        } catch (Exception e) {
            return _invalidByteValue(e);
        }
    }

    public static Byte getElementPropertyAsByteObject(
        Element element
    ) {
        String text = element2Data(element);
        return getByteObject(text);
    }

// g1u
    public static List<Byte> getElementPropertyAsByteDataList(
        Element element
    ) {
        List<Byte> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getByteObject(strings.get(i)));
        }
        return result;
    }

    public static byte getElementPropertyAsByte(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Byte.parseByte(text);
        } catch (Exception e) {
            return _invalidByteValue(e);
        }
    }

// g2a
    public static List<Byte> getElementPropertyAsByteDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsByteDataList(property);
    }

    public static List getElementPropertyAsByteList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Byte> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Byte value = getElementPropertyAsByteObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List<List<Byte>> getElementPropertyAsByteListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Byte>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Byte> values = getElementPropertyAsByteDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Byte getElementPropertyAsByteByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsByteObject(property);
    }

// g4a
    public static List getElementPropertyAsByteDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsByteDataList(property);
    }

    public static List<Byte> getElementPropertyAsByteListByStack(
        RStack stack,
        String name
    ) {
        List<Byte> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Byte value = getElementPropertyAsByteObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Byte>> getElementPropertyAsByteListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Byte>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Byte> value = getElementPropertyAsByteDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static byte getAttributePropertyAsByte(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Byte.parseByte(value);
            }
        } catch (Exception e) {
            return _invalidByteValue(e);
        }
    }

    public static Byte getAttributePropertyAsByteObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getByteObject(value);
        }
    }

    public static List<Byte> getAttributePropertyAsByteList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Byte> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getByteObject(data));
        }
        return result;
    }

    public static void setElementPropertyByByte(
        Element element,
        String name,
        byte value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Byte.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByByte(
        Element element,
        String name,
        Byte value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByByteDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByByteList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByByteListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByByte(
        Element element,
        byte value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Byte.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByByte(
        Element element,
        Byte value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByByteDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByByte(
        Element element,
        String name,
        byte value
    ) {
        element.setAttribute(name, Byte.toString(value));
    }

    public static void setAttributePropertyByByte(
        Element element,
        String name,
        Byte value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByByteList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // short type
    public static String getString(short value) {
        return Short.toString(value);
    }

    public static String getString(Short value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(short[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Short[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static short getShortValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Short(value.toString());
            }
            return object.shortValue();
        } catch (Exception e) {
            return _invalidShortValue(e);
        }
    }

    public static Short getShortObject(String text) {
        try {
            return new Short(text);
        } catch (Exception e) {
            return _invalidShortObject(e);
        }
    }

    public static Short[] getShortObjectList(String text) {
        String[] strings = getStringList(text);
        Short[] list = new Short[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getShortObject(strings[i]);
        }
        return list;
    }

    public static short getElementPropertyAsShort(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Short.parseShort(text);
        } catch (Exception e) {
            return _invalidShortValue(e);
        }
    }

    public static Short getElementPropertyAsShortObject(
        Element element
    ) {
        String text = element2Data(element);
        return getShortObject(text);
    }

// g1u
    public static List<Short> getElementPropertyAsShortDataList(
        Element element
    ) {
        List<Short> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getShortObject(strings.get(i)));
        }
        return result;
    }

    public static short getElementPropertyAsShort(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Short.parseShort(text);
        } catch (Exception e) {
            return _invalidShortValue(e);
        }
    }

// g2a
    public static List getElementPropertyAsShortDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsShortDataList(property);
    }

    public static List<Short> getElementPropertyAsShortList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Short> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Short value = getElementPropertyAsShortObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List<List<Short>> getElementPropertyAsShortListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Short>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Short> values = getElementPropertyAsShortDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Short getElementPropertyAsShortByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsShortObject(property);
    }

// g4a
    public static List getElementPropertyAsShortDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsShortDataList(property);
    }

    public static List<Short> getElementPropertyAsShortListByStack(
        RStack stack,
        String name
    ) {
        List<Short> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Short value = getElementPropertyAsShortObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Short>> getElementPropertyAsShortListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Short>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Short> value = getElementPropertyAsShortDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static short getAttributePropertyAsShort(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Short.parseShort(value);
            }
        } catch (Exception e) {
            return _invalidShortValue(e);
        }
    }

    public static Short getAttributePropertyAsShortObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getShortObject(value);
        }
    }

    public static List<Short> getAttributePropertyAsShortList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Short> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getShortObject(data));
        }
        return result;
    }

    public static void setElementPropertyByShort(
        Element element,
        String name,
        short value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Short.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByShort(
        Element element,
        String name,
        Short value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByShortDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByShortList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByShortListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByShort(
        Element element,
        short value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Short.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByShort(
        Element element,
        Short value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByShortDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByShort(
        Element element,
        String name,
        short value
    ) {
        element.setAttribute(name, Short.toString(value));
    }

    public static void setAttributePropertyByShort(
        Element element,
        String name,
        Short value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByShortList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // int type
    public static String getString(int value) {
        return Integer.toString(value);
    }

    public static String getString(Integer value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(int[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Integer[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static int getIntValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Integer(value.toString());
            }
            return object.intValue();
        } catch (Exception e) {
            return _invalidIntValue(e);
        }
    }

    public static Integer getIntObject(String text) {
        try {
            return new Integer(text);
        } catch (Exception e) {
            return _invalidIntegerObject(e);
        }
    }

    public static Integer[] getIntObjectList(String text) {
        String[] strings = getStringList(text);
        Integer[] list = new Integer[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getIntObject(strings[i]);
        }
        return list;
    }

    public static int getElementPropertyAsInt(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Integer.parseInt(text);
        } catch (Exception e) {
            return _invalidIntValue(e);
        }
    }

    public static Integer getElementPropertyAsIntObject(
        Element element
    ) {
        String text = element2Data(element);
        return getIntObject(text);
    }

// g1u
    public static List<Integer> getElementPropertyAsIntDataList(
        Element element
    ) {
        List<Integer> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getIntObject(strings.get(i)));
        }
        return result;
    }

    public static int getElementPropertyAsInt(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Integer.parseInt(text);
        } catch (Exception e) {
            return _invalidIntValue(e);
        }
    }

// g2a
    public static List getElementPropertyAsIntDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsIntDataList(property);
    }

    public static List<Integer> getElementPropertyAsIntList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Integer> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Integer value = getElementPropertyAsIntObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List<List<Integer>> getElementPropertyAsIntListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Integer> values = getElementPropertyAsIntDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Integer getElementPropertyAsIntByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsIntObject(property);
    }

// ga4
    public static List getElementPropertyAsIntDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsIntDataList(property);
    }

    public static List<Integer> getElementPropertyAsIntListByStack(
        RStack stack,
        String name
    ) {
        List<Integer> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Integer value = getElementPropertyAsIntObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Integer>> getElementPropertyAsIntListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Integer>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Integer> value = getElementPropertyAsIntDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static int getAttributePropertyAsInt(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            return _invalidIntValue(e);
        }
    }

    public static Integer getAttributePropertyAsIntObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getIntObject(value);
        }
    }

    public static List<Integer> getAttributePropertyAsIntList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Integer> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getIntObject(data));
        }
        return result;
    }

    public static void setElementPropertyByInt(
        Element element,
        String name,
        int value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Integer.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByInt(
        Element element,
        String name,
        Integer value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1u
    public static void setElementPropertyByIntDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByIntList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByIntListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByInt(
        Element element,
        int value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Integer.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByInt(
        Element element,
        Integer value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByIntDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByInt(
        Element element,
        String name,
        int value
    ) {
        element.setAttribute(name, Integer.toString(value));
    }

    public static void setAttributePropertyByInt(
        Element element,
        String name,
        Integer value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByIntList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // long type
    public static String getString(long value) {
        return Long.toString(value);
    }

    public static String getString(Long value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(long[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Long[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static long getLongValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Long(value.toString());
            }
            return object.longValue();
        } catch (Exception e) {
            return _invalidLongValue(e);
        }
    }

    public static Long getLongObject(String text) {
        try {
            return new Long(text);
        } catch (Exception e) {
            return _invalidLongObject(e);
        }
    }

    public static Long[] getLongObjectList(String text) {
        String[] strings = getStringList(text);
        Long[] list = new Long[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getLongObject(strings[i]);
        }
        return list;
    }

    public static long getElementPropertyAsLong(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Long.parseLong(text);
        } catch (Exception e) {
            return _invalidLongValue(e);
        }
    }

    public static Long getElementPropertyAsLongObject(
        Element element
    ) {
        String text = element2Data(element);
        return getLongObject(text);
    }

// g1u
    public static List<Long> getElementPropertyAsLongDataList(
        Element element
    ) {
        List<Long> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getLongObject(strings.get(i)));
        }
        return result;
    }

    public static long getElementPropertyAsLong(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Long.parseLong(text);
        } catch (Exception e) {
            return _invalidLongValue(e);
        }
    }

// g2a
    public static List getElementPropertyAsLongDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsLongDataList(property);
    }

    public static List<Long> getElementPropertyAsLongList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Long> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Long value = getElementPropertyAsLongObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List<List<Long>> getElementPropertyAsLongListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Long>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Long> values = getElementPropertyAsLongDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Long getElementPropertyAsLongByStack(
        RStack stack,
        String name
    ) {
        try {
            if (stack.isEmptyElement()) {
                return null;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                return null;
            }
            stack.popElement();
            return new Long(element2Data(property));
        } catch (Exception e) {
            return _invalidLongObject(e);
        }
    }

// g4a
    public static List getElementPropertyAsLongDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsLongDataList(property);
    }

    public static List<Long> getElementPropertyAsLongListByStack(
        RStack stack,
        String name
    ) {
        List<Long> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Long value = getElementPropertyAsLongObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Long>> getElementPropertyAsLongListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Long>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Long> value = getElementPropertyAsLongDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static long getAttributePropertyAsLong(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            return _invalidLongValue(e);
        }
    }

    public static Long getAttributePropertyAsLongObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getLongObject(value);
        }
    }

    public static List getAttributePropertyAsLongList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Long> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getLongObject(data));
        }
        return result;
    }

    public static void setElementPropertyByLong(
        Element element,
        String name,
        long value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Long.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByLong(
        Element element,
        String name,
        Long value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByLongDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByLongList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByLongListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByLong(
        Element element,
        long value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Long.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByLong(
        Element element,
        Long value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByLongDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setElementPropertyByLongList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByLong(
        Element element,
        String name,
        long value
    ) {
        element.setAttribute(name, Long.toString(value));
    }

    public static void setAttributePropertyByLong(
        Element element,
        String name,
        Long value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByLongList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // float type
    public static String getString(float value) {
        return Float.toString(value);
    }

    public static String getString(Float value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(float[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Float[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static float getFloatValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Float(value.toString());
            }
            return object.floatValue();
        } catch (Exception e) {
            return _invalidFloatValue(e);
        }
    }

    public static Float getFloatObject(String text) {
        try {
            return new Float(text);
        } catch (Exception e) {
            return _invalidFloatObject(e);
        }
    }

    public static Float[] getFloatObjectList(String text) {
        String[] strings = getStringList(text);
        Float[] list = new Float[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getFloatObject(strings[i]);
        }
        return list;
    }

    public static float getElementPropertyAsFloat(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Float.parseFloat(text);
        } catch (Exception e) {
            return _invalidFloatValue(e);
        }
    }

    public static Float getElementPropertyAsFloatObject(
        Element element
    ) {
        String text = element2Data(element);
        return getFloatObject(text);
    }

// g1u
    public static List<Float> getElementPropertyAsFloatDataList(
        Element element
    ) {
        List<Float> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getFloatObject(strings.get(i)));
        }
        return result;
    }

    public static float getElementPropertyAsFloat(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Float.parseFloat(text);
        } catch (Exception e) {
            return _invalidFloatValue(e);
        }
    }

// g2a
    public static List getElementPropertyAsFloatDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsFloatDataList(property);
    }

    public static List<Float> getElementPropertyAsFloatList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Float> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Float value = getElementPropertyAsFloatObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List<List<Float>> getElementPropertyAsFloatListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Float>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Float> values = getElementPropertyAsFloatDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Float getElementPropertyAsFloatByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsFloatObject(property);
    }

// g4a
    public static List getElementPropertyAsFloatDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsFloatDataList(property);
    }

    public static List<Float> getElementPropertyAsFloatListByStack(
        RStack stack,
        String name
    ) {
        List<Float> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Float value = getElementPropertyAsFloatObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g5a
    public static List<List<Float>> getElementPropertyAsFloatListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Float>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Float> value = getElementPropertyAsFloatDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static float getAttributePropertyAsFloat(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Float.parseFloat(value);
            }
        } catch (Exception e) {
            return _invalidFloatValue(e);
        }
    }

    public static Float getAttributePropertyAsFloatObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getFloatObject(value);
        }
    }

    public static List<Float> getAttributePropertyAsFloatList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Float> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getFloatObject(data));
        }
        return result;
    }

    public static void setElementPropertyByFloat(
        Element element,
        String name,
        float value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Float.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByFloat(
        Element element,
        String name,
        Float value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByFloatDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByFloatList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByFloatListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByFloat(
        Element element,
        float value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Float.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByFloat(
        Element element,
        Float value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

    public static void setElementPropertyByFloatList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByFloatDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByFloat(
        Element element,
        String name,
        float value
    ) {
        element.setAttribute(name, Float.toString(value));
    }

    public static void setAttributePropertyByFloat(
        Element element,
        String name,
        Float value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByFloatList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // double type
    public static String getString(double value) {
        return Double.toString(value);
    }

    public static String getString(Double value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(double[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0]);
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i]);
            }
        }
        return new String(buffer);
    }

    public static String getString(Double[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static double getDoubleValue(Object value) {
        try {
            Number object;
            if (value instanceof Number) {
                object = (Number)value;
            } else {
                object = new Double(value.toString());
            }
            return object.doubleValue();
        } catch (Exception e) {
            return _invalidDoubleValue(e);
        }
    }

    public static Double getDoubleObject(String text) {
        try {
            return new Double(text);
        } catch (Exception e) {
            return _invalidDoubleObject(e);
        }
    }

    public static Double[] getDoubleObjectList(String text) {
        String[] strings = getStringList(text);
        Double[] list = new Double[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getDoubleObject(strings[i]);
        }
        return list;
    }

    public static double getElementPropertyAsDouble(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return Double.parseDouble(text);
        } catch (Exception e) {
            return _invalidDoubleValue(e);
        }
    }

    public static Double getElementPropertyAsDoubleObject(
        Element element
    ) {
        String text = element2Data(element);
        return getDoubleObject(text);
    }

// g1u
    public static List<Double> getElementPropertyAsDoubleDataList(
        Element element
    ) {
        List<Double> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getDoubleObject(strings.get(i)));
        }
        return result;
    }

    public static double getElementPropertyAsDouble(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return Double.parseDouble(text);
        } catch (Exception e) {
            return _invalidDoubleValue(e);
        }
    }

// g2a
    public static List getElementPropertyAsDoubleDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsDoubleDataList(property);
    }

    public static List getElementPropertyAsDoubleList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Double> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Double value = getElementPropertyAsDoubleObject(nodes[i]);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsDoubleListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Double>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Double> values = getElementPropertyAsDoubleDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static List getElementPropertyAsDoubleList(
        Element element
    ) {
        List<Double> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getDoubleObject(strings.get(i)));
        }
        return result;
    }

// g4a
    public static List getElementPropertyAsDoubleDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsDoubleDataList(property);
    }

    public static Double getElementPropertyAsDoubleByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsDoubleObject(property);
    }

// g5a
    public static List getElementPropertyAsDoubleListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Double>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Double> value = getElementPropertyAsDoubleDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static List getElementPropertyAsDoubleListByStack(
        RStack stack,
        String name
    ) {
        List<Double> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Double value = getElementPropertyAsDoubleObject(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static double getAttributePropertyAsDouble(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return 0;
            } else {
                return Double.parseDouble(value);
            }
        } catch (Exception e) {
            return _invalidDoubleValue(e);
        }
    }

    public static Double getAttributePropertyAsDoubleObject(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getDoubleObject(value);
        }
    }

    public static List getAttributePropertyAsDoubleList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Double> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getDoubleObject(data));
        }
        return result;
    }

    public static void setElementPropertyByDouble(
        Element element,
        String name,
        double value
    ) {
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(Double.toString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

    public static void setElementPropertyByDouble(
        Element element,
        String name,
        Double value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByDoubleDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByDoubleList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByDoubleListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByDouble(
        Element element,
        double value
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(Double.toString(value));
        element.appendChild(text);
    }

    public static void setElementPropertyByDouble(
        Element element,
        Double value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

    public static void setElementPropertyByDoubleList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByDoubleDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByDouble(
        Element element,
        String name,
        double value
    ) {
        element.setAttribute(name, Double.toString(value));
    }

    public static void setAttributePropertyByDouble(
        Element element,
        String name,
        Double value
    ) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    public static void setAttributePropertyByDoubleList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // BigDecimal type
    public static String getString(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(BigDecimal[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static BigDecimal getBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return _invalidBigDecimal(e);
        }
    }

    public static BigDecimal getBigDecimalObject(Object value) {
        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal)value;
            } else {
                return new BigDecimal(value.toString());
            }
        } catch (Exception e) {
            return _invalidBigDecimal(e);
        }
    }

    public static BigDecimal[] getBigDecimalList(String text) {
        String[] strings = getStringList(text);
        BigDecimal[] list = new BigDecimal[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getBigDecimal(strings[i]);
        }
        return list;
    }

    public static BigDecimal getElementPropertyAsBigDecimal(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return new BigDecimal(text);
        } catch (Exception e) {
            return _invalidBigDecimal(e);
        }
    }

// g1u
    public static List<BigDecimal> getElementPropertyAsBigDecimalDataList(
        Element element
    ) {
        List<BigDecimal> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getBigDecimalObject(strings.get(i)));
        }
        return result;
    }

    public static BigDecimal getElementPropertyAsBigDecimal(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return new BigDecimal(text);
        } catch (Exception e) {
            return _invalidBigDecimal(e);
        }
    }

// g2a
    public static List getElementPropertyAsBigDecimalDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBigDecimalDataList(property);
    }

    public static List getElementPropertyAsBigDecimalList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<BigDecimal> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            try {
                list.add(new BigDecimal(element2Data(nodes[i])));
            } catch (Exception e) {
                _invalidBigDecimal(e);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsBigDecimalListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<BigDecimal>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<BigDecimal> values = getElementPropertyAsBigDecimalDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static BigDecimal getElementPropertyAsBigDecimalByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBigDecimal(property);
    }

// g4a
    public static List getElementPropertyAsBigDecimalDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBigDecimalDataList(property);
    }

    public static List getElementPropertyAsBigDecimalListByStack(
        RStack stack,
        String name
    ) {
        List<BigDecimal> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            try {
                list.add(new BigDecimal(element2Text(property)));
            } catch (Exception e) {
                _invalidBigDecimal(e);
            }
        }
        return list;
    }

// g5a
    public static List getElementPropertyAsBigDecimalListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<BigDecimal>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<BigDecimal> value = getElementPropertyAsBigDecimalDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static BigDecimal getAttributePropertyAsBigDecimal(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return null;
            } else {
                return new BigDecimal(value);
            }
        } catch (Exception e) {
            return _invalidBigDecimal(e);
        }
    }

    public static List getAttributePropertyAsBigDecimalList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<BigDecimal> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getBigDecimalObject(data));
        }
        return result;
    }

    public static void setElementPropertyByBigDecimal(
        Element element,
        String name,
        BigDecimal value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByBigDecimalDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByBigDecimalList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByBigDecimalListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByBigDecimal(
        Element element,
        BigDecimal value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3
    public static void setElementPropertyByBigDecimalDataList(
        Element element,
        List values
    ) {
        if (values == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByBigDecimal(
        Element element,
        String name,
        BigDecimal value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyByBigDecimalList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // BigInteger type
    public static String getString(BigInteger value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(BigInteger[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static BigInteger getBigInteger(String value) {
        try {
            return new BigInteger(value);
        } catch (Exception e) {
            return _invalidBigInteger(e);
        }
    }

    public static BigInteger getBigIntegerObject(Object value) {
        try {
            if (value instanceof BigInteger) {
                return (BigInteger)value;
            } else {
                return new BigInteger(value.toString());
            }
        } catch (Exception e) {
            return _invalidBigInteger(e);
        }
    }

    public static BigInteger[] getBigIntegerList(String text) {
        String[] strings = getStringList(text);
        BigInteger[] list = new BigInteger[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getBigInteger(strings[i]);
        }
        return list;
    }

    public static BigInteger getElementPropertyAsBigInteger(
        Element element
    ) {
        try {
            String text = element2Data(element);
            return new BigInteger(text);
        } catch (Exception e) {
            return _invalidBigInteger(e);
        }
    }

// g1u
    public static List<BigInteger> getElementPropertyAsBigIntegerDataList(
        Element element
    ) {
        List<BigInteger> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getBigIntegerObject(strings.get(i)));
        }
        return result;
    }

    public static BigInteger getElementPropertyAsBigInteger(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            return new BigInteger(text);
        } catch (Exception e) {
            return _invalidBigInteger(e);
        }
    }

// g2a
    public static List getElementPropertyAsBigIntegerDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBigIntegerDataList(property);
    }

    public static List getElementPropertyAsBigIntegerList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<BigInteger> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            try {
                list.add(new BigInteger(element2Data(nodes[i])));
            } catch (Exception e) {
                _invalidBigInteger(e);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsBigIntegerListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<BigInteger>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<BigInteger> values = getElementPropertyAsBigIntegerDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static BigInteger getElementPropertyAsBigIntegerByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBigInteger(property);
    }

// g4a
    public static List getElementPropertyAsBigIntegerDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBigIntegerDataList(property);
    }

    public static List<BigInteger> getElementPropertyAsBigIntegerListByStack(
        RStack stack,
        String name
    ) {
        List<BigInteger> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            try {
                list.add(new BigInteger(element2Text(property)));
            } catch (Exception e) {
                _invalidBigInteger(e);
            }
        }
        return list;
    }

// g5a
    public static List getElementPropertyAsBigIntegerListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<BigInteger>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<BigInteger> value = getElementPropertyAsBigIntegerDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static BigInteger getAttributePropertyAsBigInteger(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return null;
            } else {
                return new BigInteger(value);
            }
        } catch (Exception e) {
            return _invalidBigInteger(e);
        }
    }

    public static List<BigInteger> getAttributePropertyAsBigIntegerList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<BigInteger> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getBigIntegerObject(data));
        }
        return result;
    }

    public static void setElementPropertyByBigInteger(
        Element element,
        String name,
        BigInteger value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByBigIntegerDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByBigIntegerList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByBigIntegerListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByBigInteger(
        Element element,
        BigInteger value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByBigIntegerDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByBigInteger(
        Element element,
        String name,
        BigInteger value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyByBigIntegerList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // Date type
    public static String getString(Date value) {
        if (value == null) {
            return null;
        }
        return getString(new java.sql.Timestamp(value.getTime()));
    }

    public static String getString(Date[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static Date getDateObject(Object value) {
        if (value instanceof Date) {
            return (Date)value;
        } else {
            return getSQLTimestampObject(value);
        }
    }

    public static Date getElementPropertyAsDate(
        Element element
    ) {
        try {
            String text = element2Data(element);
            DateFormat df = DateFormat.getDateInstance();
            return df.parse(text);
        } catch (Exception e) {
            return _invalidDate(e);
        }
    }

// g1u
    public static List<Date> getElementPropertyAsDateDataList(
        Element element
    ) {
        List<Date> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getDateObject(strings.get(i)));
        }
        return result;
    }

    public static Date getElementPropertyAsDate(
        Element element,
        String name
    ) {
        try {
            Element property = getOnlyElement(element, name);
            String text = element2Data(property);
            DateFormat df = DateFormat.getDateInstance();
            return df.parse(text);
        } catch (IllegalArgumentException | ParseException e) {
            return _invalidDate(e);
        }
    }

// g2a
    public static List getElementPropertyAsDateDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsDateDataList(property);
    }

    public static List<Date> getElementPropertyAsDateList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        DateFormat df = DateFormat.getDateInstance();
        List<Date> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            try {
                list.add(df.parse(element2Data(nodes[i])));
            } catch (Exception e) {
                _invalidDate(e);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsDateListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Date>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Date> values = getElementPropertyAsDateDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Date getElementPropertyAsDateByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsDate(property);
    }

// g4a
    public static List getElementPropertyAsDateDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsDateDataList(property);
    }

    public static List<Date> getElementPropertyAsDateListByStack(
        RStack stack,
        String name
    ) {
        List<Date> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            String value = element2Text(property);
            try {
                DateFormat df = DateFormat.getDateInstance();
                list.add(df.parse(value));
            } catch (Exception e) {
                _invalidDate(e);
            }
        }
        return list;
    }

// g5a
    public static List<List<Date>> getElementPropertyAsDateListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Date>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Date> value = getElementPropertyAsDateDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static Date getAttributePropertyAsDate(
        Element element,
        String name
    ) {
        try {
            String value = getAttribute(element, name);
            if (value == null) {
                return null;
            } else {
                DateFormat df = DateFormat.getDateInstance();
                return df.parse(value);
            }
        } catch (Exception e) {
            return _invalidDate(e);
        }
    }

    public static List<Date> getAttributePropertyAsDateList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Date> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getDateObject(data));
        }
        return result;
    }

    public static void setElementPropertyByDate(
        Element element,
        String name,
        Date value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        DateFormat df = DateFormat.getDateInstance();
        Text text = doc.createTextNode(df.format(value));
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByDateDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByDateList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByDateListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByDate(
        Element element,
        Date value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        DateFormat df = DateFormat.getDateInstance();
        Text text = doc.createTextNode(df.format(value));
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByDateDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByDate(
        Element element,
        String name,
        Date value
    ) {
        if (value != null) {
            DateFormat df = DateFormat.getDateInstance();
            element.setAttribute(name, df.format(value));
        }
    }

    public static void setAttributePropertyByDateList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // Locale type
    public static String getString(Locale value) {
        return value.toString();
    }

    public static String getString(Locale[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static Locale getLocale(String value) {
        if (value == null) {
            return null;
        }
        return makeLocale(value);
    }

    public static Locale getLocaleObject(Object value) {
        if (value instanceof Locale) {
            return (Locale)value;
        } else {
            return makeLocale(value.toString());
        }
    }

    public static Locale[] getLocaleList(String text) {
        String[] strings = getStringList(text);
        Locale[] list = new Locale[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getLocale(strings[i]);
        }
        return list;
    }

    public static Locale getElementPropertyAsLocale(
        Element element
    ) {
        String text = element2Data(element);
        return makeLocale(text);
    }

// g1u
    public static List<Locale> getElementPropertyAsLocaleDataList(
        Element element
    ) {
        List<Locale> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getLocaleObject(strings.get(i)));
        }
        return result;
    }

    public static Locale getElementPropertyAsLocale(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return makeLocale(text);
    }

// g2a
    public static List getElementPropertyAsLocaleDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsLocaleDataList(property);
    }

    public static List<Locale> getElementPropertyAsLocaleList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<Locale> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            Locale locale = makeLocale(element2Data(nodes[i]));
            if (locale != null) {
                list.add(locale);
            }
        }
        return list;
    }

// g3a
    public static List getElementPropertyAsLocaleListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<Locale>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<Locale> values = getElementPropertyAsLocaleDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Locale getElementPropertyAsLocaleByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsLocale(property);
    }

// g4a
    public static List getElementPropertyAsLocaleDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsLocaleDataList(property);
    }

    public static List<Locale> getElementPropertyAsLocaleListByStack(
        RStack stack,
        String name
    ) {
        List<Locale> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            Locale locale = makeLocale(element2Text(property));
            if (locale != null) {
                list.add(locale);
            }
        }
        return list;
    }

// g5a
    public static List<List<Locale>> getElementPropertyAsLocaleListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<Locale>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<Locale> value = getElementPropertyAsLocaleDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static Locale getAttributePropertyAsLocale(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return makeLocale(value);
        }
    }

    public static List getAttributePropertyAsLocaleList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<Locale> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(makeLocale(data));
        }
        return result;
    }

    public static void setElementPropertyByLocale(
        Element element,
        String name,
        Locale value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByLocaleDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByLocaleList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByLocaleListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByLocale(
        Element element,
        Locale value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByLocaleDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByLocale(
        Element element,
        String name,
        Locale value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyByLocaleList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // URL type
    public static String getString(URL value) {
        if (value == null) {
            return null;
        }
        return value.toExternalForm();
    }

    public static String getString(URL[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(getString(values[0]));
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(getString(values[i]));
            }
        }
        return new String(buffer);
    }

    public static URL getURL(String value) {
        return makeURL4Property(value);
    }

    public static URL getURLObject(Object value) {
        if (value instanceof URL) {
            return (URL)value;
        } else {
            return makeURL4Property(value.toString());
        }
    }

    public static URL[] getURLList(String text) {
        String[] strings = getStringList(text);
        URL[] list = new URL[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getURL(strings[i]);
        }
        return list;
    }

    public static URL getElementPropertyAsURL(
        Element element
    ) {
        String text = element2Data(element);
        return makeURL4Property(text);
    }

// g1u
    public static List<URL> getElementPropertyAsURLDataList(
        Element element
    ) {
        List<URL> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getURLObject(strings.get(i)));
        }
        return result;
    }

    public static URL getElementPropertyAsURL(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return makeURL4Property(text);
    }

// g2a
    public static List getElementPropertyAsURLDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsURLDataList(property);
    }

    public static List<URL> getElementPropertyAsURLList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<URL> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            URL url = makeURL4Property(element2Data(nodes[i]));
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }

// g3a
    public static List<List<URL>> getElementPropertyAsURLListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<URL>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<URL> values = getElementPropertyAsURLDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static URL getElementPropertyAsURLByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsURL(property);
    }

// g4a
    public static List getElementPropertyAsURLDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsURLDataList(property);
    }

    public static List getElementPropertyAsURLListByStack(
        RStack stack,
        String name
    ) {
        List<URL> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            URL url = makeURL4Property(element2Text(property));
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }

// g5a
    public static List<List<URL>> getElementPropertyAsURLListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<URL>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<URL> value = getElementPropertyAsURLDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static URL getAttributePropertyAsURL(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return makeURL4Property(value);
        }
    }

    public static List getAttributePropertyAsURLList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<URL> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            URL url = makeURL4Property(data);
            if (url != null) {
                result.add(url);
            }
        }
        return result;
    }

    public static void setElementPropertyByURL(
        Element element,
        String name,
        URL value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByURLDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByURLList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByURLListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByURL(
        Element element,
        URL value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByURLDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByURL(
        Element element,
        String name,
        URL value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyByURLList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(values.get(0).toString());
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(values.get(i).toString());
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // SQL Timestamp type
    public static String getString(java.sql.Timestamp value) {
        if (value == null) {
            return null;
        }
        return adjustTime_(value.toString().replace(' ', 'T'));
    }

    public static String getString(java.sql.Timestamp[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(getString(values[0]));
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(getString(values[i]));
            }
        }
        return new String(buffer);
    }

    public static java.sql.Timestamp getSQLTimestamp(String value) {
        try {
            return java.sql.Timestamp.valueOf(value.trim().replace('T', ' '));
        } catch (Exception e) {
            return _invalidSQLTimestamp(e);
        }
    }

    public static java.sql.Timestamp getSQLTimestampObject(Object value) {
        if (value instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp)value;
        } else {
            return getSQLTimestamp(value.toString());
        }
    }

    public static java.sql.Timestamp[] getSQLTimestampList(String text) {
        String[] strings = getStringList(text);
        java.sql.Timestamp[] list = new java.sql.Timestamp[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getSQLTimestamp(strings[i]);
        }
        return list;
    }

    public static java.sql.Timestamp getElementPropertyAsSQLTimestamp(
        Element element
    ) {
        String text = element2Data(element);
        return getSQLTimestamp(text);
    }

// g1u
    public static List<java.sql.Timestamp> getElementPropertyAsSQLTimestampDataList(
        Element element
    ) {
        List<java.sql.Timestamp> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getSQLTimestampObject(strings.get(i)));
        }
        return result;
    }

    public static java.sql.Timestamp getElementPropertyAsSQLTimestamp(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return getSQLTimestamp(text);
    }

// g2a
    public static List getElementPropertyAsSQLTimestampDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsSQLTimestampDataList(property);
    }

    public static List<java.sql.Timestamp> getElementPropertyAsSQLTimestampList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<java.sql.Timestamp> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            java.sql.Timestamp ts = getSQLTimestamp(element2Data(nodes[i]));
            if (ts != null) {
                list.add(ts);
            }
        }
        return list;
    }

// g3a
    public static List<List<java.sql.Time>> getElementPropertyAsSQLTimestampListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<java.sql.Time>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<java.sql.Time> values = getElementPropertyAsSQLTimeDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static java.sql.Timestamp getElementPropertyAsSQLTimestampByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLTimestamp(property);
    }

// g4a
    public static List getElementPropertyAsSQLTimestampDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLTimestampDataList(property);
    }

    public static List<java.sql.Timestamp> getElementPropertyAsSQLTimestampListByStack(
        RStack stack,
        String name
    ) {
        List<java.sql.Timestamp> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            java.sql.Timestamp ts = getSQLTimestamp(element2Data(property));
            if (ts != null) {
                list.add(ts);
            }
        }
        return list;
    }

// g5a
    public static List<List<java.sql.Timestamp>> getElementPropertyAsSQLTimestampListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<java.sql.Timestamp>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<java.sql.Timestamp> value = getElementPropertyAsSQLTimestampDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static java.sql.Timestamp getAttributePropertyAsSQLTimestamp(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getSQLTimestamp(value);
        }
    }

    public static List getAttributePropertyAsSQLTimestampList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<java.sql.Timestamp> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getSQLTimestamp(data));
        }
        return result;
    }

    public static void setElementPropertyBySQLTimestamp(
        Element element,
        String name,
        java.sql.Timestamp value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(getString(value));
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyBySQLTimestampDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyBySQLTimestampList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(
                getString((java.sql.Timestamp)values.get(i))
            );
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyBySQLTimestampListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyBySQLTimestamp(
        Element element,
        java.sql.Timestamp value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(value));
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyBySQLTimestampDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyBySQLTimestamp(
        Element element,
        String name,
        java.sql.Timestamp value
    ) {
        if (value != null) {
            element.setAttribute(name, getString(value));
        }
    }

    public static void setAttributePropertyBySQLTimestampList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        element.setAttribute(name, getString(values));
    }

    // SQL Time type
    public static String getString(java.sql.Time value) {
        if (value == null) {
            return null;
        }
        return adjustTime_(value.toString());
    }

    private static String adjustTime_(String time) {
        if (!time.endsWith(".0")) {
            return time;
        }
        return time.substring(0, time.length() - 2);
    }

    public static String getString(java.sql.Time[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }
    public static java.sql.Time getSQLTime(String value) {
        try {
            return java.sql.Time.valueOf(value);
        } catch (Exception e) {
            return _invalidSQLTime(e);
        }
    }

    public static java.sql.Time getSQLTimeObject(Object value) {
        if (value instanceof java.sql.Time) {
            return (java.sql.Time)value;
        } else {
            return getSQLTime(value.toString());
        }
    }

    public static java.sql.Time[] getSQLTimeList(String text) {
        String[] strings = getStringList(text);
        java.sql.Time[] list = new java.sql.Time[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getSQLTime(strings[i]);
        }
        return list;
    }

    public static java.sql.Time getElementPropertyAsSQLTime(
        Element element
    ) {
        String text = element2Data(element);
        return getSQLTime(text);
    }

// g1u
    public static List<java.sql.Time> getElementPropertyAsSQLTimeDataList(
        Element element
    ) {
        List<java.sql.Time> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getSQLTimeObject(strings.get(i)));
        }
        return result;
    }

    public static java.sql.Time getElementPropertyAsSQLTime(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return getSQLTime(text);
    }

// g2a
    public static List getElementPropertyAsSQLTimeDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsSQLTimeDataList(property);
    }

    public static List<java.sql.Time> getElementPropertyAsSQLTimeList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<java.sql.Time> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            java.sql.Time time = getSQLTime(element2Data(nodes[i]));
            if (time != null) {
                list.add(time);
            }
        }
        return list;
    }

// g3a
    public static List<List<java.sql.Time>> getElementPropertyAsSQLTimeListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<java.sql.Time>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<java.sql.Time> values = getElementPropertyAsSQLTimeDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static java.sql.Time getElementPropertyAsSQLTimeByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLTime(property);
    }

// g4a
    public static List getElementPropertyAsSQLTimeDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLTimeDataList(property);
    }

    public static List<java.sql.Time> getElementPropertyAsSQLTimeListByStack(
        RStack stack,
        String name
    ) {
        List<java.sql.Time> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            java.sql.Time time = getSQLTime(element2Data(property));
            if (time != null) {
                list.add(time);
            }
        }
        return list;
    }

// g5a
    public static List<List<java.sql.Time>> getElementPropertyAsSQLTimeListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<java.sql.Time>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<java.sql.Time> value = getElementPropertyAsSQLTimeDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static java.sql.Time getAttributePropertyAsSQLTime(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getSQLTime(value);
        }
    }

    public static List getAttributePropertyAsSQLTimeList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<java.sql.Time> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getSQLTime(data));
        }
        return result;
    }

    public static void setElementPropertyBySQLTime(
        Element element,
        String name,
        java.sql.Time value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyBySQLTimeDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyBySQLTimeList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyBySQLTimeListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyBySQLTime(
        Element element,
        java.sql.Time value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyBySQLTimeDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyBySQLTime(
        Element element,
        String name,
        java.sql.Time value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyBySQLTimeList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        element.setAttribute(name, getString(values));
    }

    // SQL Date type
    public static String getString(java.sql.Date value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String getString(java.sql.Date[] values) {
        if (values == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (values.length > 0) {
            buffer.append(values[0].toString());
            for (int i = 1;i < values.length;i++) {
                buffer.append(" ");
                buffer.append(values[i].toString());
            }
        }
        return new String(buffer);
    }

    public static java.sql.Date getSQLDate(String value) {
        try {
            return java.sql.Date.valueOf(value);
        } catch (Exception e) {
            return _invalidSQLDate(e);
        }
    }

    public static java.sql.Date getSQLDateObject(Object value) {
        if (value instanceof java.sql.Date) {
            return (java.sql.Date)value;
        } else {
            return getSQLDate(value.toString());
        }
    }

    public static java.sql.Date[] getSQLDateList(String text) {
        String[] strings = getStringList(text);
        java.sql.Date[] list = new java.sql.Date[strings.length];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getSQLDate(strings[i]);
        }
        return list;
    }

    public static java.sql.Date getElementPropertyAsSQLDate(
        Element element
    ) {
        String text = element2Data(element);
        return getSQLDate(text);
    }

// g1u
    public static List<java.sql.Date> getElementPropertyAsSQLDateDataList(
        Element element
    ) {
        List<java.sql.Date> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getSQLDateObject(strings.get(i)));
        }
        return result;
    }

    public static java.sql.Date getElementPropertyAsSQLDate(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return getSQLDate(text);
    }

// g2a
    public static List getElementPropertyAsSQLDateDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsSQLDateDataList(property);
    }

    public static List<java.sql.Date> getElementPropertyAsSQLDateList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<java.sql.Date> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            java.sql.Date date = getSQLDate(element2Data(nodes[i]));
            if (date != null) {
                list.add(date);
            }
        }
        return list;
    }

// g3a
    public static List<List<java.sql.Date>> getElementPropertyAsSQLDateListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<java.sql.Date>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<java.sql.Date> values = getElementPropertyAsSQLDateDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static java.sql.Date getElementPropertyAsSQLDateByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLDate(property);
    }

// g4a
    public static List getElementPropertyAsSQLDateDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsSQLDateDataList(property);
    }

    public static List<java.sql.Date> getElementPropertyAsSQLDateListByStack(
        RStack stack,
        String name
    ) {
        List<java.sql.Date> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            java.sql.Date date = getSQLDate(element2Data(property));
            if (date != null) {
                list.add(date);
            }
        }
        return list;
    }

// g5a
    public static List<List<java.sql.Date>> getElementPropertyAsSQLDateListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<java.sql.Date>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<java.sql.Date> value = getElementPropertyAsSQLDateDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static java.sql.Date getAttributePropertyAsSQLDate(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        } else {
            return getSQLDate(value);
        }
    }

    public static List getAttributePropertyAsSQLDateList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<java.sql.Date> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(getSQLDate(data));
        }
        return result;
    }

    public static void setElementPropertyBySQLDate(
        Element element,
        String name,
        java.sql.Date value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(value.toString());
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyBySQLDateDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyBySQLDateList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyBySQLDateListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyBySQLDate(
        Element element,
        java.sql.Date value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(value.toString());
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyBySQLDateDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyBySQLDate(
        Element element,
        String name,
        java.sql.Date value
    ) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }

    public static void setAttributePropertyBySQLDateList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        element.setAttribute(name, getString(values));
    }

    // dateTime type (java.util.Calendar)
    // TODO

    // date type (java.util.Calendar)
    // TODO

    // time type (java.util.Calendar)
    // TODO

    // binary type (BASE64)
    public static String getBinaryString(byte[] value) {
        if (value == null) {
            return null;
        }
        return makeStringAsBASE64(value);
    }

    public static String getBinaryString(byte[][] value) {
        if (value == null) {
            return null;
        }
        if (value.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(makeStringAsBASE64(value[0]));
        for (int i = 1;i < value.length;i++) {
            buffer.append(" ");
            buffer.append(makeStringAsBASE64(value[i]));
        }
        return new String(buffer);
    }

    public static byte[] getBinary(String value) {
        return makeBytesByBASE64(value);
    }

    public static byte[] getBinaryObject(Object value) {
        if (value instanceof byte[]) {
            return (byte[])value;
        } else {
            return getBinary(value.toString());
        }
    }

    public static byte[][] getBinaryList(String text) {
        String[] strings = getStringList(text);
        byte[][] list = new byte[strings.length][];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getBinary(strings[i]);
        }
        return list;
    }

    public static byte[] getElementPropertyAsBinaryBASE64(
        Element element
    ) {
        String text = element2Data(element);
        return makeBytesByBASE64(text);
    }

// g1u
    public static List<byte[]> getElementPropertyAsBinaryBASE64DataList(
        Element element
    ) {
        List<byte[]> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getBinaryObject(strings.get(i)));
        }
        return result;
    }

    public static byte[] getElementPropertyAsBinaryBASE64(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return makeBytesByBASE64(text);
    }

// g2a
    public static List getElementPropertyAsBinaryBASE64DataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBinaryBASE64DataList(property);
    }

    public static List<byte[]> getElementPropertyAsBinaryListBASE64(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<byte[]> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            byte[] binary = makeBytesByBASE64(element2Data(nodes[i]));
            if (binary != null) {
                list.add(binary);
            }
        }
        return list;
    }

// g3a
    public static List<List<byte[]>> getElementPropertyAsBinaryBASE64ListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<byte[]>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<byte[]> values = getElementPropertyAsBinaryBASE64DataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static byte[] getElementPropertyAsBinaryBASE64ByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBinaryBASE64(property);
    }

// g4a
    public static List getElementPropertyAsBinaryBASE64DataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBinaryBASE64DataList(property);
    }

    public static List<byte[]> getElementPropertyAsBinaryListBASE64ByStack(
        RStack stack,
        String name
    ) {
        List<byte[]> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            byte[] binary = makeBytesByBASE64(element2Data(property));
            if (binary != null) {
                list.add(binary);
            }
        }
        return list;
    }

// g5a
    public static List<List<byte[]>> getElementPropertyAsBinaryBASE64ListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<byte[]>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<byte[]> value = getElementPropertyAsBinaryBASE64DataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static byte[] getAttributePropertyAsBinaryBASE64(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        return makeBytesByBASE64(value);
    }

    public static List<byte[]> getAttributePropertyAsBinaryBASE64List(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<byte[]> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i);
            result.add(makeBytesByBASE64(data));
        }
        return result;
    }

    public static void setElementPropertyByBinaryBASE64(
        Element element,
        String name,
        byte[] value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(makeStringAsBASE64(value));
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByBinaryBASE64DataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByBinaryListBASE64(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(
                makeStringAsBASE64((byte[])values.get(i))
            );
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByBinaryBASE64ListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByBinaryBASE64(
        Element element,
        byte[] value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(makeStringAsBASE64(value));
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByBinaryBASE64DataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByBinaryBASE64(
        Element element,
        String name,
        byte[] value
    ) {
        if (value != null) {
            element.setAttribute(name, makeStringAsBASE64(value));
        }
    }

    public static void setAttributePropertyByBinaryBASE64List(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(makeStringAsBASE64((byte[])values.get(0)));
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(makeStringAsBASE64((byte[])values.get(i)));
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // HEX type
    public static String getStringByBinaryHEX(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return makeStringAsHEX(bytes);
    }

    public static String getStringByBinaryHEX(byte[][] value) {
        if (value == null) {
            return null;
        }
        if (value.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(makeStringAsHEX(value[0]));
        for (int i = 1;i < value.length;i++) {
            buffer.append(" ");
            buffer.append(makeStringAsHEX(value[i]));
        }
        return new String(buffer);
    }

    public static byte[] getBinaryHEX(String value) {
        return makeBytesByHEX(value);
    }

    public static byte[] getBinaryHEXObject(Object value) {
        if (value instanceof byte[]) {
            return (byte[])value;
        } else {
            return makeBytesByHEX(value.toString());
        }
    }

    public static byte[][] getBinaryHEXList(String text) {
        String[] strings = getStringList(text);
        byte[][] list = new byte[strings.length][];
        for (int i = 0;i < strings.length;i++) {
            list[i] = getBinaryHEX(strings[i]);
        }
        return list;
    }

    public static byte[] getElementPropertyAsBinaryHEX(
        Element element
    ) {
        String text = element2Data(element);
        return makeBytesByHEX(text);
    }

// g1u
    public static List<byte[]> getElementPropertyAsBinaryHEXDataList(
        Element element
    ) {
        List<byte[]> result = new ArrayList<>();
        List<String> strings = getElementPropertyAsStringDataList(element);
        int size = strings.size();
        for (int i = 0;i < size;i++) {
            result.add(getBinaryHEXObject(strings.get(i)));
        }
        return result;
    }

    public static byte[] getElementPropertyAsBinaryHEX(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        String text = element2Data(property);
        return makeBytesByHEX(text);
    }

// g2a
    public static List getElementPropertyAsBinaryHEXDataList(
        Element element,
        String name
    ) {
        Element property = getOnlyElement(element, name);
        return getElementPropertyAsBinaryHEXDataList(property);
    }

    public static List<byte[]> getElementPropertyAsBinaryListHEX(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<byte[]> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            byte[] binary = makeBytesByHEX(element2Data(nodes[i]));
            if (binary != null) {
                list.add(binary);
            }
        }
        return list;
    }

// g3a
    public static List<List<byte[]>> getElementPropertyAsBinaryHEXListDataList(
        Element element,
        String name
    ) {
        Element[] nodes = getElements(element, name);
        List<List<byte[]>> list = new ArrayList<>();
        for (int i = 0;i < nodes.length;i++) {
            List<byte[]> values = getElementPropertyAsBinaryHEXDataList(nodes[i]);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static byte[] getElementPropertyAsBinaryHEXByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBinaryHEX(property);
    }

// g4a
    public static List getElementPropertyAsBinaryHEXDataListByStack(
        RStack stack,
        String name
    ) {
        if (stack.isEmptyElement()) {
            return null;
        }
        Element property = stack.peekElement();
        if (!name.equals(property.getTagName())) {
            return null;
        }
        stack.popElement();
        return getElementPropertyAsBinaryHEXDataList(property);
    }

    public static List<byte[]> getElementPropertyAsBinaryListHEXByStack(
        RStack stack,
        String name
    ) {
        List<byte[]> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            byte[] binary = getElementPropertyAsBinaryHEX(property);
            if (binary != null) {
                list.add(binary);
            }
        }
        return list;
    }

// g5a
    public static List<List<byte[]>> getElementPropertyAsBinaryHEXListDataListByStack(
        RStack stack,
        String name
    ) {
        List<List<byte[]>> list = new ArrayList<>();
        for (;;) {
            if (stack.isEmptyElement()) {
                break;
            }
            Element property = stack.peekElement();
            if (!name.equals(property.getTagName())) {
                break;
            }
            stack.popElement();
            List<byte[]> value = getElementPropertyAsBinaryHEXDataList(property);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    public static byte[] getAttributePropertyAsBinaryHEX(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        return makeBytesByHEX(value);
    }

    public static List<byte[]> getAttributePropertyAsBinaryHEXList(
        Element element,
        String name
    ) {
        String value = getAttribute(element, name);
        if (value == null) {
            return null;
        }
        List<String> list = makeStringList(value);
        List<byte[]> result = new ArrayList<>();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            String data = list.get(i).toString();
            result.add(makeBytesByHEX(data));
        }
        return result;
    }

    public static void setElementPropertyByBinaryHEX(
        Element element,
        String name,
        byte[] value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Element property = doc.createElement(name);
        Text text = doc.createTextNode(makeStringAsHEX(value));
        property.appendChild(text);
        element.appendChild(property);
    }

// s1a
    public static void setElementPropertyByBinaryHEXDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        String value = getString(values);
        Element property = doc.createElement(name);
        if (value != null) {
            Text text = doc.createTextNode(value);
            property.appendChild(text);
        }
        element.appendChild(property);
    }

    public static void setElementPropertyByBinaryListHEX(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(
                makeStringAsHEX((byte[])values.get(i))
            );
            property.appendChild(text);
            element.appendChild(property);
        }
    }

// s2
    public static void setElementPropertyByBinaryHEXListDataList(
        Element element,
        String name,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        int size = values.size();
        for (int i = 0;i < size;i++) {
            Element property = doc.createElement(name);
            Text text = doc.createTextNode(values.get(i).toString());
            property.appendChild(text);
            element.appendChild(property);
        }
    }

    public static void setElementPropertyByBinaryHEX(
        Element element,
        byte[] value
    ) {
        if (value == null) {
            return;
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(makeStringAsHEX(value));
        element.appendChild(text);
    }

// s3u
    public static void setElementPropertyByBinaryHEXDataList(
        Element element,
        List values
    ) {
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(getString(values));
        element.appendChild(text);
    }

    public static void setAttributePropertyByBinaryHEX(
        Element element,
        String name,
        byte[] value
    ) {
        if (value != null) {
            element.setAttribute(name, makeStringAsHEX(value));
        }
    }

    public static void setAttributePropertyByBinaryHEXList(
        Element element,
        String name,
        List values
    ) {
        if (values == null) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        int size = values.size();
        if (size > 0) {
            buffer.append(makeStringAsHEX((byte[])values.get(0)));
            for (int i = 1;i < size;i++) {
                buffer.append(" ");
                buffer.append(makeStringAsHEX((byte[])values.get(i)));
            }
        }
        element.setAttribute(name, new String(buffer));
    }

    // Relaxer object
    public static String getString(Object object) {
        if (object instanceof java.sql.Timestamp) {
            return getString((java.sql.Timestamp)object);
        } else if (object instanceof java.sql.Time) {
            return getString((java.sql.Time)object);
        } else if (object instanceof java.sql.Date) {
            return getString((java.sql.Date)object);
        } else if (object instanceof Date) {
            return getString((Date)object);
        } else {
            return object.toString();
        }
    }

    //
    // matchers
    //

    public static boolean isTargetElement(
        Element element,
        String localName
    ) {
        return localName.equals(element.getTagName());
    }

    public static boolean hasAttributeHungry(RStack stack, String name) {
        Attr attr = stack.getContextElement().getAttributeNode(name);
        if (attr == null) {
            return false;
        }
        if (stack.isConsumedAttribute(attr)) {
            return(false);
        }
        stack.consumeAttribute(attr);
        return true;
    }

    public static String getAttributeHungry(RStack stack, String name) {
        Attr attr = stack.getContextElement().getAttributeNode(name);
        if (attr == null) {
            return null;
        }
        if (stack.isConsumedAttribute(attr)) {
            throw (new IllegalArgumentException());
        }
        stack.consumeAttribute(attr);
        return attr.getValue();
    }

/*
    public static boolean hasAttributeHungry(RStack stack, String name) {
        if (stack.isConsumedAttribute(name)) {
            return(false);
        }
        stack.setConsumedAttribute(name);
        return hasAttribute(stack.getContextElement(), name);
    }

    public static String getAttributeHungry(RStack stack, String name) {
        if (stack.isConsumedAttribute(name)) {
            throw (new IllegalArgumentException());
        }
        stack.setConsumedAttribute(name);
        return getAttribute(stack.getContextElement(), name);
    }
*/

    public static boolean hasAttribute(Element element, String name) {
        String value = getAttribute(element, name);
        return value != null;
    }

    public static String getAttribute(Element element, String name) {
        Attr attr = element.getAttributeNode(name);
        if (attr == null) {
            return null;
        }
        return attr.getValue();
    }

    public static boolean isSequence(RStack stack, String name) {
        Element[] elements = stack.peekElements();
        if (elements == null) {
            return false;
        }
        if (elements.length != 1) {
            return false;
        }
        return name.equals(elements[0].getTagName());
    }

    public static boolean isSequence(RStack stack, String[] names) {
        Element[] elements = stack.peekElements();
        if (elements == null) {
            return false;
        }
        if (elements.length != names.length) {
            return false;
        }
        for (int i = 0;i < names.length;i++) {
            if (!names[i].equals(elements[i].getTagName())) {
                return false;
            }
        }
        return true;
    }

    public static String getElementPropertyAsValue(
        Element element,
        String typeName
    ) {
        if ("string".equals(typeName)) {
            return getElementPropertyAsString(element);
        } else {
            return getElementPropertyAsValueData(element);
        }
    }

    public static String getElementPropertyAsValueData(Element element) {
        return getElementPropertyAsString(element).trim();
    }

    public static String getElementPropertyAsValue(
        Element element,
        String slotName,
        String typeName
    ) {
        if ("string".equals(typeName)) {
            return getElementPropertyAsString(element, slotName);
        } else {
            return getElementPropertyAsValueData(element, slotName);
        }
    }

    public static String getElementPropertyAsValueData(
        Element element,
        String slotName
    ) {
        return getElementPropertyAsString(element, slotName).trim();
    }

    public static String getAttributePropertyAsValue(
        Element element,
        String attrName,
        String typeName
    ) {
        if ("string".equals(typeName)) {
            return getAttributePropertyAsString(element, attrName);
        } else {
            return getAttributePropertyAsValueData(element, attrName);
        }
    }

    public static String getAttributePropertyAsValueData(
        Element element,
        String attrName
    ) {
        String data = getAttributePropertyAsString(element, attrName);
        if (data == null) {
            return null;
        }
        return data.trim();
    }

    public static boolean isMatchDataValues(
        Element element,
        String typeName,
        String value
    ) {
        String data = getElementPropertyAsValue(element, typeName);
        return isMatchDataValues(data, typeName, value);
    }

    public static boolean isMatchDataValues(
        Element element,
        String typeName,
        String value1,
        String value2
    ) {
        String data = getElementPropertyAsValue(element, typeName);
        return isMatchDataValues(data, typeName, value1, value2);
    }

    public static boolean isMatchDataValues(
        Element element,
        String typeName,
        String value1,
        String value2,
        String value3
    ) {
        String data = getElementPropertyAsValue(element, typeName);
        return isMatchDataValues(data, typeName, value1, value2, value3);
    }

    public static boolean isMatchDataValues(
        Element element,
        String typeName,
        String[] values
    ) {
        String data = getElementPropertyAsValue(element, typeName);
        return isMatchDataValues(data, typeName, values);
    }

    public static boolean isMatchDataComplex(
        Element element,
        String typeExpr
    ) {
        String data = getElementPropertyAsValue(element, "string");
        return isMatchDataComplex(data, typeExpr);
    }

    public static boolean isMatchDataValuesElement(
        Element element,
        String elementName,
        String typeName,
        String value
    ) {
        String data
            = getElementPropertyAsValue(element, elementName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value);
    }

    public static boolean isMatchDataValuesElement(
        Element element,
        String elementName,
        String typeName,
        String value1,
        String value2
    ) {
        String data
            = getElementPropertyAsValue(element, elementName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value1, value2);
    }

    public static boolean isMatchDataValuesElement(
        Element element,
        String elementName,
        String typeName,
        String value1,
        String value2,
        String value3
    ) {
        String data
            = getElementPropertyAsValue(element, elementName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value1, value2, value3);
    }

    public static boolean isMatchDataValuesElement(
        Element element,
        String elementName,
        String typeName,
        String[] values
    ) {
        String data
            = getElementPropertyAsValue(element, elementName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, values);
    }

    public static boolean isMatchDataComplexElement(
        Element element,
        String elementName,
        String typeExpr
    ) {
        String data
            = getElementPropertyAsValue(element, elementName, "string");
        if (data == null) {
            return false;
        }
        return isMatchDataComplex(data, typeExpr);
    }

    public static boolean isMatchDataValuesAttr(
        Element element,
        String attrName,
        String typeName,
        String value
    ) {
        String data
            = getAttributePropertyAsValue(element, attrName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value);
    }

    public static boolean isMatchDataValuesAttr(
        Element element,
        String attrName,
        String typeName,
        String value1,
        String value2
    ) {
        String data
            = getAttributePropertyAsValue(element, attrName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value1, value2);
    }

    public static boolean isMatchDataValuesAttr(
        Element element,
        String attrName,
        String typeName,
        String value1,
        String value2,
        String value3
    ) {
        String data
            = getAttributePropertyAsValue(element, attrName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, value1, value2, value3);
    }

    public static boolean isMatchDataValuesAttr(
        Element element,
        String attrName,
        String typeName,
        String[] values
    ) {
        String data
            = getAttributePropertyAsValue(element, attrName, typeName);
        if (data == null) {
            return false;
        }
        return isMatchDataValues(data, typeName, values);
    }

    public static boolean isMatchDataComplexAttr(
        Element element,
        String attrName,
        String typeExpr
    ) {
        String data
            = getAttributePropertyAsValue(element, attrName, "string");
        if (data == null) {
            return false;
        }
        return isMatchDataComplex(data, typeExpr);
    }

    public static boolean isMatchDataValues(
        String data,
        String typeName,
        String value
    ) {
        return value.equals(data);
    }

    public static boolean isMatchDataValues(
        String data,
        String typeName,
        String value1,
        String value2
    ) {
        return value1.equals(data) ||
                value2.equals(data);
    }

    public static boolean isMatchDataValues(
        String data,
        String typeName,
        String value1,
        String value2,
        String value3
    ) {
        return value1.equals(data) ||
                value2.equals(data) ||
                value3.equals(data);
    }

    public static boolean isMatchDataValues(
        String data,
        String typeName,
        String[] values
    ) {
        for (int i = 0;i < values.length;i++) {
            if (values[i].equals(data)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMatchDataComplex(String data, String typeExpr) {
        return true;
    }

    //
    // utilities
    // 

    // XXX
    public static boolean hasElement(Element element, String name) {
        Element[] nodes = getElements(element, name);
        return nodes.length > 0;
    }

    // XXX
    public static boolean hasElement(Element element, String[] names) {
        Element[] elements = getElements(element);
        for (int i = 0;i < elements.length;i++) {
            for (int j = 0;j < names.length;j++) {
                if (names[j].equals(elements[i].getTagName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Element getOnlyElement(Element element, String name)
        throws IllegalArgumentException {

        Element[] nodes = getElements(element, name);
        switch (nodes.length) {

        case 0:
            return null;
        case 1:
            break;
        default:
            throw (new IllegalArgumentException());
        }
        return nodes[0];
    }

    public static Element[] getElements(Element element) {
        NodeList children = element.getChildNodes();
        List<Element> list = new ArrayList<>();
        int size = children.getLength();
        for (int i = 0;i < size;i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element)child);
            }
        }
        Element[] array = new Element[list.size()];
        return (Element[])list.toArray(array);
    }

    public static Element[] getElements(Element element, String name) {
        NodeList children = element.getChildNodes();
        List<Element> list = new ArrayList<>();
        int size = children.getLength();
        for (int i = 0;i < size;i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)child;
                if (name.equals(childElement.getTagName())) {
                    list.add(childElement);
                }
            }
        }
        Element[] array = new Element[list.size()];
        return (Element[])list.toArray(array);
    }

    public static Stack<Node> getElementsAsStack(Element element) {
        NodeList children = element.getChildNodes();
        Stack<Node> stack = new Stack<>();
        int size = children.getLength();
        for (int i = size - 1;i >= 0;i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                stack.push(children.item(i));
            }
        }
        return stack;
    }        

    public static String getStringByMixedList(List list) { // XXX : URString
        StringBuffer buffer = new StringBuffer();
        int size = list.size();
        for (int i = 0;i < size;i++) {
            Object object = list.get(i);
            if (object.getClass().getName().endsWith("RString")) {
                buffer.append(object.toString());
            }
        }
        return new String(buffer);
    }

    public static String getQName(String prefix, String localName) {
        if (prefix == null) {
            return localName;
        } else if ("".equals(prefix)) {
            return localName;
        } else {
            return prefix + ":" + localName;
        }
    }

    public static void makeQName(
        String prefix,
        String localName,
        StringBuffer buffer
    ) {
        if (prefix == null) {
            buffer.append(localName);
        } else if ("".equals(prefix)) {
            buffer.append(localName);
        } else {
            buffer.append(prefix);
            buffer.append(":");
            buffer.append(localName);
        }
    }

    public static void makeQName(
        String prefix,
        String localName,
        Writer buffer
    ) throws IOException {
        if (prefix == null) {
            buffer.write(localName);
        } else if ("".equals(prefix)) {
            buffer.write(localName);
        } else {
            buffer.write(prefix);
            buffer.write(":");
            buffer.write(localName);
        }
    }

    public static void makeQName(
        String prefix,
        String localName,
        PrintWriter buffer
    ) {
        if (prefix == null) {
            buffer.print(localName);
        } else if ("".equals(prefix)) {
            buffer.print(localName);
        } else {
            buffer.print(prefix);
            buffer.print(":");
            buffer.print(localName);
        }
    }

    public static String getNSMapping(String prefix, String uri) {
        if (prefix == null) {
            return "";
        }
        if ("".equals(prefix)) {
            return " xmlns=\"" + uri + "\"";
        }
        return " xmlns:" + prefix + "=\"" + uri + "\"";
    }

    public static String element2Data(Element element) {
        return element2Text(element).trim();
    }

    public static String element2Text(Element element) {
        return node2Text(element);
    }

    public static String nodes2Text(Node[] nodes) {
        StringBuffer buffer = new StringBuffer();
        int nNodes = nodes.length;
        for (int i = 0;i < nNodes;i++) {
            node2Text(nodes[i], buffer);
        }
        return new String(buffer);
    }

    public static String node2Text(Node node) {
        StringBuffer buffer = new StringBuffer();
        node2Text(node, buffer);
        return new String(buffer);
    }

    public static void node2Text(Node node, StringBuffer buffer) {
        switch(node.getNodeType()) {

        case Node.DOCUMENT_NODE:
        case Node.ELEMENT_NODE:
            _nodeChildren2Text(node, buffer);
            break;
        case Node.ENTITY_REFERENCE_NODE:
            EntityReference eref = (EntityReference)node;
            String erefName = eref.getNodeName();
        switch (erefName) {
            case "lt":
                buffer.append("<");
                break;
            case "gt":
                buffer.append(">");
                break;
            case "amp":
                buffer.append("&");
                break;
            case "quot":
                buffer.append("'");
                break;
            case "apos":
                buffer.append("\"");
                break;
            default:
                _nodeChildren2Text(eref, buffer);
                break;
        }
            break;
        case Node.ATTRIBUTE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.COMMENT_NODE:
        case Node.PROCESSING_INSTRUCTION_NODE:
            // do nothing
            break;
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
            Text text = (Text)node;
            buffer.append(text.getData());
            break;
        default:
            throw (new UnsupportedOperationException("not supported yet"));
        }
    }

    private static void _nodeChildren2Text(Node node, StringBuffer buffer) {
        NodeList nodes = node.getChildNodes();
        int nNodes = nodes.getLength();
        for (int i = 0;i < nNodes;i++) {
            node2Text(nodes.item(i), buffer);
        }
    }

    public static Locale makeLocale(String name) {
        try {
            name = name.replace('-', '_'); // XXX
            StringTokenizer st = new StringTokenizer(name, "_");
            switch (st.countTokens()) {
            case 1:
                return new Locale(st.nextToken(), "", "");
            case 2:
                return new Locale(st.nextToken(), st.nextToken(), "");
            case 3:
                return new Locale(
                    st.nextToken(), st.nextToken(), st.nextToken());
            default:
                return null;
            }
        } catch (Exception e) {
            return _invalidLocale(e);
        }
    }
    
    public static URL makeURL4Property(String name) {
        try {
            return new URL(name);
        } catch (Exception e) {
            return _invalidURL(e);
        }
    }

    public static URL makeURL(URL base, URL leaf) {
        if (leaf.getProtocol() != null) {
            return leaf;
        }
        try {
            return new URL(base, leaf.toExternalForm());
        } catch (Exception e) {
            return _invalidURL(e);
        }
    }

    public static URL makeURL(String uri) throws MalformedURLException {
        try {
            return new URL(uri);
        } catch (MalformedURLException e) {
            return new File(uri).toURI().toURL();
        }
    }

    public static String makeUrlString(String base, String leaf) {
        try {
            URL url = new URL(leaf);
            return url.toExternalForm();
        } catch (MalformedURLException e) {
        }
        if (base == null) {
            return leaf;
        }
        if (leaf == null) {
            return base;
        }
        int index = base.lastIndexOf('/');
        if (index == -1) {
            return leaf;
        }
        return base.substring(0, index + 1) + leaf;
    }

    public static String makeUrlString(URL base, String leaf) {
        if (base == null) {
            return leaf;
        }
        return makeUrlString(base.toExternalForm(), leaf);
    }

/*
    // XXX : locale? schema2 problem:
    public static String makeStringAsDate(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static String makeStringAsTime(Date date) {
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        String text = df.format(date);
        int tz = date.getTimezoneOffset();
        tz = 0;                        // XXX
        StringBuffer offset = new StringBuffer();
        if (tz != 0) {
            if (tz > 0) {
                offset.append("+");
            } else {
                offset.append("-");
            }
            int hours = tz / 60;
            int minutes = tz % 60;
            if (hours < 10) {
                offset.append("0");
            }
            offset.append(Integer.toString(hours));
            if (minutes < 10) {
                offset.append("0");
            }
            offset.append(Integer.toString(minutes));
        }
        return text + new String(offset);
    }

    public static Date makeDateByDate(String cdata) throws ParseException {
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Date date = df.parse(cdata);
        return date);
    }

    public static Date makeDateByTime(String cdata) throws ParseException {
        DateFormat odf = new SimpleDateFormat("hh:mm");
        int index = cdata.indexOf("-");
        int offsetValue = 0;
        String timeText = cdata;
        if (index != -1) {
            timeText = cdata.substring(0, index);
            String offsetText = cdata.substring(index + 1, cdata.length());
            Date offset = odf.parse(offsetText);
            offsetValue = (offset.getHours() * 60 + offset.getMinutes()) * -1;
        } else {
            index = cdata.indexOf("+");
            if (index != -1) {
                timeText = cdata.substring(0, index);
                String offsetText = cdata.substring(index + 1, cdata.length());
                Date offset = odf.parse(offsetText);
                offsetValue = (offset.getHours() * 60 + offset.getMinutes());
            }
        }
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Date time = df.parse(timeText);
        // XXX : timezone
        return time);
    }
*/

    public static String makeStringJava(Date date) {
        DateFormat df = DateFormat.getDateInstance();
        return df.format(date);
    }

    public static Date makeDateJava(String cdata) throws ParseException {
        DateFormat df = DateFormat.getDateInstance();
        return df.parse(cdata);
    }

    public static String makeStringAsBASE64(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        int count = 0;
        for (int i = 0;i < bytes.length;i += 3) {
            int data1 = (bytes[i] & 0xFC) >> 2;
            int data2;
            int data3;
            int data4;
            if (i + 1 >= bytes.length) {
                data2 = (bytes[i] & 0x03) << 4;
                data3 = -1;
                data4 = -1;
            } else {
                data2 =
                    ((bytes[i] & 0x03) << 4) |
                    ((bytes[i + 1] & 0xF0) >> 4);
                if (i + 2 >= bytes.length) {
                    data3 = (bytes[i + 1] & 0x0F) << 2;
                    data4 = -1;
                } else {
                    data3 =
                        ((bytes[i + 1] & 0x0F) << 2) |
                        ((bytes[i + 2] & 0xC0) >> 6);
                    data4 = bytes[i + 2] & 0x3F;
                }
            }
            buffer.append(map__[data1]);
            buffer.append(map__[data2]);
            if (data3 == -1) {
                buffer.append("=");
            } else {
                buffer.append(map__[data3]);
            }
            if (data4 == -1) {
                buffer.append("=");
            } else {
                buffer.append(map__[data4]);
            }
            count += 4;
            if (count >= 76) {
                buffer.append("\n");
                count = 0;
            }
        }
        return new String(buffer);
    }

    public static byte[] makeBytesByBASE64(String cdata) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int status = 0;
        int byteData = 0;
        int size = cdata.length();
        for (int i = 0;i < size;i++) {
            int charData = cdata.charAt(i);
            int newData = _calcData(charData);
            if (charData == '=') {
                break;
            }
            if (newData != -1) {
                switch (status) {

                case 0:
                    byteData = newData << 2;
                    status = 1;
                    break;
                case 1:
                    byteData |= (newData & 0x30) >> 4;
                    buffer.write(byteData);
                    byteData = (newData & 0x0F) << 4;
                    status = 2;
                    break;
                case 2:
                    byteData |= (newData & 0x3C) >> 2;
                    buffer.write(byteData);
                    byteData = (newData & 0x03) << 6;
                    status = 3;
                    break;
                case 3:
                    byteData |= (newData);
                    buffer.write(byteData);
                    status = 0;
                    break;
                default:
                    return _invalidBinary("char = " + newData);
                }
            }
        }
        return buffer.toByteArray();
    }

    private static int _calcData(int charData) {
        if ('A' <= charData && charData <= 'Z') {
            return charData - 'A';
        }
        if ('a' <= charData && charData <= 'z') {
            return charData - 'a' + 26;
        }
        if ('0' <= charData && charData <= '9') {
            return charData - '0' + 26 + 26;
        }
        if (charData == '+') {
            return 62;
        }
        if (charData == '/') {
            return 63;
        }
        return -1;
    }

    private static char[] map__ = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static String makeStringAsHEX(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0;i < bytes.length;i++) {
            switch (bytes[i] & 0xF0) {

            case 0x00:
                buffer.append("0");
                break;
            case 0x10:
                buffer.append("1");
                break;
            case 0x20:
                buffer.append("2");
                break;
            case 0x30:
                buffer.append("3");
                break;
            case 0x40:
                buffer.append("4");
                break;
            case 0x50:
                buffer.append("5");
                break;
            case 0x60:
                buffer.append("6");
                break;
            case 0x70:
                buffer.append("7");
                break;
            case 0x80:
                buffer.append("8");
                break;
            case 0x90:
                buffer.append("9");
                break;
            case 0xA0:
                buffer.append("A");
                break;
            case 0xB0:
                buffer.append("B");
                break;
            case 0xC0:
                buffer.append("C");
                break;
            case 0xD0:
                buffer.append("D");
                break;
            case 0xE0:
                buffer.append("E");
                break;
            case 0xF0:
                buffer.append("F");
                break;
            default:
                throw (new IllegalArgumentException());
            }
            switch (bytes[i] & 0x0F) {

            case 0x00:
                buffer.append("0");
                break;
            case 0x01:
                buffer.append("1");
                break;
            case 0x02:
                buffer.append("2");
                break;
            case 0x03:
                buffer.append("3");
                break;
            case 0x04:
                buffer.append("4");
                break;
            case 0x05:
                buffer.append("5");
                break;
            case 0x06:
                buffer.append("6");
                break;
            case 0x07:
                buffer.append("7");
                break;
            case 0x08:
                buffer.append("8");
                break;
            case 0x09:
                buffer.append("9");
                break;
            case 0x0A:
                buffer.append("A");
                break;
            case 0x0B:
                buffer.append("B");
                break;
            case 0x0C:
                buffer.append("C");
                break;
            case 0x0D:
                buffer.append("D");
                break;
            case 0x0E:
                buffer.append("E");
                break;
            case 0x0F:
                buffer.append("F");
                break;
            default:
                throw (new IllegalArgumentException());
            }
        }
        return new String(buffer);
    }

    public static byte[] makeBytesByHEX(String cdata) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int size = cdata.length();
            for (int i = 0;i < size;i += 2) {
                byte byteData;
                switch (cdata.charAt(i)) {

                case '0':
                    byteData = 0x00;
                    break;
                case '1':
                    byteData = 0x10;
                    break;
                case '2':
                    byteData = 0x20;
                    break;
                case '3':
                    byteData = 0x30;
                    break;
                case '4':
                    byteData = 0x40;
                    break;
                case '5':
                    byteData = 0x50;
                    break;
                case '6':
                    byteData = 0x60;
                    break;
                case '7':
                    byteData = 0x70;
                    break;
                case '8':
                    byteData = (byte)0x80;
                    break;
                case '9':
                    byteData = (byte)0x90;
                    break;
                case 'a':
                case 'A':
                    byteData = (byte)0xA0;
                    break;
                case 'b':
                case 'B':
                    byteData = (byte)0xB0;
                    break;
                case 'c':
                case 'C':
                    byteData = (byte)0xC0;
                    break;
                case 'd':
                case 'D':
                    byteData = (byte)0xD0;
                    break;
                case 'e':
                case 'E':
                    byteData = (byte)0xE0;
                    break;
                case 'f':
                case 'F':
                    byteData = (byte)0xF0;
                    break;
/*
                case ' ':
                case '\t':
                case '\f':
                case '\n':
                case '\r':
                    // do nothing
                    break;
*/
                default:
                    return _invalidBinary("char = " + cdata.charAt(i));
                }
                switch (cdata.charAt(i + 1)) {

                case '0':
                    byteData |= 0x00;
                    break;
                case '1':
                    byteData |= 0x01;
                    break;
                case '2':
                    byteData |= 0x02;
                    break;
                case '3':
                    byteData |= 0x03;
                    break;
                case '4':
                    byteData |= 0x04;
                    break;
                case '5':
                    byteData |= 0x05;
                    break;
                case '6':
                    byteData |= 0x06;
                    break;
                case '7':
                    byteData |= 0x07;
                    break;
                case '8':
                    byteData |= 0x08;
                    break;
                case '9':
                    byteData |= 0x09;
                    break;
                case 'a':
                case 'A':
                    byteData |= 0x0A;
                    break;
                case 'b':
                case 'B':
                    byteData |= 0x0B;
                    break;
                case 'c':
                case 'C':
                    byteData |= 0x0C;
                    break;
                case 'd':
                case 'D':
                    byteData |= 0x0D;
                    break;
                case 'e':
                case 'E':
                    byteData |= 0x0E;
                    break;
                case 'f':
                case 'F':
                    byteData |= 0x0F;
                    break;
/*
                case ' ':
                case '\t':
                case '\f':
                case '\n':
                case '\r':
                    // do nothing
                    break;
*/
                default:
                    return _invalidBinary("char = " + cdata.charAt(i));
                }
                buffer.write(byteData);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return _invalidBinary(e);
        }
        return buffer.toByteArray();
    }

    // Auto

    private static Object autoIDHandler__ = null;
    private static String autoIDPrefix__ = null;
    private static Boolean autoIDThreadSingle__ = null;
    private static Boolean autoIDThreadGroupSingle__ = null;

    public static void setAutoIDHandler(Object handler) {
        autoIDHandler__ = handler;
    }

    public static void setAutoIDPrefix(String prefix) {
        autoIDPrefix__ = prefix;
    }

    public static String getAutoIDPrefix() {
        return autoIDPrefix__;
    }

    public static void setAutoIDThreadSingle(Boolean threadSingle) {
        autoIDThreadSingle__ = threadSingle;
    }

    public static Boolean getAutoIDThreadSingle() {
        return autoIDThreadSingle__;
    }

    public static void setAutoIDThreadGroupSingle(Boolean threadGroupSingle) {
        autoIDThreadGroupSingle__ = threadGroupSingle;
    }

    public static Boolean getAutoIDThreadGroupSingle() {
        return autoIDThreadGroupSingle__;
    }

    public static String makeAutoID() {
        if (autoIDHandler__ != null) {
            return autoIDHandler__.toString();
        } else {
            return _makeDefaultAutoID();
        }
    }

    private static String _makeDefaultAutoID() {
        String prefix = _getAutoIDPrefix();
        StringBuffer buffer = new StringBuffer();
        if (prefix != null) {
            buffer.append(prefix);
        }
        buffer.append(Long.toHexString(System.currentTimeMillis()));
        if (!_isAutoIDSingleThread()) {
            Thread thread = Thread.currentThread();
            buffer.append("00");
            _hashAppend(thread.getName(), buffer);
            if (!_isAutoIDSingleThreadGroup()) {
                buffer.append("00");
                _hashAppend(thread.getThreadGroup().getName(), buffer);
            }
        }
        return new String(buffer);
    }

    private static void _hashAppend(String string, StringBuffer buffer) {
        int size = string.length();
        for (int i = 0;i < size;i++) {
            char c = string.charAt(i);
            buffer.append(Integer.toHexString(c));
        }
    }

    private static String _getAutoIDPrefix() {
        String prefix = getAutoIDPrefix();
        if (prefix != null) {
            return prefix;
        }
        return _getPropertyPrefix();
    }

    private static boolean _isAutoIDSingleThread() {
        Boolean value = getAutoIDThreadSingle();
        if (value != null) {
            return value.booleanValue();
        }
        return _isPropertySingleThread();
    }

    private static boolean _isAutoIDSingleThreadGroup() {
        Boolean value = getAutoIDThreadGroupSingle();
        if (value != null) {
            return value.booleanValue();
        }
        return _isPropertySingleThreadGroup();
    }

    private static String _getPropertyPrefix() {
        return System.getProperty("org.relaxer.autoid.prefix");
    }        

    private static boolean _isPropertySingleThread() {
        return 
            "true".equals(
                System.getProperty("org.relaxer.autoid.thread.single")
            );
    }

    private static boolean _isPropertySingleThreadGroup() {
        return 
            "true".equals(
                System.getProperty("org.relaxer.autoid.threadGroup.single")
            );
    }

    public static java.sql.Timestamp makeAutoSQLTimestamp() {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }

    public static java.sql.Time makeAutoSQLTime() {
        return new java.sql.Time(System.currentTimeMillis());
    }

    public static java.sql.Date makeAutoSQLDate() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    // JavaBeans

    public static Object getPropertyObject(String value) {
        return value;
    }

    public static Object getPropertyObject(boolean value) {
        return new Boolean(value);
    }

    public static Object getPropertyObject(byte value) {
        return new Byte(value);
    }

    public static Object getPropertyObject(short value) {
        return new Short(value);
    }

    public static Object getPropertyObject(int value) {
        return new Integer(value);
    }

    public static Object getPropertyObject(long value) {
        return new Long(value);
    }

    public static Object getPropertyObject(float value) {
        return new Float(value);
    }

    public static Object getPropertyObject(double value) {
        return new Double(value);
    }

    public static Object getPropertyObject(Object value) {
        return value;
    }

    public static Object getPropertyObject(Collection value) {
        return value.toArray();
    }

    // XML

    public static String doc2String4Print(Document doc) {
        StringBuffer buffer = new StringBuffer();
        Element element = doc.getDocumentElement();
        buffer.append("<?xml version='1.0' ?>\n");
        _node2String4Print(element, "", buffer);
        return new String(buffer);
    }

    public static String doc2String4Print(Document doc, String encoding) {
        StringBuffer buffer = new StringBuffer();
        Element element = doc.getDocumentElement();
        buffer.append("<?xml version='1.0' encoding='");
        buffer.append(encoding);
        buffer.append("' ?>\n");
        _node2String4Print(element, "", buffer);
        return new String(buffer);
    }

    public static String node2String4Print(Node node, String encoding) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0' encoding='");
        buffer.append(encoding);
        buffer.append("' ?>\n");
        _node2String4Print(node, "", buffer);
        return new String(buffer);
    }

    public static String node2String4Print(Node node) {
        return _node2String4Print(node, "");
    }

    protected static String _node2String4Print(Node node, String indent) {
        StringBuffer buffer = new StringBuffer();
        _node2String4Print(node, indent, buffer);
        return new String(buffer);
    }

    protected static void _node2String4Print(
        Node node,
        String indent,
        StringBuffer buffer
    ) {
        switch(node.getNodeType()) {

        case Node.ELEMENT_NODE: {
            Element element = (Element)node;
            String tag = element.getTagName();
            buffer.append(indent);
            buffer.append("<");
            buffer.append(tag);
            NamedNodeMap attrs = element.getAttributes();
            int nAttrs = attrs.getLength();
            for (int i = 0;i < nAttrs;i++) {
                Attr attr = (Attr)attrs.item(i);
                buffer.append(' ');
                buffer.append(attr.getName());
                buffer.append("=\"");
                buffer.append(attr.getValue());
                buffer.append('\"');
            }
            buffer.append(">");
            boolean needIndent = hasChildElement(element);
            if (needIndent) {
                buffer.append("\n");
            }
            NodeList nodes = element.getChildNodes();
            int nNodes = nodes.getLength();
            for (int i = 0;i < nNodes;i++) {
                Node child = nodes.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    // XXX : xml:space
                    Text text = (Text)child;
                    if (!isBlankText(text)) {
                        buffer.append(text.getData());
                    }
                } else {
                    _node2String4Print(child, indent + "  ", buffer);
                }
            }
            if (needIndent) {
                buffer.append(indent);
            }
            buffer.append("</" + tag + ">\n");
            break;
        }
        case Node.ATTRIBUTE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.TEXT_NODE:
            Text text = (Text)node;
            buffer.append(text.getData());
            break;
        case Node.CDATA_SECTION_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.ENTITY_REFERENCE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.PROCESSING_INSTRUCTION_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.COMMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.DOCUMENT_NODE: {
            NodeList nodes = node.getChildNodes();
            int nNodes = nodes.getLength();
            for (int i = 0;i < nNodes;i++) {
                _node2String4Print(nodes.item(i), indent, buffer);
            }
            break;
        }
        case Node.DOCUMENT_TYPE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.DOCUMENT_FRAGMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.NOTATION_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        default:
            throw (new UnsupportedOperationException("not supported yet"));
        }
    }

    public static String doc2String4Data(Document doc) {
        StringBuffer buffer = new StringBuffer();
        Element element = doc.getDocumentElement();
        buffer.append("<?xml version='1.0' ?>");
        _node2String4Data(element, buffer);
        return new String(buffer);
    }

    public static String node2String4Data(Node node) {
        StringBuffer buffer = new StringBuffer();
        _node2String4Data(node, buffer);
        return new String(buffer);
    }

    private static void _node2String4Data(Node node, StringBuffer buffer) {
        switch(node.getNodeType()) {

        case Node.DOCUMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.ELEMENT_NODE:
            Element element = (Element)node;
            String tag = element.getTagName();
            buffer.append('<');
            buffer.append(tag);
            NamedNodeMap attrs = element.getAttributes();
            int size = attrs.getLength();
            for (int i = 0;i < size;i++) {
                Attr attr = (Attr)attrs.item(i);
                buffer.append(' ');
                buffer.append(attr.getName());
                buffer.append("=\"");
                buffer.append(attr.getValue());
                buffer.append('\"');
            }
            buffer.append('>');
            NodeList nodes = element.getChildNodes();
            int nNodes = nodes.getLength();
            for (int i = 0;i < nNodes;i++) {
                _node2String4Data(nodes.item(i), buffer);
            }
            buffer.append("</");
            buffer.append(tag);
            buffer.append('>');
            break;
        case Node.ATTRIBUTE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.COMMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
            Text text = (Text)node;
            buffer.append(text.getData());
            break;
        default:
            throw (new UnsupportedOperationException("not supported yet"));
        }
    }

    public static boolean hasChildElement(Element element) {
        NodeList nodes = element.getChildNodes();
        int nNodes = nodes.getLength();
        for (int i = 0;i < nNodes;i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlankText(Text text) {
        String data = text.getData();
        char[] chars = data.toCharArray();
        for (int i = 0;i < chars.length;i++) {
            if (!isSpace(chars[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSpace(char c) {
        switch (c) {

        case ' ':
        case '\t':
        case '\r':
        case '\n':
            return true;
        default:
            return false;
        }
    }

/*
    public static class RelativeStreamHandler
        extends java.net.URLStreamHandler {

        protected URLConnection openConnection(URL u) throws IOException {
            throw (new IOException("UnsupportedOperation"));
        }

        protected void parseURL(URL u, String spec, int start, int limit) {
            String protocol;
            String file;
            String ref;
            int afterProtocol = spec.indexOf(":");
            protocol = spec.substring(0, afterProtocol);
            int afterFile = spec.indexOf("#");
            if (afterFile == -1) {
                file =  spec.substring(afterProtocol + 1);
                ref = null;
            } else {
                file = spec.substring(afterProtocol + 1, afterFile);
                ref = spec.substring(afterFile + 1);
            }
            setURL(u, protocol, null, -1, file, ref);
        }

        protected String toExternalForm(URL u) {
            return u.getFile();
        }
    }
*/
    //
    static boolean isRigid__ = true;
    static boolean isBadNumber__ = true;

    private static boolean _invalidBooleanValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return false;
        }
    }

    private static byte _invalidByteValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static short _invalidShortValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static int _invalidIntValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static long _invalidLongValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static float _invalidFloatValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return Float.NaN;
            } else {
                return 0;
            }
        }
    }

    private static double _invalidDoubleValue(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            if (isBadNumber__) {
                return Double.NaN;
            } else {
                return 0;
            }
        }
    }

    private static Boolean _invalidBooleanObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Byte _invalidByteObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Short _invalidShortObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Integer _invalidIntegerObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Long _invalidLongObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Float _invalidFloatObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Double _invalidDoubleObject(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static BigDecimal _invalidBigDecimal(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static BigInteger _invalidBigInteger(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Date _invalidDate(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static Locale _invalidLocale(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static URL _invalidURL(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static java.sql.Timestamp _invalidSQLTimestamp(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static java.sql.Time _invalidSQLTime(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static java.sql.Date _invalidSQLDate(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }

    private static byte[] _invalidBinary(Object value) {
        if (isRigid__) {
            throw (new IllegalArgumentException(value.toString()));
        } else {
            return null;
        }
    }
}
