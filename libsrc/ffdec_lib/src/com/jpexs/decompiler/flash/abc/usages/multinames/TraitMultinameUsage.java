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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.Objects;

/**
 * Trait multiname usage.
 *
 * @author JPEXS
 */
public abstract class TraitMultinameUsage extends MultinameUsage implements InsideClassMultinameUsageInterface {

    /**
     * Trait index
     */
    protected final int traitIndex;

    /**
     * Traits type - class
     */
    public static final int TRAITS_TYPE_CLASS = 1;

    /**
     * Traits type - instance
     */
    public static final int TRAITS_TYPE_INSTANCE = 2;

    /**
     * Traits type - script
     */
    public static final int TRAITS_TYPE_SCRIPT = 3;

    /**
     * Traits type
     */
    protected final int traitsType;

    /**
     * Class index
     */
    protected final int classIndex;

    /**
     * Script index
     */
    protected final int scriptIndex;

    /**
     * Traits
     */
    protected final Traits traits;

    /**
     * Parent trait index
     */
    protected final int parentTraitIndex;

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
    public TraitMultinameUsage(ABC abc, int multinameIndex, int scriptIndex, int classIndex, int traitIndex, int traitsType, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex, scriptIndex);
        this.scriptIndex = scriptIndex;
        this.classIndex = classIndex;
        this.traitIndex = traitIndex;
        this.traitsType = traitsType;
        this.traits = traits;
        this.parentTraitIndex = parentTraitIndex;
    }

    @Override
    public String toString() {
        if (classIndex != -1) {
            InstanceInfo ii = abc.instance_info.get(classIndex);
            String kind = ii.isInterface() ? "interface" : "class";
            return kind + " " + ii.getName(abc.constants).getNameWithNamespace(abc.constants, true).toPrintableString(true);
        }
        DottedChain scriptSimpleName = abc.script_info.get(scriptIndex).getSimplePackName(abc);
        return "script " + (scriptSimpleName == null ? "" + scriptIndex : scriptSimpleName.toPrintableString(true));
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

    /**
     * Gets trait index.
     * @return Trait index
     */
    public int getTraitIndex() {
        return traitIndex;
    }

    /**
     * Gets traits type.
     * @return Traits type
     */
    public int getTraitsType() {
        return traitsType;
    }

    /**
     * Gets class index.
     * @return Class index
     */
    public int getScriptIndex() {
        return scriptIndex;
    }

    /**
     * Gets traits.
     * @return Traits
     */
    public Traits getTraits() {
        return traits;
    }

    /**
     * Gets parent trait index.
     * @return Parent trait index
     */
    public int getParentTraitIndex() {
        return parentTraitIndex;
    }
}
