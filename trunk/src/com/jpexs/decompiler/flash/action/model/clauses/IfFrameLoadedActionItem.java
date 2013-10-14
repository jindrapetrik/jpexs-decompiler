/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionWaitForFrame2;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IfFrameLoadedActionItem extends ActionItem implements Block {

    private List<GraphTargetItem> actions;
    private GraphTargetItem frame;

    public IfFrameLoadedActionItem(GraphTargetItem frame, List<GraphTargetItem> actions, GraphSourceItem instruction) {
        super(instruction, NOPRECEDENCE);
        this.actions = actions;
        this.frame = frame;
    }

    @Override
    protected HilightedTextWriter appendTo(HilightedTextWriter writer, LocalData localData) {
        writer.append("ifFrameLoaded(");
        frame.toString(writer, localData);
        writer.append(")").newLine();
        writer.append("{").newLine();
        writer.indent();
        Graph.graphToString(actions, writer, localData);
        writer.unindent();
        return writer.append("}");
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<ContinueItem> getContinues() {
        return new ArrayList<>();
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(actions);
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> body = generator.generate(localData, actions);
        return toSourceMerge(localData, generator, frame, new ActionWaitForFrame2(body.size()), body);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
