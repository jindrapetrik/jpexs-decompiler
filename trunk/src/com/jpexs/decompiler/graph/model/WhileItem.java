/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.LoopWithType;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class WhileItem extends LoopItem implements Block {

    public List<GraphTargetItem> expression;
    public List<GraphTargetItem> commands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(commands);
        return ret;
    }

    public WhileItem(GraphSourceItem src, Loop loop, List<GraphTargetItem> expression, List<GraphTargetItem> commands) {
        super(src, loop);
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    protected HilightedTextWriter appendTo(HilightedTextWriter writer, LocalData localData) {
        writer.startLoop(loop.id, LoopWithType.LOOP_TYPE_LOOP);
        writer.append("loop" + loop.id + ":").newLine();
        writer.append("while(");
        for (int i = 0; i < expression.size(); i++) {
            if (expression.get(i).isEmpty()) {
                continue;
            }
            if (i != 0) {
                writer.append(", ");
            }
            expression.get(i).toString(writer, localData);
        }
        writer.append(")").newLine();
        writer.append("{").newLine();
        writer.indent();
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                ti.toStringSemicoloned(writer, localData).newLine();
            }
        }
        writer.unindent();
        writer.append("}").newLine();
        writer.append(":loop" + loop.id);
        writer.endLoop(loop.id);
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : commands) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
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
