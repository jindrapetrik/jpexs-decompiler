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

import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class DoWhileItem extends LoopItem implements Block {

    public List<GraphTargetItem> commands;
    public List<GraphTargetItem> expression;

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(commands);
        return ret;
    }

    public DoWhileItem(GraphSourceItem src, Loop loop, List<GraphTargetItem> commands, List<GraphTargetItem> expression) {
        super(src, loop);
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    public String toString(boolean highlight, List<Object> localData) {
        String ret = "";
        ret += hilight("loop" + loop.id + ":", highlight) + "\r\n";
        ret += hilight("do", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                ret += ti.toStringSemicoloned(highlight, localData) + "\r\n";
            }
        }
        String expStr = "";
        for (int i = 0; i < expression.size(); i++) {
            if (expression.get(i).isEmpty()) {
                continue;
            }
            if (!expStr.equals("")) {
                expStr += hilight(", ", highlight);
            }
            expStr += expression.get(i).toString(highlight, localData);
        }
        ret += hilight("}", highlight) + "\r\n" + hilight("while(", highlight) + expStr + hilight(");", highlight) + "\r\n";
        ret += hilight(":loop" + loop.id, highlight);

        return ret;
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
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
