/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class NewFunctionTreeItem extends TreeItem {
    public String paramStr;
    public String returnStr;
    public String functionBody;

    public NewFunctionTreeItem(AVM2Instruction instruction, String paramStr, String returnStr, String functionBody) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.paramStr = paramStr;
        this.returnStr = returnStr;
        this.functionBody = functionBody;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("new function(" + paramStr + "):" + returnStr + "\r\n{\r\n" + functionBody + "}\r\n");
    }


}
