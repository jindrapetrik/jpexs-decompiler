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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;

/**
 *
 * @author JPEXS
 */
public abstract class InsideClassMultinameUsage extends MultinameUsage {

    public int multinameIndex;

    public int classIndex;

    public InsideClassMultinameUsage(ABC abc, int multinameIndex, int classIndex) {
        super(abc);
        this.multinameIndex = multinameIndex;
        this.classIndex = classIndex;
    }

    @Override
    public String toString() {
        return "class " + abc.constants.getMultiname(abc.instance_info.get(classIndex).name_index).getNameWithNamespace(abc.constants, false);
    }

    public int getMultinameIndex() {
        return multinameIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }
}
