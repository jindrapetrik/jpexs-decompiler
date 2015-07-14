/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

    public static final DottedChain EMPTY = new DottedChain();

    private final String[] parts;

    private final int hash;

    public DottedChain(List<String> parts) {
        this.parts = parts.toArray(new String[parts.size()]);
        hash = calcHash();
    }

    public DottedChain(String... parts) {
        this.parts = parts;
        hash = calcHash();
    }

    public boolean isEmpty() {
        return parts.length == 0;
    }

    public int size() {
        return parts.length;
    }

    public String get(int index) {
        return parts[index];
    }

    public DottedChain subChain(int count) {
        String[] nparts = Arrays.copyOfRange(parts, 0, count);
        return new DottedChain(nparts);
    }

    public String getLast() {
        if (parts.length == 0) {
            return "";
        } else {
            return parts[parts.length - 1];
        }
    }

    public DottedChain getWithoutLast() {
        if (parts.length < 2) {
            return EMPTY;
        }

        String[] nparts = Arrays.copyOfRange(parts, 0, parts.length - 1);
        return new DottedChain(nparts);
    }

    public DottedChain add(String name) {
        String[] nparts = new String[parts.length + 1];
        if (parts.length > 0) {
            System.arraycopy(parts, 0, nparts, 0, parts.length);
        }

        nparts[nparts.length - 1] = name;
        return new DottedChain(nparts);
    }

    private String toString(boolean as3, boolean raw) {
        if (parts.length == 0 || (parts.length == 1 && parts[0].isEmpty())) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                ret.append(".");
            }

            String part = parts[i];
            boolean lastStar = i == parts.length - 1 && "*".equals(part);
            ret.append((raw || lastStar) ? part : IdentifiersDeobfuscation.printIdentifier(as3, part));
        }
        return ret.toString();
    }

    public String toFilePath() {
        if (parts.length == 0 || (parts.length == 1 && parts[0].isEmpty())) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
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
        if (parts.length > 0 && parts[0].equals("§§")) {
            int a = 1;
        }
        return Arrays.hashCode(parts);
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
        if (!Arrays.equals(parts, other.parts)) {
            return false;
        }
        return true;
    }
}
