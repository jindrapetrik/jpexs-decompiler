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
    public String toString(List localData) {
        return hilight("break") + " " + "loop" + loopId;
    }
}
