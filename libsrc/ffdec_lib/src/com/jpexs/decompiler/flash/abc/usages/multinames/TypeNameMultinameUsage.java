/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.usages.multinames;

import com.jpexs.decompiler.flash.abc.ABC;
import java.util.ArrayList;

/**
 * TypeName multiname usage.
 *
 * @author JPEXS
 */
public class TypeNameMultinameUsage extends MultinameUsage {

    /**
     * Type name index
     */
    protected int typename_index;

    /**
     * Constructor.
     * @param abc ABC
     * @param multinameIndex Multiname index
     * @param typename_index Type name index
     * @param scriptIndex Script index
     */
    public TypeNameMultinameUsage(ABC abc, int multinameIndex, int typename_index, int scriptIndex) {
        super(abc, multinameIndex, scriptIndex);
        this.typename_index = typename_index;
    }

    @Override
    public String toString() {
        return "TypeName " + abc.constants.getMultiname(typename_index).toString(abc.constants, new ArrayList<>());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + this.typename_index;
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
        if (!super.equals(obj)) {
            return false;
        }
        final TypeNameMultinameUsage other = (TypeNameMultinameUsage) obj;
        if (this.typename_index != other.typename_index) {
            return false;
        }
        return true;
    }

    @Override
    public boolean collides(MultinameUsage other) {
        return false;
    }

    /**
     * Get type name index.
     * @return Type name index
     */
    public int getTypenameIndex() {
        return typename_index;
    }
}
