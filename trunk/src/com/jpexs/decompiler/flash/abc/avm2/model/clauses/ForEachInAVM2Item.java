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
package com.jpexs.decompiler.flash.abc.avm2.model.clauses;

import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import java.util.ArrayList;
import java.util.List;

public class ForEachInAVM2Item extends LoopItem implements Block {

    public InAVM2Item expression;
    public List<GraphTargetItem> commands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(commands);
        return ret;
    }

    public ForEachInAVM2Item(GraphSourceItem instruction, Loop loop, InAVM2Item expression, List<GraphTargetItem> commands) {
        super(instruction, loop);
        if (!commands.isEmpty()) {
            GraphTargetItem firstAssign = commands.get(0);
            if (firstAssign instanceof SetTypeAVM2Item) {
                if (expression.object instanceof LocalRegAVM2Item) {
                    if (((SetTypeAVM2Item) firstAssign).getValue().getNotCoerced() instanceof LocalRegAVM2Item) {
                        if (((LocalRegAVM2Item) ((SetTypeAVM2Item) firstAssign).getValue().getNotCoerced()).regIndex == ((LocalRegAVM2Item) expression.object).regIndex) {
                            commands.remove(0);
                            expression.object = ((SetTypeAVM2Item) firstAssign).getObject();
                        }
                    }

                }
                //locAssign.
            }
        }
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, List<Object> localData) {
        hilight("loop" + loop.id + ":", writer).appendNewLine();
        hilight("for each (", writer);
        expression.toString(writer, localData);
        hilight(")", writer).appendNewLine();
        hilight("{", writer).appendNewLine();
        hilight(Graph.INDENTOPEN, writer).appendNewLine();
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                ti.toStringSemicoloned(writer, localData).appendNewLine();
            }
        }
        hilight(Graph.INDENTCLOSE, writer).appendNewLine();
        hilight("}", writer).appendNewLine();
        hilight(":loop" + loop.id, writer);
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
    public boolean hasReturnValue() {
        return false;
    }
}
