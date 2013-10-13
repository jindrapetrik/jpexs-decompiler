package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.HashMap;
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
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("break ", writer);
        return hilight("loop" + loopId, writer);
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
