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
import static com.jpexs.decompiler.flash.abc.types.traits.Trait.TRAIT_CONST;
import com.jpexs.decompiler.flash.graph.Graph;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.HashMap;
import java.util.List;

public class TraitSlotConst extends Trait {

    public int slot_id;
    public int type_index;
    public int value_index;
    public int value_kind;
    public GraphTargetItem assignedValue;

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

    public String getNameStr(ABC abc, List<String> fullyQualifiedNames) {
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
        return slotconst + " " + getName(abc).getName(abc.constants, fullyQualifiedNames) + typeStr;

    }

    public String getValueStr(ABC abc, List<String> fullyQualifiedNames) {
        String valueStr = null;
        ValueKind val = null;
        if (value_kind != 0) {
            val = new ValueKind(value_index, value_kind);
            valueStr = val.toString(abc.constants);
        }

        if (assignedValue != null) {
            valueStr = Highlighting.stripHilights(assignedValue.toString(abc.constants, new HashMap<Integer, String>(), fullyQualifiedNames));
        }
        return valueStr;
    }

    public String getNameValueStr(ABC abc, List<String> fullyQualifiedNames) {
        String valueStr = getValueStr(abc, fullyQualifiedNames);
        return getNameStr(abc, fullyQualifiedNames) + (valueStr == null ? "" : " = " + valueStr) + ";";
    }

    public boolean isNamespace() {
        if (value_kind != 0) {
            ValueKind val = new ValueKind(value_index, value_kind);
            return val.isNamespace();
        }
        return false;
    }

    @Override
    public String convert(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight, List<String> fullyQualifiedNames) {
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
        String ret = ABC.IDENT_STRING + ABC.IDENT_STRING + modifier + getNameStr(abc, fullyQualifiedNames);
        String valueStr = getValueStr(abc, fullyQualifiedNames);
        if (valueStr != null) {
            ret += " = ";
            int befLen = ret.length();
            String valueStrParts[] = valueStr.split("\r\n");
            boolean first = true;
            for (int i = 0; i < valueStrParts.length; i++) {
                if (valueStrParts[i].equals("")) {
                    continue;
                }
                if (Highlighting.stripHilights(valueStrParts[i]).equals(Graph.INDENTOPEN)) {
                    //befLen+=ABC.IDENT_STRING.length();
                    continue;
                }
                if (Highlighting.stripHilights(valueStrParts[i]).equals(Graph.INDENTCLOSE)) {
                    //befLen-=ABC.IDENT_STRING.length();
                    continue;
                }
                if (!first) {
                    ret += ABC.IDENT_STRING + ABC.IDENT_STRING;
                    for (int j = 0; j < befLen; j++) {
                        ret += " ";
                    }
                }
                ret += valueStrParts[i];
                ret += "\r\n";
                first = false;
            }
        }
        if (ret.endsWith("\r\n")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        ret += ";";
        return ret;
    }

    public boolean isConst() {
        return kindType == TRAIT_CONST;
    }

    public boolean isVar() {
        return kindType == TRAIT_SLOT;
    }
}
