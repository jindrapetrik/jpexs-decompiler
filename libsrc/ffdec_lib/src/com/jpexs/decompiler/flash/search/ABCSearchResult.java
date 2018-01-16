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
package com.jpexs.decompiler.flash.search;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;

/**
 *
 * @author JPEXS
 */
public class ABCSearchResult {

    public static String STR_INSTANCE_INITIALIZER = AppResources.translate("trait.instanceinitializer");

    public static String STR_CLASS_INITIALIZER = AppResources.translate("trait.classinitializer");

    public static String STR_SCRIPT_INITIALIZER = AppResources.translate("trait.scriptinitializer");

    private final ScriptPack scriptPack;

    private final boolean pcode;

    private final int classIndex;

    private final int traitId;

    public ABCSearchResult(ScriptPack scriptPack) {
        this.scriptPack = scriptPack;
        pcode = false;
        classIndex = 0;
        traitId = GraphTextWriter.TRAIT_UNKNOWN;
    }

    public ABCSearchResult(ScriptPack scriptPack, int classIndex, int traitId) {
        this.scriptPack = scriptPack;
        pcode = true;
        this.classIndex = classIndex;
        this.traitId = traitId;
    }

    public ScriptPack getScriptPack() {
        return scriptPack;
    }

    public boolean isPcode() {
        return pcode;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getTraitId() {
        return traitId;
    }

    private String getTraitName() {
        if (traitId == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            return STR_SCRIPT_INITIALIZER;
        }

        if (classIndex == -1) {
            return null;
        }

        if (traitId == GraphTextWriter.TRAIT_CLASS_INITIALIZER) {
            return STR_CLASS_INITIALIZER;
        }

        if (traitId == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER) {
            return STR_INSTANCE_INITIALIZER;
        }

        ABC abc = scriptPack.abc;

        int staticTraitCount = abc.class_info.get(classIndex).static_traits.traits.size();
        boolean isStatic = traitId < staticTraitCount;

        if (isStatic) {
            return abc.class_info.get(classIndex).static_traits.traits.get(traitId).getName(abc).getName(abc.constants, null, false, true);
        } else {
            int index = traitId - staticTraitCount;
            return abc.instance_info.get(classIndex).instance_traits.traits.get(index).getName(abc).getName(abc.constants, null, false, true);
        }
    }

    @Override
    public String toString() {
        String result = scriptPack.getClassPath().toString();

        if (pcode) {
            result += "/";
            result += getTraitName();
        }

        return result;
    }
}
