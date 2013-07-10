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
package com.jpexs.decompiler.flash.action.treemodel.clauses;

import com.jpexs.decompiler.flash.KeyValue;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.Block;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class ClassTreeItem extends TreeItem implements Block {

    public List<GraphTargetItem> functions;
    public List<GraphTargetItem> staticFunctions;
    public GraphTargetItem extendsOp;
    public List<GraphTargetItem> implementsOp;
    public GraphTargetItem className;
    public List<KeyValue<GraphTargetItem, GraphTargetItem>> vars;
    public List<KeyValue<GraphTargetItem, GraphTargetItem>> staticVars;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(functions);
        ret.add(staticFunctions);
        return ret;
    }

    public ClassTreeItem(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, List<GraphTargetItem> functions, List<KeyValue<GraphTargetItem, GraphTargetItem>> vars, List<GraphTargetItem> staticFunctions, List<KeyValue<GraphTargetItem, GraphTargetItem>> staticVars) {
        super(null, NOPRECEDENCE);
        this.className = className;
        this.functions = functions;
        this.vars = vars;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.staticFunctions = staticFunctions;
        this.staticVars = staticVars;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret;
        ret = hilight("class ") + className.toStringNoQuotes(Helper.toList(constants));
        if (extendsOp != null) {
            ret += hilight(" extends ") + extendsOp.toStringNoQuotes(Helper.toList(constants));
        }
        if (!implementsOp.isEmpty()) {
            ret += hilight(" implements ");
            boolean first = true;
            for (GraphTargetItem t : implementsOp) {
                if (!first) {
                    ret += ", ";
                }
                first = false;
                ret += Action.getWithoutGlobal(t).toString(constants);
            }
        }
        ret += "\r\n{\r\n";
        for (GraphTargetItem f : functions) {
            ret += f.toString(constants) + "\r\n";
        }
        for (GraphTargetItem f : staticFunctions) {
            ret += "static " + f.toString(constants) + "\r\n";
        }
        for (KeyValue<GraphTargetItem, GraphTargetItem> item : vars) {
            ret += "var " + item.key.toStringNoQuotes(constants) + " = " + item.value.toString(constants) + ";\r\n";
        }
        for (KeyValue<GraphTargetItem, GraphTargetItem> item : staticVars) {
            ret += "static var " + item.key.toStringNoQuotes(constants) + " = " + item.value.toString(constants) + ";\r\n";
        }
        ret += "}\r\n";
        return ret;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }
}
