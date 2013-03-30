package com.jpexs.decompiler.flash.graph;

/**
 *
 * @author JPEXS
 */
public class NotItem extends UnaryOpItem implements LogicalOpItem {

    public NotItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_UNARY, value, "!");
    }

    @Override
    public boolean toBoolean() {
        boolean ret = !value.toBoolean();
        return ret;
    }

    @Override
    public GraphTargetItem invert() {
        return value;
    }

    public GraphTargetItem getOriginal() {
        return value;
    }
}
