/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;

/**
 *
 * @author JPEXS
 */
public class ImplementsMultinameUsage extends MultinameUsage implements InsideClassMultinameUsageInterface {

    private final int classIndex;

    public ImplementsMultinameUsage(ABC abc, int multinameIndex, int classIndex, int scriptIndex) {
        super(abc, multinameIndex, scriptIndex);
        this.classIndex = classIndex;
    }

    @Override
    public int getClassIndex() {
        return classIndex;
    }

    @Override
    public String toString() {
        return super.toString() + " implements";
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 59 * hash + this.classIndex;
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
        final ImplementsMultinameUsage other = (ImplementsMultinameUsage) obj;
        if (this.classIndex != other.classIndex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean collides(MultinameUsage other) {
        return false;
    }
}
