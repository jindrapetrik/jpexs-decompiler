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

public class ForItem extends LoopItem implements Block {

    public List<GraphTargetItem> firstCommands;
    public GraphTargetItem expression;
    public List<GraphTargetItem> finalCommands;
    public List<GraphTargetItem> commands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(firstCommands);
        ret.add(commands);
        ret.add(finalCommands);
        return ret;
    }

    public ForItem(GraphSourceItem src, Loop loop, List<GraphTargetItem> firstCommands, GraphTargetItem expression, List<GraphTargetItem> finalCommands, List<GraphTargetItem> commands) {
        super(src, loop);
        this.firstCommands = firstCommands;
        this.expression = expression;
        this.finalCommands = finalCommands;
        this.commands = commands;
    }

    private String stripSemicolon(String s) {
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @Override
    public String toString(boolean highlight, List<Object> localData) {
        String ret = "";
        ret += "loop" + loop.id + ":\r\n";
        ret += hilight("for(", highlight);
        int p = 0;
        for (int i = 0; i < firstCommands.size(); i++) {
            if (firstCommands.get(i).isEmpty()) {
                continue;
            }

            if (p > 0) {
                ret += ",";
            }
            ret += stripSemicolon(firstCommands.get(i).toString(highlight, localData));
            p++;
        }
        ret += ";";
        ret += expression.toString(highlight, localData);
        ret += ";";
        p = 0;
        for (int i = 0; i < finalCommands.size(); i++) {
            if (finalCommands.get(i).isEmpty()) {
                continue;
            }
            if (p > 0) {
                ret += ",";
            }
            ret += stripSemicolon(finalCommands.get(i).toString(highlight, localData));
            p++;
        }
        ret += hilight(")", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                ret += ti.toStringSemicoloned(highlight, localData) + "\r\n";
            }
        }
        ret += hilight("}", highlight) + "\r\n";
        ret += ":loop" + loop.id;
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
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
