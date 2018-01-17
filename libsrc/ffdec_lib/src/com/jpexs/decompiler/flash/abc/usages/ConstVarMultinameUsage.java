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
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import java.util.ArrayList;

/**
 *
 * @author JPEXS
 */
public abstract class ConstVarMultinameUsage extends TraitMultinameUsage {

    public ConstVarMultinameUsage(ABC abc, int multinameIndex, int scriptIndex, int classIndex, int traitIndex, int traitsType, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, traits, parentTraitIndex);
    }

    @Override
    public String toString() {
        NulWriter nulWriter = new NulWriter();
        ConvertData convertData = new ConvertData();
        if (parentTraitIndex > -1) {
            if (traitsType == TRAITS_TYPE_CLASS) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).convertHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
            } else if (traitsType == TRAITS_TYPE_INSTANCE) {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).convertHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).convertHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS /*?? FIXME*/, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
        } catch (InterruptedException ex) {
            // ignore
        }

        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        writer.appendNoHilight(super.toString() + " ");
        if (parentTraitIndex > -1) {
            if (traitsType == TRAITS_TYPE_CLASS) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).toStringHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false);
            } else if (traitsType == TRAITS_TYPE_INSTANCE) {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).toStringHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).toStringHeader(null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false);
        } catch (InterruptedException ex) {
            // ignore
        }
        return writer.toString().trim();
    }

    public boolean isStatic() {
        return traitsType == TRAITS_TYPE_CLASS;
    }
}
