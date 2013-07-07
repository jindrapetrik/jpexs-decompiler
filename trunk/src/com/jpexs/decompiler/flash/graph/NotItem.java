package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.ecma.EcmaScript;

/**
 *
 * @author JPEXS
 */
public class NotItem extends UnaryOpItem implements LogicalOpItem {

    public NotItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_UNARY, value, "!");
    }

    @Override
    public Object getResult() {
        Object ret = EcmaScript.toBoolean(value.getResult());
        if (ret == Boolean.TRUE) {
            return Boolean.FALSE;
        }
        if (ret == Boolean.FALSE) {
            return Boolean.TRUE;
        }
        return ret;
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }

    @Override
    public GraphTargetItem invert() {
        return value;
    }

    public GraphTargetItem getOriginal() {
        return value;
    }
}
