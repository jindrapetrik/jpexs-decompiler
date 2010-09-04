/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.ABCException;


public class ExceptionTreeItem extends TreeItem {
    public ABCException exception;

    public ExceptionTreeItem(ABCException exception) {
        super(null, NOPRECEDENCE);
        this.exception = exception;
    }

    @Override
    public String toString(ConstantPool constants) {
        return exception.getVarName(constants);
    }


}
