/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.graph.DottedChain;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class ClassPath {

    public final DottedChain packageStr;

    public final String className;

    public final String namespaceSuffix;

    public ClassPath(DottedChain packageStr, String className, String namespaceSuffix) {
        this.packageStr = packageStr == null ? DottedChain.TOPLEVEL : packageStr;
        this.className = className;
        this.namespaceSuffix = namespaceSuffix;
    }

    @Override
    public String toString() {
        return packageStr.add(className, namespaceSuffix).toPrintableString(true);
    }

    public String toRawString() {
        return packageStr.add(className, namespaceSuffix).toRawString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.packageStr);
        hash = 31 * hash + Objects.hashCode(this.className);
        hash = 31 * hash + Objects.hashCode(this.namespaceSuffix);
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
        final ClassPath other = (ClassPath) obj;
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.namespaceSuffix, other.namespaceSuffix)) {
            return false;
        }
        if (!Objects.equals(this.packageStr, other.packageStr)) {
            return false;
        }
        return true;
    }

}
