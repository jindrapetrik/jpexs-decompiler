/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.helpers.Helper;


public class TraitClass extends Trait {

    public int slot_id;
    public int class_info;

    @Override
    public String toString(ConstantPool constants) {
        return "Class " + constants.constant_multiname[name_index].toString(constants) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
    }
}
