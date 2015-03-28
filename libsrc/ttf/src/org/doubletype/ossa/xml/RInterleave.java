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
import java.net.URL;
import java.math.*;
import java.lang.reflect.*;
import java.sql.Time;
import java.sql.Timestamp;
import org.w3c.dom.*;

/**
 * RInterleave
 *
 * @since   May. 13, 2002
 * @version Oct. 22, 2003
 * @author  ASAMI, Tomoharu (asami@relaxer.org)
 */
public final class RInterleave {
    private RStack rstack_;
    private List<Entry> entries_ = new ArrayList<>();
    private Map<Class,Entry> entryByStateClass_ = new HashMap<>();
    private Map<String,Entry> entryByElementName_ = new HashMap<>();
    private Boolean isMatch_ = null;

    public RInterleave(RStack rstack) {
        rstack_ = rstack;
    }

    public Object getProperty(Class stateClass) {
        StateClassEntry entry = _getEntryByStateClass(stateClass);
        return (entry.getObject());
    }

    public Object[] getPropertyList(Class stateClass) {
        StateClassEntry entry = _getEntryByStateClass(stateClass);
        return (entry.getObjects());
    }

    public String getElementPropertyAsString(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsString(element));
    }

    public boolean getElementPropertyAsBoolean(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (false);
        }
        return (URelaxer.getElementPropertyAsBoolean(element));
    }

    public Boolean getElementPropertyAsBooleanObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsBooleanObject(element));
    }

    public byte getElementPropertyAsByte(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (-1);
        }
        return (URelaxer.getElementPropertyAsByte(element));
    }

    public Byte getElementPropertyAsByteObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsByteObject(element));
    }

    public short getElementPropertyAsShort(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (-1);
        }
        return (URelaxer.getElementPropertyAsShort(element));
    }

    public Short getElementPropertyAsShortObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsShortObject(element));
    }

    public int getElementPropertyAsInt(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (-1);
        }
        return (URelaxer.getElementPropertyAsInt(element));
    }

    public Integer getElementPropertyAsIntObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsIntObject(element));
    }

    public long getElementPropertyAsLong(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (-1);
        }
        return (URelaxer.getElementPropertyAsLong(element));
    }

    public Long getElementPropertyAsLongObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsLongObject(element));
    }

    public float getElementPropertyAsFloat(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (Float.NaN);
        }
        return (URelaxer.getElementPropertyAsFloat(element));
    }

    public Float getElementPropertyAsFloatObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsFloatObject(element));
    }

    public double getElementPropertyAsDouble(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (Double.NaN);
        }
        return (URelaxer.getElementPropertyAsDouble(element));
    }

    public Double getElementPropertyAsDoubleObject(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsDoubleObject(element));
    }

    public BigDecimal getElementPropertyAsBigDecimal(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsBigDecimal(element));
    }

    public BigInteger getElementPropertyAsBigInteger(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsBigInteger(element));
    }

    public Date getElementPropertyAsDate(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsDate(element));
    }

    public Locale getElementPropertyAsLocale(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsLocale(element));
    }

    public URL getElementPropertyAsURL(String elementName) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsURL(element));
    }

    public java.sql.Timestamp getElementPropertyAsSQLTimestamp(
        String elementName
    ) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsSQLTimestamp(element));
    }

    public java.sql.Time getElementPropertyAsSQLTime(
        String elementName
    ) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsSQLTime(element));
    }

    public java.sql.Date getElementPropertyAsSQLDate(
        String elementName
    ) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsSQLDate(element));
    }

    public byte[] getElementPropertyAsBinaryBASE64(
        String elementName
    ) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsBinaryBASE64(element));
    }

    public byte[] getElementPropertyAsBinaryHEX(
        String elementName
    ) {
        Element element = _getElementByElementName(elementName);
        if (element == null) {
            return (null);
        }
        return (URelaxer.getElementPropertyAsBinaryHEX(element));
    }

    public List getElementPropertyAsStringList(String elementName) {
        List list = _getElementListByElementName(elementName);
        int size = list.size();
        List<String> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsString(element));
        }
        return (result);
    }

    public List<Boolean> getElementPropertyAsBooleanList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Boolean> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsBooleanObject(element));
        }
        return (result);
    }

    public List<Byte> getElementPropertyAsByteList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Byte> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsByteObject(element));
        }
        return (result);
    }

    public List<Short> getElementPropertyAsShortList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Short> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsShortObject(element));
        }
        return (result);
    }

    public List<Integer> getElementPropertyAsIntList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Integer> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsIntObject(element));
        }
        return (result);
    }

    public List<Long> getElementPropertyAsLongList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Long> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsLongObject(element));
        }
        return (result);
    }

    public List<Float> getElementPropertyAsFloatList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Float> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsFloatObject(element));
        }
        return (result);
    }

    public List<Double> getElementPropertyAsDoubleList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Double> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsDoubleObject(element));
        }
        return (result);
    }

    public List<BigDecimal> getElementPropertyAsBigDecimalList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<BigDecimal> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsBigDecimal(element));
        }
        return (result);
    }

    public List<BigInteger> getElementPropertyAsBigIntegerList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<BigInteger> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsBigInteger(element));
        }
        return (result);
    }

    public List<Date> getElementPropertyAsDateList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Date> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsDate(element));
        }
        return (result);
    }

    public List<Locale> getElementPropertyAsLocaleList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Locale> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsLocale(element));
        }
        return (result);
    }

    public List<URL> getElementPropertyAsURLList(String elementName) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<URL> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsURL(element));
        }
        return (result);
    }

    public List<Timestamp> getElementPropertyAsSQLTimestampList(
        String elementName
    ) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Timestamp> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsSQLTimestamp(element));
        }
        return (result);
    }

    public List<Time> getElementPropertyAsSQLTimeList(
        String elementName
    ) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Time> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsSQLTime(element));
        }
        return (result);
    }

    public List<Date> getElementPropertyAsSQLDateList(
        String elementName
    ) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<Date> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsSQLDate(element));
        }
        return (result);
    }

    public List<byte[]> getElementPropertyAsBinaryBASE64List(
        String elementName
    ) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<byte[]> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsBinaryBASE64(element));
        }
        return (result);
    }

    public List<byte[]> getElementPropertyAsBinaryHEXList(
        String elementName
    ) {
        List<Element> list = _getElementListByElementName(elementName);
        int size = list.size();
        List<byte[]> result = new ArrayList<>();
        for (int i = 0;i < size;i++) {
            Element element = (Element)list.get(i);
            result.add(URelaxer.getElementPropertyAsBinaryHEX(element));
        }
        return (result);
    }

    private StateClassEntry _getEntryByStateClass(Class stateClass) {
        if (!isMatch()) {
            throw (new IllegalStateException());
        }
        StateClassEntry entry
            = (StateClassEntry)entryByStateClass_.get(stateClass);
        if (entry == null) {
            throw (new IllegalStateException());
        }
        return (entry);
    }

    private ElementNameEntry _getEntryByElementName(String elementName) {
        if (!isMatch()) {
            throw (new IllegalStateException());
        }
        ElementNameEntry entry
            = (ElementNameEntry)entryByElementName_.get(elementName);
        if (entry == null) {
            throw (new IllegalStateException());
        }
        return (entry);
    }

    private Element _getElementByElementName(String elementName) {
        ElementNameEntry entry = _getEntryByElementName(elementName);
        if (entry.elements.size() == 0) {
            return (null);
        } else {
            return ((Element)entry.elements.get(0));
        }
    }

    private List<Element> _getElementListByElementName(String elementName) {
        ElementNameEntry entry = _getEntryByElementName(elementName);
        return (entry.elements);
    }

    public void addElementSlot(String elementName, String occurs) {
        Entry entry = new ElementNameEntry(elementName, occurs);
        entries_.add(entry);
        entryByElementName_.put(elementName, entry);
    }

    public void addElementSlot(Class stateClass, String occurs) {
        Entry entry = new StateClassEntry(stateClass, occurs);
        entries_.add(entry);
        entryByStateClass_.put(stateClass, entry);
    }

    public void addHedgeSlot(Class stateClass, String occurs) {
        Entry entry = new StateClassEntry(stateClass, occurs);
        entries_.add(entry);
        entryByStateClass_.put(stateClass, entry);
    }

    public void addAttributeSlot(Class stateClass, String occurs) {
        Entry entry = new StateClassEntry(stateClass, occurs);
        entries_.add(entry);
        entryByStateClass_.put(stateClass, entry);
    }

    public boolean isMatch() {
        if (isMatch_ == null) {
            isMatch_ = new Boolean(_isMatch());
        }
        return (isMatch_.booleanValue());
    }

    private boolean _isMatch() {
        Entry[] entries = _getEntries();
        for (;;) {
            if (!_isMatchHungry(entries)) {
                if (_isNoMatch(entries)) {
                    return (false);
                }
                return (_isStable(entries));
            }
            if (_isUnmatch(entries)) {
                return (false);
            }
        }
    }

    private Entry[] _getEntries() {
        int size = entries_.size();
        Entry[] entries = new Entry[size];
        for (int i = 0;i < size;i++) {
            entries[i] = (Entry)entries_.get(i);
        }
        return (entries);
    }

    private boolean _isNoMatch(Entry[] entries) {
        for (int i = 0;i < entries.length;i++) {
            if (entries[i].count > 0) {
                return (false);
            }
        }
        return (true);
    }

    private boolean _isStable(Entry[] entries) {
        for (int i = 0;i < entries.length;i++) {
            Entry entry = entries[i];
            switch (entry.occurs) {
                case "":
                    if (entry.count != 1) {
                        return (false);
                    }   break;
                case "?":
                    break;
                case "+":
                    if (entry.count == 0) {
                        return (false);
                    }   break;
                case "*":
                    break;
                default:
                    throw (new InternalError());
            }
        }
        return (true);
    }

    private boolean _isUnmatch(Entry[] entries) {
        for (int i = 0;i < entries.length;i++) {
            Entry entry = entries[i];
            switch (entry.occurs) {
                case "":
                    if (entry.count > 1) {
                        return (true);
                    }   break;
                case "?":
                    if (entry.count > 1) {
                        return (true);
                    }   break;
                case "+":
                    break;
                case "*":
                    break;
                default:
                    throw (new InternalError());
            }
        }
        return (false);
    }

    private boolean _isMatchHungry(Entry[] entries) {
        for (int i = 0;i < entries.length;i++) {
            Entry entry = entries[i];
            if (entry.isMatchHungry(rstack_)) {
                return (true);
            }
        }
        return (false);
    }

    static abstract class Entry {
        public String occurs;
        public int count = 0;

        protected Entry(String occurs) {
            this.occurs = occurs;
        }

        public abstract boolean isMatchHungry(RStack stack);
    }

    static class StateClassEntry extends Entry {
        public Class<?> stateClass;
        public List<RStack> stacks = new ArrayList<>();

        public StateClassEntry(Class<?> stateClass, String occurs) {
            super(occurs);
            this.stateClass = stateClass;
        }

        public boolean isMatchHungry(RStack stack) {
//System.out.println("enter:isMatchHungry [" + stateClass + "] - " + stack);
            try {
                RStack backup = stack.makeClone();
                Method method = stateClass.getMethod(
                    "isMatchHungry",
                    new Class[] { RStack.class }
                );
                Boolean result = (Boolean)method.invoke(
                    null,
                    new Object[] { stack }
                );
                boolean match = result.booleanValue();
                if (match) {
                    count++;
                    stacks.add(backup);
                }
//System.out.println("leave:isMatchHungry [" + stateClass + "]- " + stack + " = " + match);
                return (match);
            } catch (    NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw (new IllegalArgumentException());
            }
        }

        public Object getObject() {
            if (stacks.size() == 0) {
                return (null);
            }
            RStack stack = (RStack)stacks.get(0);
            try {
                Constructor constructor = stateClass.getConstructor(
                    new Class[] { RStack.class }
                );
                Object result = constructor.newInstance(
                    new Object[] { stack }
                );
                return (result);
            } catch (    NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw (new IllegalArgumentException());
            }
        }

        public Object[] getObjects() {
            int size = stacks.size();
            Object array = Array.newInstance(stateClass, size);
            for (int i = 0;i < size;i++) {
                RStack stack = (RStack)stacks.get(i);
                try {
                    Constructor constructor = stateClass.getConstructor(
                        new Class[] { RStack.class }
                    );
                    Object result = constructor.newInstance(
                        new Object[] { stack }
                    );
                    Array.set(array, i, result);
                } catch (        NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw (new IllegalArgumentException());
                }
            }
            return ((Object[])array);
        }
    }

    static class ElementNameEntry extends Entry {
        public String elementName;
        public List<Element> elements = new ArrayList<>();

        public ElementNameEntry(String elementName, String occurs) {
            super(occurs);
            this.elementName = elementName;
        }

        public boolean isMatch(Element element) {
            String localName = element.getTagName(); // DOM1
            int index = localName.indexOf(':');
            if (index != -1) {
                localName = localName.substring(index + 1);
            }
            return (elementName.equals(localName));
        }

        public boolean isMatchHungry(RStack stack) {
            Element element = stack.peekElement();
            if (element == null) {
                return (false);
            }
            boolean result = isMatch(element);
            if (result) {
                stack.popElement();
                count++;
                elements.add(element);
            }
            return (result);
        }
    }
}
