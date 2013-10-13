package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 *
 * @author JPEXS
 */
public class TrueItem extends GraphTargetItem {

    public TrueItem(GraphSourceItem src) {
        super(src, PRECEDENCE_PRIMARY);
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        return hilight("true", writer);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
