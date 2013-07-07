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
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.graph.Graph;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.List;

public class FunctionTreeItem extends TreeItem {

    public List<GraphTargetItem> actions;
    public List<String> constants;
    public String functionName;
    public List<String> paramNames;
    public GraphTargetItem calculatedFunctionName;
    private int regStart;

    public FunctionTreeItem(GraphSourceItem instruction, String functionName, List<String> paramNames, List<GraphTargetItem> actions, List<String> constants, int regStart) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.actions = actions;
        this.constants = constants;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.regStart = regStart;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (true) {
            //return "<func>";
        }
        String ret = hilight("function");
        if (calculatedFunctionName != null) {
            ret += " " + calculatedFunctionName.toStringNoQuotes(constants);
        } else if (!functionName.equals("")) {
            ret += " " + functionName;
        }
        ret += hilight("(");
        for (int p = 0; p < paramNames.size(); p++) {
            if (p > 0) {
                ret += hilight(", ");
            }
            String pname = paramNames.get(p);
            if (pname == null || pname.equals("")) {
                pname = "register" + (regStart + p);
            }
            ret += hilight(pname);
        }
        ret += hilight(")") + "\r\n{\r\n" + Graph.graphToString(actions, constants) + "}";
        return ret;
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem ti : actions) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean isCompileTime() {
        for (GraphTargetItem a : actions) {
            if (!a.isCompileTime()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getResult() {
        if (!actions.isEmpty()) {
            if (actions.get(actions.size() - 1) instanceof ReturnTreeItem) {
                ReturnTreeItem r = (ReturnTreeItem) actions.get(actions.size() - 1);
                return r.value.getResult();
            }
        }
        return 0;
    }

    @Override
    public boolean needsNewLine() {
        return true;
    }
}
