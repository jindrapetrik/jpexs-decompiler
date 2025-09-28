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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;

/**
 * Const or var name multiname usage.
 *
 * @author JPEXS
 */
public class ConstVarNameMultinameUsage extends ConstVarMultinameUsage implements DefinitionUsage {

    /**
     * Constructor.
     * @param abc ABC
     * @param multinameIndex Multiname index
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param traitIndex Trait index
     * @param traitsType Traits type
     * @param traits Traits
     * @param parentTraitIndex Parent trait index
     */
    public ConstVarNameMultinameUsage(ABC abc, int multinameIndex, int scriptIndex, int classIndex, int traitIndex, int traitsType, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, traits, parentTraitIndex);
    }

    @Override
    public String toString() {
        return super.toString() + " name";
    }

    @Override
    public boolean collides(MultinameUsage other) {
        if ((other instanceof ConstVarNameMultinameUsage) || (other instanceof MethodNameMultinameUsage)) {
            TraitMultinameUsage otherTrait = (TraitMultinameUsage) other;
            if (otherTrait.classIndex == classIndex && otherTrait.traitsType == traitsType && otherTrait.parentTraitIndex == parentTraitIndex) {
                if (other.sameMultinameName(this, false)) {
                    return true;
                }
            }
        }
        return false;
    }

}
