/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.helpers.Helper;


public class TraitFunction extends Trait {

    public int slot_index;
    public int method_info;

    @Override
    public String toString(ConstantPool constants) {
        return "Function " + constants.constant_multiname[name_index].toString(constants) + " slot=" + slot_index + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
    }
}
