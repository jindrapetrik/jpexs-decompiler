/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.Multiname;


public class SetSlotTreeItem extends TreeItem {
    public Multiname slotName;
    public TreeItem value;
    public TreeItem scope;

    public SetSlotTreeItem(AVM2Instruction instruction, TreeItem scope, Multiname slotName, TreeItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.slotName = slotName;
        this.value = value;
        this.scope = scope;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";

        if (!(scope instanceof NewActivationTreeItem)) {
            ret = scope.toString(constants) + ".";
        }
        if(scope instanceof LocalRegTreeItem){
            if(((LocalRegTreeItem)scope).computedValue !=null){
                if(((LocalRegTreeItem)scope).computedValue instanceof NewActivationTreeItem){
                    ret="";
                }
            }
        }
        return ret + hilight(slotName.getName(constants)) + hilight("=") + value.toString(constants) + ";";
    }

}
