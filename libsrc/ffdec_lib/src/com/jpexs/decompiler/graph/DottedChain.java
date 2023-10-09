/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import java.util.Objects;

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

    private List<PathPart> parts;

    private boolean isNull = false;

    public String getNamespaceSuffix(int index) {
        return parts.get(index).namespaceSuffix;
    }

    public String getLastNamespaceSuffix() {
        if (parts.isEmpty()) {
            return "";
        }
        return parts.get(parts.size() - 1).namespaceSuffix;
    }

    public static final DottedChain parseNoSuffix(String name) {
        if (name == null) {
            return DottedChain.EMPTY;
        } else if (name.isEmpty()) {
            return DottedChain.TOPLEVEL;
        } else {
            String[] parts = name.split("\\.");
            List<PathPart> newParts = new ArrayList<>();
            for (String part : parts) {
                newParts.add(new PathPart(part, false, ""));
            }

            return new DottedChain(newParts, false);
        }
    }

    public static final DottedChain parseWithSuffix(String name) {
        if (name == null) {
            return DottedChain.EMPTY;
        } else if (name.isEmpty()) {
            return DottedChain.TOPLEVEL;
        } else {
            String[] parts = name.split("\\.");
            List<PathPart> newParts = new ArrayList<>();
            for (String part : parts) {
                String nameNoSuffix = part;
                String namespaceSuffix = "";
                if (part.matches(".*#[0-9]+$")) {
                    nameNoSuffix = part.substring(0, part.lastIndexOf("#"));
                    namespaceSuffix = part.substring(part.lastIndexOf("#"));
                }
                newParts.add(new PathPart(nameNoSuffix, false, namespaceSuffix));
            }

            return new DottedChain(newParts, false);
        }
    }

    private DottedChain(boolean isNull) {
        this.isNull = isNull;
        this.parts = new ArrayList<>();
    }

    public DottedChain(DottedChain src) {
        this.parts = new ArrayList<>(src.parts);
        this.isNull = src.isNull;
    }

    public DottedChain(String[] parts) {
        this(Arrays.asList(parts));
    }

    private DottedChain(List<PathPart> parts, boolean isNull) {
        this.parts = parts;
        this.isNull = isNull;
    }

    public DottedChain(List<String> parts) {
        List<PathPart> newParts = new ArrayList<>();
        for (String part : parts) {
            newParts.add(new PathPart(part, false, ""));
        }
        this.parts = newParts;
    }

    public DottedChain(String[] parts, String[] namespaceSuffixes) {
        this(new boolean[parts.length], parts, namespaceSuffixes);
    }

    public DottedChain(boolean[] attributes, String[] parts, String[] namespaceSuffixes) {
        List<PathPart> newParts = new ArrayList<>();
        for (int i = 0; i < attributes.length; i++) {
            newParts.add(new PathPart(parts[i], attributes[i], namespaceSuffixes[i]));
        }
        this.parts = newParts;

    }

    public boolean isTopLevel() {
        return !isNull && parts.isEmpty();
    }

    public boolean isEmpty() {
        return isNull;
    }

    public int size() {
        return parts.size();
    }

    public String get(int index) {
        if (index >= parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return parts.get(index).name;
    }

    public boolean isAttribute(int index) {
        if (index >= parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return parts.get(index).attribute;
    }

    public DottedChain subChain(int count) {
        if (count > parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return new DottedChain(new ArrayList<>(parts.subList(0, count)), isNull);
    }

    public String getLast() {
        if (isNull) {
            return null;
        }
        if (parts.isEmpty()) {
            return "";
        } else {
            return parts.get(parts.size() - 1).name;
        }
    }

    public boolean isLastAttribute() {
        if (isNull) {
            return false;
        }
        if (parts.isEmpty()) {
            return false;
        } else {
            return parts.get(parts.size() - 1).attribute;
        }
    }

    public DottedChain getWithoutLast() {
        if (isNull) {
            return null;
        }
        if (parts.size() < 2) {
            return EMPTY;
        }

        return subChain(parts.size() - 1);
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
        List<PathPart> newParts = new ArrayList<>(parts);
        newParts.add(new PathPart(name, attribute, namespaceSuffix));
        return new DottedChain(newParts, false);
    }

    public DottedChain preAdd(String name, String namespaceSuffix) {
        return preAdd(false, name, namespaceSuffix);
    }

    public DottedChain preAdd(boolean attribute, String name, String namespaceSuffix) {
        if (name == null) {
            return new DottedChain(this);
        }
        List<PathPart> newParts = new ArrayList<>(parts);
        newParts.add(0, new PathPart(name, attribute, namespaceSuffix));
        return new DottedChain(newParts, false);
    }

    @Override
    public String toString() {
        return toRawString();
    }
    
    protected String toString(boolean as3, boolean raw, boolean withSuffix) {
        if (isNull) {
            return "";
        }
        if (parts.isEmpty()) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                ret.append(".");
            }
            if (parts.get(i).attribute) {
                ret.append("@");
            }
            String part = parts.get(i).name;
            boolean lastStar = i == parts.size() - 1 && "*".equals(part);
            ret.append((raw || lastStar) ? part : IdentifiersDeobfuscation.printIdentifier(as3, part));
            if (withSuffix) {
                ret.append(parts.get(i).namespaceSuffix);
            }
        }

        return ret.toString();
    }

    public String toFilePath() {
        if (isNull) {
            return "";
        }
        if (parts.isEmpty()) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                ret.append(File.separator);
            }

            ret.append(Helper.makeFileName(IdentifiersDeobfuscation.printIdentifier(true, parts.get(i).name)));
        }
        return ret.toString();
    }

    public List<String> toList() {
        List<String> ret = new ArrayList<>();
        for (PathPart p : parts) {
            ret.add(p.name);
        }
        return ret;
    }

    public String toPrintableString(boolean as3) {
        return toString(as3, false, true);
    }

    public String toRawString() { //Is SUFFIX correctly handled?
        return toString(false/*ignored*/, true, true);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.parts);
        hash = 41 * hash + (this.isNull ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DottedChain other = (DottedChain) obj;
        if (this.isNull != other.isNull) {
            return false;
        }
        return Objects.equals(this.parts, other.parts);
    }

    @Override
    public int compareTo(DottedChain o) {
        return toRawString().compareTo(o.toRawString());
    }

    private static class PathPart implements Serializable {

        public String name;
        public boolean attribute;
        public String namespaceSuffix;

        public PathPart(String name, boolean attribute, String namespaceSuffix) {
            this.name = name;
            this.attribute = attribute;
            this.namespaceSuffix = namespaceSuffix;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.name);
            hash = 79 * hash + (this.attribute ? 1 : 0);
            hash = 79 * hash + Objects.hashCode(this.namespaceSuffix);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathPart other = (PathPart) obj;
            if (this.attribute != other.attribute) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return Objects.equals(this.namespaceSuffix, other.namespaceSuffix);
        }
    }
}
