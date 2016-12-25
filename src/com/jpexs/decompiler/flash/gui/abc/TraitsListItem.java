/*
 *  Copyright (C) 2010-2016 JPEXS
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
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class TraitsListItem {

    private final Type type;

    private final boolean isStatic;

    private final ABC abc;

    private final int classIndex;

    private final int index;

    private final int scriptIndex;

    public String STR_INSTANCE_INITIALIZER = AppStrings.translate("abc.traitslist.instanceinitializer");

    public String STR_CLASS_INITIALIZER = AppStrings.translate("abc.traitslist.classinitializer");

    public String STR_SCRIPT_INITIALIZER = AppStrings.translate("abc.traitslist.scriptinitializer");

    public TraitsListItem(Type type, int index, boolean isStatic, ABC abc, int classIndex, int scriptIndex) {
        this.type = type;
        this.index = index;
        this.isStatic = isStatic;
        this.abc = abc;
        this.classIndex = classIndex;
        this.scriptIndex = scriptIndex;
    }

    public int getGlobalTraitId() {
        if (type == Type.INITIALIZER) {
            if (!isStatic) {
                return -1;
                //return abc.class_info.get(classIndex).static_traits.traits.size() + abc.instance_info.get(classIndex).instance_traits.traits.size();
            } else {
                return -2;
                //return abc.class_info.get(classIndex).static_traits.traits.size() + abc.instance_info.get(classIndex).instance_traits.traits.size() + 1;
            }
        }
        if (type == Type.SCRIPT_INITIALIZER) {
            //return abc.class_info.get(classIndex).static_traits.traits.size() + abc.instance_info.get(classIndex).instance_traits.traits.size() + 2;
            return -3;
        }
        if (isStatic) {
            return index;
        } else {
            return abc.class_info.get(classIndex).static_traits.traits.size() + index;
        }
    }

    public String toStringName() {

        if (type == Type.INITIALIZER) {
            if (!isStatic) {
                return "__" + STR_INSTANCE_INITIALIZER;
            } else {
                return "__" + STR_CLASS_INITIALIZER;
            }
        }
        if (type == Type.SCRIPT_INITIALIZER) {
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
            if (type == Type.SCRIPT_INITIALIZER) {
                s = STR_SCRIPT_INITIALIZER;
            } else if (type == Type.INITIALIZER) {
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

    public Type getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public enum Type {
        METHOD,
        VAR,
        CONST,
        INITIALIZER,
        SCRIPT_INITIALIZER;

        public static Type getTypeForTrait(Trait t) {
            if (t instanceof TraitMethodGetterSetter) {
                return METHOD;
            }
            if (t instanceof TraitSlotConst) {
                if (((TraitSlotConst) t).isConst()) {
                    return CONST;
                } else {
                    return VAR;
                }
            }
            return null;
        }
    }
}
