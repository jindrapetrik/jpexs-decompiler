/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.ValueKind;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;


public class TraitSlotConst extends Trait {

    public int slot_id;
    public int type_index;
    public int value_index;
    public int value_kind;

    public TreeItem assignedValue;

    @Override
    public String toString(ConstantPool constants) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = constants.constant_multiname[type_index].toString(constants);
        }
        return "0x" + Helper.formatAddress(fileOffset) + " " + Helper.byteArrToString(bytes) + " SlotConst " + constants.constant_multiname[name_index].toString(constants) + " slot=" + slot_id + " type=" + typeStr + " value=" + (new ValueKind(value_index, value_kind)).toString(constants) + " metadata=" + Helper.intArrToString(metadata);
    }

    public String getNameValueStr(ConstantPool constants) {
        String typeStr = "*";
        if (type_index > 0) {
            typeStr = constants.constant_multiname[type_index].getName(constants);
        }
        String valueStr = "";
        if (value_kind != 0) {
            valueStr = " = " + (new ValueKind(value_index, value_kind)).toString(constants);
        }

        if (assignedValue != null) {
            valueStr = " = " + Highlighting.stripHilights(assignedValue.toString(constants));
        }

        String slotconst = "var";
        if (kindType == TRAIT_CONST) {
            slotconst = "const";
        }
        return slotconst + " " + constants.constant_multiname[name_index].getName(constants) + ":" + typeStr + valueStr;
    }

    @Override
    public String convert(ConstantPool constants, MethodInfo[] methodInfo, boolean isStatic) {
        String modifier = getModifiers(constants, isStatic) + " ";
        if (modifier.equals(" ")) modifier = "";
        return modifier + getNameValueStr(constants);
    }

}
