package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TrueItem extends GraphTargetItem {

    public TrueItem(GraphSourceItem src) {
        super(src, PRECEDENCE_PRIMARY);
    }

    @Override
    public String toString(boolean highlight, List<Object> localData) {
        return "true";
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
