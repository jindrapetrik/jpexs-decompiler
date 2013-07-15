package com.jpexs.decompiler.flash.graph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class BreakItem extends GraphTargetItem {

    public long loopId;

    public BreakItem(GraphSourceItem src, long loopId) {
        super(src, NOPRECEDENCE);
        this.loopId = loopId;
    }

    @Override
    public String toString(List<Object> localData) {
        return hilight("break") + " " + "loop" + loopId;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
