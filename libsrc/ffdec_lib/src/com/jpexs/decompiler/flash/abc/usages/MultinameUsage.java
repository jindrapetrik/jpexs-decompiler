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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public abstract class MultinameUsage {

    protected final ABC abc;

    private final int multinameIndex;

    public MultinameUsage(ABC abc, int multinameIndex) {
        this.abc = abc;
        this.multinameIndex = multinameIndex;
    }

    public int getMultinameIndex() {
        return multinameIndex;
    }

    public ABC getAbc() {
        return abc;
    }

    protected boolean sameMultinameName(MultinameUsage other) {
        Multiname thisM = abc.constants.getMultiname(multinameIndex);
        Multiname otherM = other.abc.constants.getMultiname(other.multinameIndex);
        if (thisM == null && otherM == null) {
            return false; // honfika: why false?
        }
        if (thisM == null || otherM == null) {
            return false;
        }
        if ((thisM.kind == Multiname.QNAME || thisM.kind == Multiname.QNAMEA) && otherM.kind == thisM.kind) {
            String thisName = thisM.getName(abc.constants, new ArrayList<>(), true, true);
            String otherName = otherM.getName(other.abc.constants, new ArrayList<>(), true, true);
            Namespace thisNs = thisM.getNamespace(abc.constants);
            Namespace otherNs = otherM.getNamespace(other.abc.constants);
            if (!Objects.equals(thisName, otherName)) {
                return false;
            }

            //Both are custom namespaced
            if (thisNs.kind == Namespace.KIND_NAMESPACE && otherNs.kind == Namespace.KIND_NAMESPACE) {
                //compare those custom
                return Objects.equals(thisNs.getName(abc.constants), otherNs.getName(other.abc.constants));
            }
            //One is custom namespaced, other cannot be the same
            if (thisNs.kind == Namespace.KIND_NAMESPACE || otherNs.kind == Namespace.KIND_NAMESPACE) {
                return false;
            }

            //public or package internal are colliding when have same package ns
            if ((thisNs.kind == Namespace.KIND_PACKAGE || thisNs.kind == Namespace.KIND_PACKAGE_INTERNAL)
                    && (otherNs.kind == Namespace.KIND_PACKAGE || otherNs.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                return Objects.equals(thisNs.getName(abc.constants), otherNs.getName(other.abc.constants));
            }

            //one of them is private/protected
            return true;

        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.abc);
        hash = 97 * hash + this.multinameIndex;
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
        final MultinameUsage other = (MultinameUsage) obj;
        if (this.multinameIndex != other.multinameIndex) {
            return false;
        }
        if (!Objects.equals(this.abc, other.abc)) {
            return false;
        }
        return true;
    }

    public abstract boolean collides(MultinameUsage other);
}
