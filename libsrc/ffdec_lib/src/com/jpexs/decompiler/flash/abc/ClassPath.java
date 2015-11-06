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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class ClassPath {

    public final DottedChain packageStr;

    public final String className;

    public ClassPath(DottedChain packageStr, String className) {
        this.packageStr = packageStr == null ? DottedChain.EMPTY : packageStr;
        this.className = className;
    }

    @Override
    public String toString() {
        return packageStr.isEmpty() || packageStr.isTopLevel() ? IdentifiersDeobfuscation.printIdentifier(true, className)
                : packageStr.toPrintableString(true) + "." + IdentifiersDeobfuscation.printIdentifier(true, className);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(packageStr);
        hash = 37 * hash + Objects.hashCode(className);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassPath other = (ClassPath) obj;
        if (!Objects.equals(packageStr, other.packageStr)) {
            return false;
        }
        return Objects.equals(className, other.className);
    }
}
