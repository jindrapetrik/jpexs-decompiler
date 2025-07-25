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
package com.jpexs.decompiler.flash.abc.usages.multinames;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import java.util.LinkedHashSet;

/**
 * Class name multiname usage.
 *
 * @author JPEXS
 */
public class ClassNameMultinameUsage extends MultinameUsage implements DefinitionUsage, InsideClassMultinameUsageInterface {

    private final int classIndex;

    /**
     * Constructor.
     * @param abc ABC
     * @param multinameIndex Multiname index
     * @param classIndex Class index
     * @param scriptIndex Script index
     */
    public ClassNameMultinameUsage(ABC abc, int multinameIndex, int classIndex, int scriptIndex) {
        super(abc, multinameIndex, scriptIndex);
        this.classIndex = classIndex;
    }

    @Override
    public int getClassIndex() {
        return classIndex;
    }

    @Override
    public String toString() {
        InstanceInfo ii = abc.instance_info.get(classIndex);
        String kind = ii.isInterface() ? "interface" : "class";
        return kind + " " + ii.getName(abc.constants).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true) + " name";
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + this.classIndex;
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
        final ClassNameMultinameUsage other = (ClassNameMultinameUsage) obj;
        if (this.classIndex != other.classIndex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean collides(MultinameUsage other) {
        if (other instanceof InsideClassMultinameUsageInterface) {
            if (((InsideClassMultinameUsageInterface) other).getClassIndex() == getClassIndex()) {
                return false;
            }
        }
        if (other instanceof ClassNameMultinameUsage) {
            return sameMultinameName(other);
        }
        return false;
    }

    @Override
    public int getScriptIndex() {
        return scriptIndex;
    }
}
