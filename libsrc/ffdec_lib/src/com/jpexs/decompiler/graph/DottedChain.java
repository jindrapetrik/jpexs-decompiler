/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DottedChain implements Serializable {

    public static final DottedChain EMPTY = new DottedChain(true);

    public static final DottedChain TOPLEVEL = new DottedChain();

    public static final DottedChain BOOLEAN = new DottedChain("Boolean");

    public static final DottedChain STRING = new DottedChain("String");

    public static final DottedChain ARRAY = new DottedChain("Array");

    public static final DottedChain NUMBER = new DottedChain("Number");

    public static final DottedChain OBJECT = new DottedChain("Object");

    public static final DottedChain INT = new DottedChain("int");

    public static final DottedChain UINT = new DottedChain("uint");

    public static final DottedChain UNDEFINED = new DottedChain("Undefined");

    public static final DottedChain XML = new DottedChain("XML");

    public static final DottedChain NULL = new DottedChain("null");

    public static final DottedChain FUNCTION = new DottedChain("Function");

    public static final DottedChain VOID = new DottedChain("void");

    public static final DottedChain NAMESPACE = new DottedChain("Namespace");

    public static final DottedChain ALL = new DottedChain("*");

    private final String[] parts;

    private final int length;

    private final int hash;
    private boolean isNull = false;

    public static final DottedChain parse(String name) {
        if (name == null) {
            return DottedChain.EMPTY;
        } else if (name.isEmpty()) {
            return DottedChain.TOPLEVEL;
        } else {
            return new DottedChain(name.split("\\."));
        }
    }

    private DottedChain(boolean isNull) {
        this.isNull = isNull;
        this.parts = new String[0];
        this.length = 0;
        this.hash = 0;
    }

    public DottedChain(DottedChain src) {
        this(src.parts);
        this.isNull = src.isNull;
    }

    public DottedChain(List<String> parts) {
        length = parts.size();
        this.parts = parts.toArray(new String[length]);
        hash = calcHash();
    }

    public DottedChain(String... parts) {
        if (parts.length == 1 && parts[0].isEmpty()) {
            length = 0;
            this.parts = new String[0];
        } else {
            length = parts.length;
            this.parts = parts;
        }
        hash = calcHash();
    }

    private DottedChain(String[] parts, int length) {
        this.length = length;
        this.parts = parts;
        hash = calcHash();
    }

    public boolean isTopLevel() {
        return !isNull && length == 0;
    }

    public boolean isEmpty() {
        return isNull;
    }

    public int size() {
        return length;
    }

    public String get(int index) {
        if (index >= length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return parts[index];
    }

    public DottedChain subChain(int count) {
        if (count > length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return new DottedChain(parts, count);
    }

    public String getLast() {
        if (isNull) {
            return null;
        }
        if (length == 0) {
            return "";
        } else {
            return parts[length - 1];
        }
    }

    public DottedChain getWithoutLast() {
        if (isNull) {
            return null;
        }
        if (length < 2) {
            return EMPTY;
        }

        return new DottedChain(parts, length - 1);
    }

    public DottedChain add(String name) {
        if (name == null) {
            return new DottedChain(this);
        }
        String[] nparts = new String[length + 1];
        if (length > 0) {
            System.arraycopy(parts, 0, nparts, 0, length);
        }

        nparts[nparts.length - 1] = name;
        return new DottedChain(nparts);
    }

    protected String toString(boolean as3, boolean raw) {
        if (isNull) {
            return "";
        }
        if (length == 0) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                ret.append(".");
            }

            String part = parts[i];
            boolean lastStar = i == length - 1 && "*".equals(part);
            ret.append((raw || lastStar) ? part : IdentifiersDeobfuscation.printIdentifier(as3, part));
        }
        return ret.toString();
    }

    public String toFilePath() {
        if (isNull) {
            return "";
        }
        if (length == 0) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                ret.append(File.separator);
            }

            ret.append(Helper.makeFileName(IdentifiersDeobfuscation.printIdentifier(true, parts[i])));
        }
        return ret.toString();
    }

    public List<String> toList() {
        return new ArrayList<>(Arrays.asList(parts));
    }

    public String toPrintableString(boolean as3) {
        return toString(as3, false);
    }

    public String toRawString() {
        return toString(false/*ignored*/, true);
    }

    @Override
    public String toString() {
        return toRawString();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private int calcHash() {
        if (isNull) {
            return 0;
        }
        int result = 1;
        for (int i = 0; i < length; i++) {
            result = 31 * result + parts[i].hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DottedChain other = (DottedChain) obj;
        if (isNull && other.isNull) {
            return true;
        }
        if (length != other.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            String s1 = parts[i];
            String s2 = other.parts[i];
            if (!s1.equals(s2)) {
                return false;
            }
        }

        return true;
    }
}
