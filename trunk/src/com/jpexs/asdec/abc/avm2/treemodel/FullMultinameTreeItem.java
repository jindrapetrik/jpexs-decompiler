/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.Namespace;


public class FullMultinameTreeItem extends TreeItem {
    public int multinameIndex;
    public TreeItem name;
    public TreeItem namespace;

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = null;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name, TreeItem namespace) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = namespace;
    }

    public boolean isRuntime() {
        return (name != null) || (namespace != null);
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        if (name != null) {
            ret = name.toString(constants);
        } else {
            ret = constants.constant_multiname[multinameIndex].getName(constants);
        }
        if (namespace != null) {
            ret = ret + "[" + namespace.toString(constants) + "]";
        } else {
            Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
            if (ns != null) {
                ret = ret + "[" + ns.getName(constants) + "]";
            }
        }

        if ((name == null) && (namespace == null)) {
            ret = hilight(constants.constant_multiname[multinameIndex].getName(constants));
        }
        return ret;
    }


}
