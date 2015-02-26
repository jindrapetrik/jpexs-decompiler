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

    public ConstVarMultinameUsage(ABC abc, int multinameIndex, int classIndex, int traitIndex, boolean isStatic, Traits traits, int parentTraitIndex) {
        super(abc, multinameIndex, classIndex, traitIndex, isStatic, traits, parentTraitIndex);
    }

    @Override
    public String toString() {
        NulWriter nulWriter = new NulWriter();
        if (parentTraitIndex > -1) {
            if (isStatic) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).convertHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
            } else {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).convertHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).convertHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, nulWriter, new ArrayList<String>(), false);
        } catch (InterruptedException ex) {
            // ignore
        }

        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        writer.appendNoHilight(super.toString() + " ");
        if (parentTraitIndex > -1) {
            if (isStatic) {
                ((TraitMethodGetterSetter) abc.class_info.get(classIndex).static_traits.traits.get(parentTraitIndex)).toStringHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
            } else {
                ((TraitMethodGetterSetter) abc.instance_info.get(classIndex).instance_traits.traits.get(parentTraitIndex)).toStringHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
            }
        }
        try {
            ((TraitSlotConst) traits.traits.get(traitIndex)).toStringHeader(null, "", abc, isStatic, ScriptExportMode.AS, -1/*FIXME*/, classIndex, writer, new ArrayList<String>(), false);
        } catch (InterruptedException ex) {
            // ignore
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
