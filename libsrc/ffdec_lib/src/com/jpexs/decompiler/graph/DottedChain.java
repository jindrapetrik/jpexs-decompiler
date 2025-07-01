/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * Dotted chain class. Represents a chain of names separated by dots.
 *
 * @author JPEXS
 */
public class DottedChain implements Serializable, Comparable<DottedChain> {

    //Basic dotted chains
    public static final DottedChain UNBOUNDED = new DottedChain(new String[]{"*"});

    public static final DottedChain TOPLEVEL = new DottedChain(new String[]{});

    public static final DottedChain EMPTY = TOPLEVEL;

    public static final DottedChain BOOLEAN = new DottedChain(new String[]{"Boolean"});

    public static final DottedChain STRING = new DottedChain(new String[]{"String"});

    public static final DottedChain ARRAY = new DottedChain(new String[]{"Array"});

    public static final DottedChain NUMBER = new DottedChain(new String[]{"Number"});

    public static final DottedChain DECIMAL = new DottedChain(new String[]{"decimal"});

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

    /**
     * Parts of the chain.
     */
    private List<PathPart> parts;

    /**
     * Get the namespace suffix of the part at the given index.
     *
     * @param index Index
     * @return Namespace suffix
     */
    public String getNamespaceSuffix(int index) {
        return parts.get(index).namespaceSuffix;
    }

    /**
     * Gets the last namespace suffix.
     *
     * @return Last namespace suffix
     */
    public String getLastNamespaceSuffix() {
        if (parts.isEmpty()) {
            return "";
        }
        return parts.get(parts.size() - 1).namespaceSuffix;
    }

    /**
     * Parses a dotted chain from a string without suffix.
     *
     * @param name Name
     * @return Dotted chain
     */
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

