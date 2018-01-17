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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public abstract class TraitMultinameUsage extends MultinameUsage implements InsideClassMultinameUsageInterface {

    protected final int traitIndex;

    public static final int TRAITS_TYPE_CLASS = 1;

    public static final int TRAITS_TYPE_INSTANCE = 2;

    public static final int TRAITS_TYPE_SCRIPT = 3;

    protected final int traitsType;

    protected final int classIndex;

    protected final int scriptIndex;

    protected final Traits traits;

    protected final int parentTraitIndex;

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
        return "class " + abc.constants.getMultiname(abc.instance_info.get(classIndex).name_index).getNameWithNamespace(abc.constants, true).toPrintableString(true);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 23 * hash + this.traitIndex;
        hash = 23 * hash + this.traitsType;
        hash = 23 * hash + this.classIndex;
        hash = 23 * hash + this.scriptIndex;
        hash = 23 * hash + Objects.hashCode(this.traits);
        hash = 23 * hash + this.parentTraitIndex;
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
        final TraitMultinameUsage other = (TraitMultinameUsage) obj;
        if (this.traitIndex != other.traitIndex) {
            return false;
        }
        if (this.traitsType != other.traitsType) {
            return false;
        }
        if (this.classIndex != other.classIndex) {
            return false;
        }
        if (this.scriptIndex != other.scriptIndex) {
            return false;
        }
        if (this.parentTraitIndex != other.parentTraitIndex) {
            return false;
        }
        if (!Objects.equals(this.traits, other.traits)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean collides(MultinameUsage other) {
        return false;
    }

    @Override
    public int getClassIndex() {
        return classIndex;
    }

    public int getTraitIndex() {
        return traitIndex;
    }

    public int getTraitsType() {
        return traitsType;
    }

    public int getScriptIndex() {
        return scriptIndex;
    }

    public Traits getTraits() {
        return traits;
    }

    public int getParentTraitIndex() {
        return parentTraitIndex;
    }
}
