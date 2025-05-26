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
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.ArrayList;

/**
 * Const or var multiname usage.
 *
 * @author JPEXS
 */
public abstract class ConstVarMultinameUsage extends TraitMultinameUsage {

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
    public ConstVarMultinameUsage(ABC abc, int multinameIndex, int scriptIndex, int classIndex, int traitIndex, int traitsType, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex, scriptIndex, classIndex, traitIndex, traitsType, traits, parentTraitIndex);
    }

    @Override
    public String toString() {
        NulWriter nulWriter = new NulWriter();
        ConvertData convertData = new ConvertData();
        if (parentTraitIndex > -1) {
            if (traitsType == TRAITS_TYPE_CLASS) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).convertHeader(-1, null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
            } else if (traitsType == TRAITS_TYPE_INSTANCE) {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).convertHeader(-1, null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).convertHeader(-1, null, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS /*?? FIXME*/, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<>(), false);
        } catch (InterruptedException ex) {
            // ignore
        }

        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        writer.appendNoHilight(super.toString() + " ");
        boolean insideInterface = false;
        if (classIndex > -1) {
            insideInterface = abc.instance_info.get(classIndex).isInterface();
        }

        if (parentTraitIndex > -1) {
            if (traitsType == TRAITS_TYPE_CLASS) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).toStringHeader(-1, null, DottedChain.EMPTY /*??*/, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false, insideInterface);
            } else if (traitsType == TRAITS_TYPE_INSTANCE) {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).toStringHeader(-1, null, DottedChain.EMPTY /*??*/, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false, insideInterface);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).toStringHeader(-1, null, DottedChain.EMPTY /*??*/, convertData, "", abc, traitsType == TRAITS_TYPE_CLASS, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<>(), false, insideInterface);
        } catch (InterruptedException ex) {
            // ignore
        }
        writer.finishHilights();
        return writer.toString().trim();
    }

    public boolean isStatic() {
        return traitsType == TRAITS_TYPE_CLASS;
    }
}
