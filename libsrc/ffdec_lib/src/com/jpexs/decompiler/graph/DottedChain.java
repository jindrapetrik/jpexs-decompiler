/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
public class DottedChain implements Serializable, Comparable<DottedChain> {

    public static final DottedChain EMPTY = new DottedChain(true);
    
    public static final DottedChain UNBOUNDED = new DottedChain(new String[]{"*"});

    public static final DottedChain TOPLEVEL = new DottedChain(new String[]{});

    public static final DottedChain BOOLEAN = new DottedChain(new String[]{"Boolean"});

    public static final DottedChain STRING = new DottedChain(new String[]{"String"});

    public static final DottedChain ARRAY = new DottedChain(new String[]{"Array"});

    public static final DottedChain NUMBER = new DottedChain(new String[]{"Number"});

    public static final DottedChain OBJECT = new DottedChain(new String[]{"Object"});

    public static final DottedChain INT = new DottedChain(new String[]{"int"});

    public static final DottedChain UINT = new DottedChain(new String[]{"uint"});

    public static final DottedChain UNDEFINED = new DottedChain(new String[]{"Undefined"});

    public static final DottedChain XML = new DottedChain(new String[]{"XML"});

    public static final DottedChain NULL = new DottedChain(new String[]{"null"});

    public static final DottedChain FUNCTION = new DottedChain(new String[]{"Function"});

    public static final DottedChain VOID = new DottedChain(new String[]{"void"});

    public static final DottedChain NAMESPACE = new DottedChain(new String[]{"Namespace"});

    public static final DottedChain ALL = new DottedChain(new String[]{"*"});

    private final String[] parts;
    
    private final boolean[] attributes;

    private final int length;

    private final int hash;

    private boolean isNull = false;

    private String[] namespaceSuffixes;

    public String getNamespaceSuffix(int index) {
        return namespaceSuffixes[index];
    }

    public String getLastNamespaceSuffix() {
        if (length == 0) {
            return "";
        }
        return namespaceSuffixes[length - 1];
    }

    
    public static final DottedChain parseWithSuffix(String name) {
        if (name == null) {
            return DottedChain.EMPTY;
        } else if (name.isEmpty()) {
            return DottedChain.TOPLEVEL;
        } else {
            String parts[] = name.split("\\.");
            String nsSuffixes[] = new String[parts.length];
            int i = 0;
            for (String part : parts) {
                String nameNoSuffix = part;
                String namespaceSuffix = "";
                if (part.matches(".*#[0-9]+$")) {
                    nameNoSuffix = part.substring(0, part.lastIndexOf("#"));
                    namespaceSuffix = part.substring(part.lastIndexOf("#"));
                }             
                parts[i] = nameNoSuffix;
                nsSuffixes[i] = namespaceSuffix;
                i++;
            }
            
            return new DottedChain(parts, nsSuffixes);
        }
    }

    private DottedChain(boolean isNull) {
        this.isNull = isNull;
        this.parts = new String[0];
        this.length = 0;
        this.hash = 0;
        this.attributes = new boolean[0];
        this.namespaceSuffixes = new String[0];
    }

    public DottedChain(DottedChain src) {
        this(src.parts, src.namespaceSuffixes);
        this.isNull = src.isNull;
    }

    public DottedChain(String[] parts) {
        this(Arrays.asList(parts));
    }
    
    public DottedChain(List<String> parts) {
        length = parts.size();
        this.parts = parts.toArray(new String[length]);
        attributes = new boolean[length];
        hash = calcHash();
        this.namespaceSuffixes = new String[length];
        for(int i = 0; i < length; i++) {
            namespaceSuffixes[i] = "";
        }
    }

    /*public DottedChain(String onePart, String namespaceSuffix) {
        this(new String[]{onePart}, namespaceSuffix);
    }*/
    public DottedChain(String[] parts, String[] namespaceSuffixes) {
        this(new boolean[parts.length], parts, namespaceSuffixes);
    }
    
    public DottedChain(boolean attributes[], String[] parts, String[] namespaceSuffixes) {
        if (parts.length == 1 && parts[0].isEmpty()) {
            length = 0;
            this.parts = new String[0];        
        } else {
            length = parts.length;
            this.parts = parts;            
        }
        this.attributes = attributes;
        hash = calcHash();
        this.namespaceSuffixes = namespaceSuffixes;
    }

    private DottedChain(String[] parts, int length) {
        this.length = length;
        this.parts = parts;
        attributes = new boolean[length];
        this.namespaceSuffixes = new String[length];
        for(int i = 0; i < length; i++) {
            namespaceSuffixes[i] = "";
        }
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
    
    public boolean isAttribute(int index) {
        if (index >= length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return attributes[index];
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
    
    public boolean isLastAttribute() {
        if (isNull) {
            return false;
        }
        if (length == 0) {
            return false;
        } else {
            return attributes[length - 1];
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

    public DottedChain addWithSuffix(String name) {
        String addedNameNoSuffix = name;
        String addedNamespaceSuffix = "";
        if (name != null && name.matches(".*#[0-9]+$")) {
            addedNameNoSuffix = name.substring(0, name.lastIndexOf("#"));
            addedNamespaceSuffix = name.substring(name.lastIndexOf("#"));
        }
        return add(addedNameNoSuffix, addedNamespaceSuffix);
    }

    public DottedChain add(String name, String namespaceSuffix) {
        return add(false, name, namespaceSuffix);
    }
    public DottedChain add(boolean attribute, String name, String namespaceSuffix) {
        if (name == null) {
            return new DottedChain(this);
        }
        String[] nparts = new String[length + 1];
        boolean[] nattributes = new boolean[length + 1];
        String[] nnamespaceSuffixes = new String[length + 1];
        if (length > 0) {
            System.arraycopy(parts, 0, nparts, 0, length);
            System.arraycopy(attributes, 0, nattributes, 0, length);
            System.arraycopy(namespaceSuffixes, 0, nnamespaceSuffixes, 0, length);            
        }
       
        nattributes[nattributes.length - 1] = attribute;
        nparts[nparts.length - 1] = name;
        nnamespaceSuffixes[nparts.length - 1] = namespaceSuffix;
        return new DottedChain(nattributes, nparts, nnamespaceSuffixes);
    }

    protected String toString(boolean as3, boolean raw, boolean withSuffix) {
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
            if (attributes[i]) {
                ret.append("@");
            }
            String part = parts[i];
            boolean lastStar = i == length - 1 && "*".equals(part);
            ret.append((raw || lastStar) ? part : IdentifiersDeobfuscation.printIdentifier(as3, part));
            if (withSuffix) {
                ret.append(namespaceSuffixes[i]);
            }
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
        return toString(as3, false, true);
    }

    public String toRawString() { //Is SUFFIX correctly handled?
        return toString(false/*ignored*/, true, true);
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
            if (attributes[i] != other.attributes[i]) {
                return false;
            }
            if (!namespaceSuffixes[i].equals(other.namespaceSuffixes[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int compareTo(DottedChain o) {
        return toRawString().compareTo(o.toRawString());
    }
}
