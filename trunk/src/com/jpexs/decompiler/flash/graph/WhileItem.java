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
package com.jpexs.decompiler.flash.graph;

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
    public String toString(List<Object> localData) {
        String ret = "";
        ret += "loop" + loop.id + ":\r\n";
        String expStr = "";
        for (int i = 0; i < expression.size(); i++) {
            if (expression.get(i).isEmpty()) {
                continue;
            }
            if (!expStr.equals("")) {
                expStr += ", ";
            }
            expStr += expression.get(i).toString(localData);
        }
        ret += hilight("while(") + expStr + hilight(")") + "\r\n{\r\n";
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
}
