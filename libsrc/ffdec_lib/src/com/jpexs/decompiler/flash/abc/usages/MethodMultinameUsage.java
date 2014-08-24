/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class MethodMultinameUsage extends TraitMultinameUsage {

    public boolean isInitializer;

    public MethodMultinameUsage(List<ABCContainerTag> abcTags, ABC abc, int multinameIndex, int classIndex, int traitIndex, boolean isStatic, boolean isInitializer, Traits traits, int parentTraitIndex) {
        super(abcTags, abc, multinameIndex, classIndex, traitIndex, isStatic, traits, parentTraitIndex);
        this.isInitializer = isInitializer;
    }

    public boolean isInitializer() {
        return isInitializer;
    }

    @Override
    public String toString() {
        NulWriter nulWriter = new NulWriter();
        if (!isInitializer) {
            if (parentTraitIndex > -1) {
                if (isStatic) {
                    ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).convertHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
                } else {
                    ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).convertHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
                }
            }
            ((TraitMethodGetterSetter) traits.traits.get(traitIndex)).convertHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
        }

        HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), false);
        writer.appendNoHilight(super.toString());
        writer.appendNoHilight(" ");
        if (isInitializer) {
            if (isStatic) {
                writer.appendNoHilight("class initializer");
            } else {
                writer.appendNoHilight("instance initializer");
            }
        } else {
            if (parentTraitIndex > -1) {
                if (isStatic) {
                    ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).toStringHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
                } else {
                    ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).toStringHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
                }
                writer.appendNoHilight(" ");
            }
            ((TraitMethodGetterSetter) traits.traits.get(traitIndex)).toStringHeader(null, "", abcTags, abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
        }
        return writer.toString();
    }

    public int getTraitIndex() {
        return traitIndex;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
