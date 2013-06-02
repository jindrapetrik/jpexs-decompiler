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
package com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses;

import com.jpexs.decompiler.flash.abc.avm2.treemodel.*;
import com.jpexs.decompiler.flash.graph.Block;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.Loop;
import com.jpexs.decompiler.flash.graph.LoopItem;
import java.util.ArrayList;
import java.util.List;

public class ForInTreeItem extends LoopItem implements Block {

    public InTreeItem expression;
    public List<GraphTargetItem> commands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<List<GraphTargetItem>>();
        ret.add(commands);
        return ret;
    }

    public ForInTreeItem(GraphSourceItem instruction, Loop loop, InTreeItem expression, List<GraphTargetItem> commands) {
        super(instruction, loop);
        if (!commands.isEmpty()) {
            GraphTargetItem firstAssign = commands.get(0);
            if (firstAssign instanceof SetTypeTreeItem) {
                if (expression.object instanceof LocalRegTreeItem) {
                    if (((SetTypeTreeItem) firstAssign).getValue().getNotCoerced() instanceof LocalRegTreeItem) {
                        if (((LocalRegTreeItem) ((SetTypeTreeItem) firstAssign).getValue().getNotCoerced()).regIndex == ((LocalRegTreeItem) expression.object).regIndex) {
                            commands.remove(0);
                            expression.object = ((SetTypeTreeItem) firstAssign).getObject();
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
    public String toString(List<Object> localData) {
        String ret = "";
        ret += "loop" + loop.id + ":\r\n";
        ret += hilight("for (") + expression.toString(localData) + hilight(")") + "\r\n{\r\n";
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                ret += ti.toStringSemicoloned(localData) + "\r\n";
            }
        }
        ret += hilight("}") + "\r\n";
        ret += ":loop" + loop.id;
        return ret;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<ContinueItem>();
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
}
