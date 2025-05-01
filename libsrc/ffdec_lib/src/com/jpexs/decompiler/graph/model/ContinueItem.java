/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;

/**
 * Continue statement.
 *
 * @author JPEXS
 */
public class ContinueItem extends GraphTargetItem {

    /**
     * Loop id
     */
    public long loopId;

    /**
     * Label required
     */
    private boolean labelRequired;

    /**
     * Constructor.
     *
     * @param dialect Dialect
     * @param src Source item
     * @param lineStartIns Line start instruction
     * @param loopId Loop id
     */
    public ContinueItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns, long loopId) {
        super(dialect, src, lineStartIns, NOPRECEDENCE);
        this.loopId = loopId;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("continue");
        if (writer instanceof NulWriter) {
            NulWriter nulWriter = (NulWriter) writer;
            labelRequired = loopId != nulWriter.getNonSwitchLoop();
            if (labelRequired) {
                nulWriter.setLoopUsed(loopId);
            }
        }
        if (labelRequired) {
            writer.append(" loop").append(loopId);
        }
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}