            DottedChain ret = new DottedChain();
            ret.parts = newParts;
            return ret;
        }
    }

    /**
     * Parses a dotted chain from a string with suffix.
     *
     * @param name Name
     * @return Dotted chain
     */
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

            DottedChain ret = new DottedChain();
            ret.parts = newParts;
            return ret;
        }
    }
    
    /**
     * Parses a dotted chain from a deobfuscated string.
     *
     * @param name Name
     * @return Dotted chain
     */
    public static final DottedChain parsePrintable(String name) {
        if (name == null) {
            return DottedChain.EMPTY;
        } else if (name.isEmpty()) {
            return DottedChain.TOPLEVEL;
        } else {
            String[] parts = name.split("\\.");
            List<PathPart> newParts = new ArrayList<>();
            for (String part : parts) {
                newParts.add(new PathPart(IdentifiersDeobfuscation.unescapeOIdentifier(part), false, ""));
            }

            DottedChain ret = new DottedChain();
            ret.parts = newParts;
            return ret;
        }
    }
       
    /**
     * Constructs a new dotted chain.
     *
     */
    private DottedChain() {
        this.parts = new ArrayList<>();
    }

    /**
     * Constructs a new dotted chain.
     *
     * @param src Source chain
     */
    public DottedChain(DottedChain src) {
        this.parts = new ArrayList<>(src.parts);
    }

    /**
     * Constructs a new dotted chain.
     *
     * @param parts Parts
     */
    public DottedChain(String[] parts) {
        this(Arrays.asList(parts));
    }

    /**
     * Constructs a new dotted chain.
     *
     * @param parts Parts
     */
    public DottedChain(List<String> parts) {
        List<PathPart> newParts = new ArrayList<>();
        for (String part : parts) {
            newParts.add(new PathPart(part, false, ""));
        }
        this.parts = newParts;
    }

    /**
     * Constructs a new dotted chain.
     *
     * @param parts Parts
     * @param namespaceSuffixes Namespace suffixes
     */
    public DottedChain(String[] parts, String[] namespaceSuffixes) {
        this(new boolean[parts.length], parts, namespaceSuffixes);
    }

    /**
     * Constructs a new dotted chain.
     *
     * @param attributes Attributes
     * @param parts Parts
     * @param namespaceSuffixes Namespace suffixes
     */
    public DottedChain(boolean[] attributes, String[] parts, String[] namespaceSuffixes) {
        List<PathPart> newParts = new ArrayList<>();
        for (int i = 0; i < attributes.length; i++) {
            newParts.add(new PathPart(parts[i], attributes[i], namespaceSuffixes[i]));
        }
        this.parts = newParts;

    }

    /**
     * Checks whether this chain is top-level.
     *
     * @return Whether this chain is top-level
     */
    public boolean isTopLevel() {
        return parts.isEmpty();
    }

    /**
     * Checks whether this chain is empty.
     *
     * @return Whether this chain is empty
     */
    public boolean isEmpty() {
        return parts.isEmpty();
    }

    /**
     * Gets the number of parts in this chain.
     *
     * @return Number of parts
     */
    public int size() {
        return parts.size();
    }

    /**
     * Gets the part at the given index.
     *
     * @param index Index
     * @return Part
     */
    public String get(int index) {
        if (index >= parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return parts.get(index).name;
    }
    
    /**
     * Gets string parts as list.
     * @return String parts
     */
    public List<String> getStringParts() {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            ret.add(parts.get(i).name);
        }
        return ret;
    }

    /**
     * Checks whether the part at the given index is an attribute.
     *
     * @param index Index
     * @return Whether the part at the given index is an attribute
     */
    public boolean isAttribute(int index) {
        if (index >= parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return parts.get(index).attribute;
    }

    /**
     * Gets the sub-chain of specific length.
     *
     * @param count Length
     * @return Sub-chain
     */
    public DottedChain subChain(int count) {
        if (count > parts.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        DottedChain ret = new DottedChain();
        ret.parts = new ArrayList<>(parts.subList(0, count));
        return ret;
    }

    /**
     * Gets last part.
     *
     * @return Last part
     */
    public String getLast() {
        if (parts.isEmpty()) {
            return "";
        } else {
            return parts.get(parts.size() - 1).name;
        }
    }

    /**
     * Checks whether the last part is an attribute.
     *
     * @return Whether the last part is an attribute
     */
    public boolean isLastAttribute() {
        if (parts.isEmpty()) {
            return false;
        } else {
            return parts.get(parts.size() - 1).attribute;
        }
    }

    /**
     * Gets the chain without the last part.
     *
     * @return Chain without the last part
     */
    public DottedChain getWithoutLast() {
        if (parts.size() < 2) {
            return TOPLEVEL;
        }

        return subChain(parts.size() - 1);
    }

    /**
     * Adds a part to the chain with a suffix.
     *
     * @param name Name
     * @return New chain
     */
    public DottedChain addWithSuffix(String name) {
        String addedNameNoSuffix = name;
        String addedNamespaceSuffix = "";
        if (name != null && name.matches(".*#[0-9]+$")) {
            addedNameNoSuffix = name.substring(0, name.lastIndexOf("#"));
            addedNamespaceSuffix = name.substring(name.lastIndexOf("#"));
        }
        return add(addedNameNoSuffix, addedNamespaceSuffix);
    }

    /**
     * Adds a part to the chain.
     *
     * @param name Name
     * @param namespaceSuffix Namespace suffix
     * @return New chain
     */
    public DottedChain add(String name, String namespaceSuffix) {
        return add(false, name, namespaceSuffix);
    }

    /**
     * Adds a part to the chain.
     *
     * @param attribute Whether the part is an attribute
     * @param name Name
     * @param namespaceSuffix Namespace suffix
     * @return New chain
     */
    public DottedChain add(boolean attribute, String name, String namespaceSuffix) {
        if (name == null) {
            return new DottedChain(this);
        }
        List<PathPart> newParts = new ArrayList<>(parts);
        newParts.add(new PathPart(name, attribute, namespaceSuffix));
        DottedChain ret = new DottedChain();
        ret.parts = newParts;
        return ret;
    }

    /**
     * Adds prefix to the chain.
     *
     * @param name Name
     * @param namespaceSuffix Namespace suffix
     * @return New chain
     */
    public DottedChain preAdd(String name, String namespaceSuffix) {
        return preAdd(false, name, namespaceSuffix);
    }

    /**
     * Adds prefix to the chain.
     *
     * @param attribute Whether the part is an attribute
     * @param name Name
     * @param namespaceSuffix Namespace suffix
     * @return New chain
     */
    public DottedChain preAdd(boolean attribute, String name, String namespaceSuffix) {
        if (name == null) {
            return new DottedChain(this);
        }
        List<PathPart> newParts = new ArrayList<>(parts);
        newParts.add(0, new PathPart(name, attribute, namespaceSuffix));
        DottedChain ret = new DottedChain();
        ret.parts = newParts;
        return ret;
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return toRawString();
    }

    /**
     * To string.
     *
     * @param as3 Whether to print as AS3
     * @param raw Whether to print raw (without deobfuscation)
     * @param withSuffix Whether to print with suffix
     * @return String
     */
    protected String toString(boolean as3, boolean raw, boolean withSuffix) {
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

    /**
     * To file path.
     *
     * @return File path
     */
    public String toFilePath() {
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

    /**
     * To list.
     *
     * @return List
     */
    public List<String> toList() {
        List<String> ret = new ArrayList<>();
        for (PathPart p : parts) {
            ret.add(p.name);
        }
        return ret;
    }

    /**
     * To printable string.
     *
     * @param as3 Whether to print as AS3
     * @return Printable string
     */
    public String toPrintableString(boolean as3) {
        return toString(as3, false, true);
    }

    /**
     * To raw string. (without deobfuscation)
     *
     * @return Raw string
     */
    public String toRawString() { //Is SUFFIX correctly handled?
        return toString(false/*ignored*/, true, true);
    }

    /**
     * Hash code.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.parts);
        return hash;
    }

    /**
     * Equals.
     *
     * @param obj Object
     * @return Whether this object is equal to the given object
     */
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
        return Objects.equals(this.parts, other.parts);
    }

    /**
     * Compare to.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(DottedChain o) {
        return toRawString().compareTo(o.toRawString());
    }

    /**
     * Path part class.
     */
    private static class PathPart implements Serializable {

        /**
         * Name.
         */
        public String name;

        /**
         * Is this part an attribute?
         */
        public boolean attribute;

        /**
         * Namespace suffix.
         */
        public String namespaceSuffix;

        /**
         * Constructs a new path part.
         *
         * @param name Name
         * @param attribute Whether this part is an attribute
         * @param namespaceSuffix Namespace suffix
         */
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
