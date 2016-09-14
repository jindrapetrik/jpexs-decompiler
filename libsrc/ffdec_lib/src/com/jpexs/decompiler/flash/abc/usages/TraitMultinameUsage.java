/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;

/**
 *
 * @author JPEXS
 */
public abstract class TraitMultinameUsage extends MultinameUsage {

    public int traitIndex;

    public static final int TRAITS_TYPE_CLASS = 1;
    public static final int TRAITS_TYPE_INSTANCE = 2;
    public static final int TRAITS_TYPE_SCRIPT = 3;

    public int traitsType;
    public int classIndex;
    public int scriptIndex;

    public Traits traits;

    public int parentTraitIndex;

    public TraitMultinameUsage(ABC abc, int multinameIndex, int scriptIndex, int classIndex, int traitIndex, int traitsType, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex);
        this.scriptIndex = scriptIndex;
        this.classIndex = classIndex;
        this.traitIndex = traitIndex;
        this.traitsType = traitsType;
        this.traits = traits;
        this.parentTraitIndex = parentTraitIndex;
    }

    @Override
    public String toString() {
        return "class " + abc.constants.getMultiname(abc.instance_info.get(classIndex).name_index).getNameWithNamespace(abc.constants).toPrintableString(true);
    }
}
