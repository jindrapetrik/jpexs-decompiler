package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.action.treemodel.operations.Inverted;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NotItem extends UnaryOpItem implements LogicalOpItem, Inverted {

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

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
