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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class DottedChain {

    public final List<String> parts;

    public DottedChain(List<String> parts) {
        this.parts = new ArrayList<>(parts);
    }

    public DottedChain(String... parts) {
        this.parts = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            this.parts.add(parts[i]);
        }
    }

    public String getLast() {
        if (parts.isEmpty()) {
            return "";
        } else {
            return parts.get(parts.size() - 1);
        }
    }

    public DottedChain getWithoutLast() {
        List<String> nparts = new ArrayList<>(parts);
        if (!nparts.isEmpty()) {
            nparts.remove(nparts.size() - 1);
        }
        return new DottedChain(nparts);
    }

    public String toPrintableString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                ret.append(".");
            }
            ret.append(IdentifiersDeobfuscation.printIdentifier(true, parts.get(0)));
        }
        return ret.toString();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                ret.append(".");
            }
            ret.append(parts.get(i));
        }
        return ret.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(parts);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            obj = new DottedChain(((String) obj).split("\\."));
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DottedChain other = (DottedChain) obj;
        if (!Objects.equals(parts, other.parts)) {
            return false;
        }
        return true;
    }
}
