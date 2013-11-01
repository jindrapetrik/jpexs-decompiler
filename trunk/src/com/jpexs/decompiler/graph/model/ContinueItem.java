package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ContinueItem extends GraphTargetItem {

    public long loopId;
    private boolean labelRequired;

    public ContinueItem(GraphSourceItem src, long loopId) {
        super(src, NOPRECEDENCE);
        this.loopId = loopId;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("continue");
        if (writer instanceof NulWriter) {
            NulWriter nulWriter = (NulWriter) writer;
            labelRequired = loopId != nulWriter.getNonSwitchLoop();
            if (labelRequired) {
                nulWriter.setLoopUsed(loopId);
            }
        }
        if (labelRequired) {
            writer.append(" loop" + loopId);            
        }
        return writer;
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
