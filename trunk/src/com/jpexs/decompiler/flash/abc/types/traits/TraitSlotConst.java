/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.List;

public class TraitSlotConst extends Trait implements TraitWithSlot {

    public int slot_id;
    public int type_index;
    public int value_index;
    public int value_kind;
    public GraphTargetItem assignedValue;

    @Override
    public int getSlotIndex() {
        return slot_id;
    }

    @Override
    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = abc.constants.constant_multiname[type_index].toString(abc.constants, fullyQualifiedNames);
        }
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " SlotConst " + abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " type=" + typeStr + " value=" + (new ValueKind(value_index, value_kind)).toString(abc.constants) + " metadata=" + Helper.intArrToString(metadata);
    }

    public String getType(ConstantPool constants, List<String> fullyQualifiedNames) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = constants.constant_multiname[type_index].getName(constants, fullyQualifiedNames);
        }
        return typeStr;
    }

    public String getNameStr(boolean highlight, ABC abc, List<String> fullyQualifiedNames) {
        String typeStr = getType(abc.constants, fullyQualifiedNames);
        if (typeStr.equals("*")) {
            typeStr = "";
        } else {
            typeStr = ":" + typeStr;
        }
        ValueKind val = null;
        if (value_kind != 0) {
            val = new ValueKind(value_index, value_kind);
        }

        String slotconst = "var";
        if (kindType == TRAIT_CONST) {
            slotconst = "const";
        }
        if (val != null && val.isNamespace()) {
            slotconst = "namespace";
        }
        return Highlighting.hilighSpecial(highlight, slotconst, "traittype") + " " + Highlighting.hilighSpecial(highlight, getName(abc).getName(abc.constants, fullyQualifiedNames), "traitname") + Highlighting.hilighSpecial(highlight, typeStr, "traittypename");

    }

    public String getValueStr(Trait parent, boolean highlight, ABC abc, List<String> fullyQualifiedNames) {
        String valueStr = null;
        ValueKind val = null;
        if (value_kind != 0) {
            val = new ValueKind(value_index, value_kind);
            valueStr = val.toString(abc.constants);
            valueStr = Highlighting.hilighSpecial(highlight, valueStr, "traitvalue");
        }

        if (assignedValue != null) {
            valueStr = Highlighting.trim(assignedValue.toString(highlight, LocalData.create(abc.constants, new HashMap<Integer, String>(), fullyQualifiedNames)));
            if (highlight && (parent instanceof TraitClass)) {
                TraitClass tc = (TraitClass) parent;
                int traitInitId = abc.class_info[tc.class_info].static_traits.traits.length
                        + abc.instance_info[tc.class_info].instance_traits.traits.length + 1;
                int initMethod = abc.class_info[tc.class_info].cinit_index;
                valueStr = Highlighting.hilighMethod(valueStr, initMethod);
                valueStr = Highlighting.hilighTrait(valueStr, traitInitId);
            }
        }
        return valueStr;
    }

    public String getNameValueStr(Trait parent, boolean highlight, ABC abc, List<String> fullyQualifiedNames) {
        String valueStr = getValueStr(parent, highlight, abc, fullyQualifiedNames);
        return getNameStr(highlight, abc, fullyQualifiedNames) + (valueStr == null ? "" : " = " + valueStr) + ";";
    }

    public boolean isNamespace() {
        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            return val.isNamespace();
        }
        return false;
    }

    @Override
    public String convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean parallel) {
        String modifier = getModifiers(abcTags, abc, isStatic) + " ";
        if (modifier.equals(" ")) {
            modifier = "";
        }
        Multiname n = getName(abc);
        boolean showModifier = true;
        if ((classIndex == -1) && (n != null)) {
            Namespace ns = n.getNamespace(abc.constants);
            if (ns == null) {
                showModifier = false;
            } else {
                if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
                    showModifier = false;
                }
            }
        }
        if (!showModifier) {
            modifier = "";
        }
        String ret = modifier + getNameStr(highlight, abc, fullyQualifiedNames);
        String valueStr = getValueStr(parent, highlight, abc, fullyQualifiedNames);

        if (valueStr != null) {
            ret += " = ";
            int befLen = Highlighting.stripHilights(Graph.INDENT_STRING + Graph.INDENT_STRING + ret).length();
            String[] valueStrParts = valueStr.split("\r\n");
            boolean first = true;
            for (int i = 0; i < valueStrParts.length; i++) {
                if (valueStrParts[i].equals("")) {
                    continue;
                }
                if (Highlighting.stripHilights(valueStrParts[i]).equals(Graph.INDENTOPEN)) {
                    if (!first) {
                        befLen += Graph.INDENT_STRING.length();
                    }
                    ret += valueStrParts[i].replace(Graph.INDENTOPEN, ""); //there can be highlights!
                    continue;
                }
                if (Highlighting.stripHilights(valueStrParts[i]).equals(Graph.INDENTCLOSE)) {
                    if (!first) {
                        befLen -= Graph.INDENT_STRING.length();
                    }
                    ret += valueStrParts[i].replace(Graph.INDENTCLOSE, ""); //there can be highlights!
                    continue;
                }
                if (!first) {
                    for (int j = 0; j < befLen; j++) {
                        ret += " ";
                    }
                }
                ret += valueStrParts[i];
                ret += "\r\n";
                first = false;
            }
        }
        ret = Graph.INDENT_STRING + Graph.INDENT_STRING + Highlighting.trim(ret) + ";";
        return ret;
    }

    public boolean isConst() {
        return kindType == TRAIT_CONST;
    }

    public boolean isVar() {
        return kindType == TRAIT_SLOT;
    }

    @Override
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) {
        //do nothing
        return 0;
    }
}
