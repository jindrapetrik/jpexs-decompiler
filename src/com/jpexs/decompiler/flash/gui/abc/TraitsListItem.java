/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class TraitsListItem {

    private final TraitType type;

    private final boolean isStatic;

    private final ABC abc;

    private final int classIndex;

    private final int index;

    private final int scriptIndex;

    public static String STR_INSTANCE_INITIALIZER = ABCSearchResult.STR_INSTANCE_INITIALIZER;

    public static String STR_CLASS_INITIALIZER = ABCSearchResult.STR_CLASS_INITIALIZER;

    public static String STR_SCRIPT_INITIALIZER = ABCSearchResult.STR_SCRIPT_INITIALIZER;

    public TraitsListItem(TraitType type, int index, boolean isStatic, ABC abc, int classIndex, int scriptIndex) {
        this.type = type;
        this.index = index;
        this.isStatic = isStatic;
        this.abc = abc;
        this.classIndex = classIndex;
        this.scriptIndex = scriptIndex;
    }

    public int getGlobalTraitId() {
        return abc.getGlobalTraitId(type, isStatic, classIndex, index);
    }

    public String toStringName() {

        if (type == TraitType.INITIALIZER) {
            if (!isStatic) {
                return "__" + STR_INSTANCE_INITIALIZER;
            } else {
                return "__" + STR_CLASS_INITIALIZER;
            }
        }
        if (type == TraitType.SCRIPT_INITIALIZER) {
            return "__" + STR_SCRIPT_INITIALIZER;
        }
        if (isStatic) {
            return abc.class_info.get(classIndex).static_traits.traits.get(index).getName(abc).getName(abc.constants, null, false, true);
        } else {
            return abc.instance_info.get(classIndex).instance_traits.traits.get(index).getName(abc).getName(abc.constants, null, false, true);
        }
    }

    @Override
    public String toString() {
        String s = "";
        try {
            if (type == TraitType.SCRIPT_INITIALIZER) {
                s = STR_SCRIPT_INITIALIZER;
            } else if (type == TraitType.INITIALIZER) {
                if (!isStatic) {
                    s = STR_INSTANCE_INITIALIZER;
                } else {
                    s = STR_CLASS_INITIALIZER;
                }
            } else if (isStatic) {
                ConvertData convertData = new ConvertData();
                Trait trait = abc.class_info.get(classIndex).static_traits.traits.get(index);
                trait.convertHeader(null, convertData, "", abc, true, ScriptExportMode.AS, scriptIndex, classIndex, new NulWriter(), new ArrayList<>(), false);
                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
                trait.toStringHeader(null, convertData, "", abc, true, ScriptExportMode.AS, scriptIndex, classIndex, writer, new ArrayList<>(), false);
                s = writer.toString();
            } else {
                ConvertData convertData = new ConvertData();
                Trait trait = abc.instance_info.get(classIndex).instance_traits.traits.get(index);
                trait.convertHeader(null, convertData, "", abc, false, ScriptExportMode.AS, scriptIndex, classIndex, new NulWriter(), new ArrayList<>(), false);
                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
                trait.toStringHeader(null, convertData, "", abc, false, ScriptExportMode.AS, scriptIndex, classIndex, writer, new ArrayList<>(), false);
                s = writer.toString();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(TraitsListItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        s = s.replaceAll("[ \r\n]+", " ");
        return s;
    }

    public TraitType getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getIndex() {
        return index;
    }
}
